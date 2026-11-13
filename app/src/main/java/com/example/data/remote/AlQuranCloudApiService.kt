package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

data class AlQuranCloudEditionsResponseDto(
    @property:Json(name = "code") val code: Int = 200,
    @property:Json(name = "status") val status: String? = null,
    @property:Json(name = "data") val data: List<AlQuranCloudEditionDto> = emptyList()
)

data class AlQuranCloudEditionDto(
    @property:Json(name = "identifier") val identifier: String,
    @property:Json(name = "language") val language: String? = null,
    @property:Json(name = "name") val name: String? = null,
    @property:Json(name = "englishName") val englishName: String? = null,
    @property:Json(name = "format") val format: String? = null,
    @property:Json(name = "type") val type: String? = null,
    @property:Json(name = "bitrate") val bitrate: String? = null
)

data class HttpStreamProbeResult(
    val isReachable: Boolean,
    val httpCode: Int,
    val contentType: String?,
    val diagnosticMessage: String
)

interface StreamProbeClient {
    suspend fun fetchSurahAudioEditions(): List<AlQuranCloudEditionDto>
    suspend fun fetchVerseAudioEditions(): List<AlQuranCloudEditionDto>
    suspend fun verifyAudioUrl(url: String): HttpStreamProbeResult
}

interface AlQuranCloudApiService {
    @GET("v1/edition/format/audio")
    suspend fun getVerseAudioEditions(): AlQuranCloudEditionsResponseDto
}

class OkHttpStreamProbeClient : StreamProbeClient {
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val api: AlQuranCloudApiService = Retrofit.Builder()
        .baseUrl("https://api.alquran.cloud/")
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AlQuranCloudApiService::class.java)

    override suspend fun fetchSurahAudioEditions(): List<AlQuranCloudEditionDto> =
        withContext(Dispatchers.IO) {
            val req = Request.Builder()
                .url("https://cdn.islamic.network/quran/info/by-surah/info.json")
                .get()
                .build()
            try {
                okHttp.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext emptyList()
                    val body = resp.body?.string() ?: return@withContext emptyList()
                    val listType = Types.newParameterizedType(
                        List::class.java,
                        AlQuranCloudEditionDto::class.java
                    )
                    val adapter = moshi.adapter<List<AlQuranCloudEditionDto>>(listType)
                    adapter.fromJson(body).orEmpty()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

    override suspend fun fetchVerseAudioEditions(): List<AlQuranCloudEditionDto> =
        withContext(Dispatchers.IO) {
            try {
                api.getVerseAudioEditions().data
            } catch (_: Exception) {
                emptyList()
            }
        }

    override suspend fun verifyAudioUrl(url: String): HttpStreamProbeResult =
        withContext(Dispatchers.IO) {
            if (!url.startsWith("https://")) {
                return@withContext HttpStreamProbeResult(
                    isReachable = false,
                    httpCode = 0,
                    contentType = null,
                    diagnosticMessage = "Rejected non-HTTPS audio URL"
                )
            }

            try {
                val headReq = Request.Builder()
                    .url(url)
                    .head()
                    .build()

                okHttp.newCall(headReq).execute().use { headResp ->
                    val ct = headResp.header("Content-Type")
                    if (headResp.isSuccessful) {
                        return@withContext HttpStreamProbeResult(
                            isReachable = true,
                            httpCode = headResp.code,
                            contentType = ct,
                            diagnosticMessage = "HTTP ${headResp.code} verified via HEAD"
                        )
                    }
                    // Some CDN edge nodes disallow HEAD (405/403); verify with 2-byte Range GET
                    if (headResp.code !in listOf(403, 405)) {
                        return@withContext HttpStreamProbeResult(
                            isReachable = false,
                            httpCode = headResp.code,
                            contentType = ct,
                            diagnosticMessage = "HTTP ${headResp.code} returned for stream check"
                        )
                    }
                }

                val rangeReq = Request.Builder()
                    .url(url)
                    .header("Range", "bytes=0-1")
                    .get()
                    .build()

                okHttp.newCall(rangeReq).execute().use { rangeResp ->
                    val ok = rangeResp.code in listOf(200, 206)
                    HttpStreamProbeResult(
                        isReachable = ok,
                        httpCode = rangeResp.code,
                        contentType = rangeResp.header("Content-Type"),
                        diagnosticMessage = if (ok) {
                            "HTTP ${rangeResp.code} verified via byte-range probe"
                        } else {
                            "HTTP ${rangeResp.code} on byte-range probe"
                        }
                    )
                }
            } catch (e: Exception) {
                HttpStreamProbeResult(
                    isReachable = false,
                    httpCode = -1,
                    contentType = null,
                    diagnosticMessage = "Network probe failed: ${e.javaClass.simpleName}"
                )
            }
        }
}
