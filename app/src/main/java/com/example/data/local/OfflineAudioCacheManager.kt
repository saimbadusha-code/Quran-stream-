package com.example.data.local

import android.content.Context
import com.example.data.model.AudioTrack
import com.example.data.model.DownloadPermission
import com.example.data.model.OfflineSavedTrackInfo
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

data class OfflineDownloadsUiState(
    val savedTracks: Map<String, OfflineSavedTrackInfo> = emptyMap(),
    val activeDownloadProgress: Map<String, Float> = emptyMap(),
    val lastDownloadError: String? = null
)

class OfflineAudioCacheManager(context: Context) {

    private val cacheDir = File(context.applicationContext.filesDir, "offline_quran_audio").apply {
        if (!exists()) mkdirs()
    }
    private val manifestFile = File(cacheDir, "offline_manifest.json")

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _state = MutableStateFlow(OfflineDownloadsUiState(savedTracks = readManifestFromDisk()))
    val state: StateFlow<OfflineDownloadsUiState> = _state.asStateFlow()

    fun getLocalFileForTrack(trackId: String): File? {
        val info = _state.value.savedTracks[trackId] ?: return null
        val f = File(info.localFilePath)
        return if (f.exists() && f.length() > 0L) f else null
    }

    fun isDownloadPermitted(track: AudioTrack): Boolean {
        return track.licenceMetadata.downloadPermission == DownloadPermission.PERSONAL_OFFLINE_PERMITTED &&
            track.licenceMetadata.permissionConfirmed &&
            !track.audioUrl.isNullOrBlank()
    }

    suspend fun downloadTrackForPersonalOfflineUse(track: AudioTrack): Result<OfflineSavedTrackInfo> =
        withContext(Dispatchers.IO) {
            if (!isDownloadPermitted(track)) {
                val msg = "Offline download disabled for ${track.sourceProvider.shortBadge}: provider terms allow streaming only."
                _state.update { it.copy(lastDownloadError = msg) }
                return@withContext Result.failure(IllegalStateException(msg))
            }

            // Prevent duplicate downloads
            _state.value.savedTracks[track.id]?.let { existing ->
                if (File(existing.localFilePath).exists()) {
                    return@withContext Result.success(existing)
                }
            }
            if (track.id in _state.value.activeDownloadProgress) {
                return@withContext Result.failure(IllegalStateException("Download already in progress."))
            }

            val url = track.audioUrl ?: return@withContext Result.failure(IllegalStateException("Missing stream URL."))
            val safeFileName = "${track.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.mp3"
            val tempFile = File(cacheDir, "$safeFileName.part")
            val destFile = File(cacheDir, safeFileName)

            _state.update {
                it.copy(
                    activeDownloadProgress = it.activeDownloadProgress + (track.id to 0.02f),
                    lastDownloadError = null
                )
            }

            try {
                val req = Request.Builder().url(url).get().build()
                okHttp.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        throw IllegalStateException("Download failed with HTTP ${resp.code}")
                    }
                    val body = resp.body ?: throw IllegalStateException("Empty audio response")
                    val totalBytes = body.contentLength()

                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(16 * 1024)
                            var downloaded = 0L
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                downloaded += read
                                if (totalBytes > 0L) {
                                    val frac = (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0.05f, 0.99f)
                                    _state.update { st ->
                                        st.copy(activeDownloadProgress = st.activeDownloadProgress + (track.id to frac))
                                    }
                                }
                            }
                            output.flush()
                        }
                    }
                }

                if (destFile.exists()) destFile.delete()
                if (!tempFile.renameTo(destFile)) {
                    tempFile.copyTo(destFile, overwrite = true)
                    tempFile.delete()
                }

                val savedInfo = OfflineSavedTrackInfo(
                    trackId = track.id,
                    surahNumber = track.surahNumber,
                    surahName = track.surahName,
                    reciterId = track.reciterId,
                    reciterName = track.reciterName,
                    localFilePath = destFile.absolutePath,
                    fileSizeBytes = destFile.length(),
                    providerCode = track.sourceProvider.code,
                    attributionText = track.licenceMetadata.attributionText,
                    licenceName = track.licenceMetadata.licenceName,
                    savedAtEpochMs = System.currentTimeMillis()
                )

                val updatedMap = _state.value.savedTracks + (track.id to savedInfo)
                writeManifestToDisk(updatedMap)
                _state.update {
                    it.copy(
                        savedTracks = updatedMap,
                        activeDownloadProgress = it.activeDownloadProgress - track.id,
                        lastDownloadError = null
                    )
                }
                Result.success(savedInfo)
            } catch (e: Exception) {
                tempFile.delete()
                val err = e.localizedMessage ?: "Failed to save chapter offline."
                _state.update {
                    it.copy(
                        activeDownloadProgress = it.activeDownloadProgress - track.id,
                        lastDownloadError = err
                    )
                }
                Result.failure(e)
            }
        }

    suspend fun removeOfflineTrack(trackId: String) = withContext(Dispatchers.IO) {
        val current = _state.value.savedTracks[trackId] ?: return@withContext
        File(current.localFilePath).delete()
        val updated = _state.value.savedTracks - trackId
        writeManifestToDisk(updated)
        _state.update { it.copy(savedTracks = updated) }
    }

    fun clearDownloadError() {
        _state.update { it.copy(lastDownloadError = null) }
    }

    private fun readManifestFromDisk(): Map<String, OfflineSavedTrackInfo> {
        if (!manifestFile.exists()) return emptyMap()
        return try {
            val arr = JSONArray(manifestFile.readText())
            buildMap {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val path = obj.optString("localFilePath")
                    if (path.isNotBlank() && File(path).exists()) {
                        val id = obj.optString("trackId")
                        put(
                            id,
                            OfflineSavedTrackInfo(
                                trackId = id,
                                surahNumber = obj.optInt("surahNumber", 1),
                                surahName = obj.optString("surahName"),
                                reciterId = obj.optString("reciterId"),
                                reciterName = obj.optString("reciterName"),
                                localFilePath = path,
                                fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                                providerCode = obj.optString("providerCode"),
                                attributionText = obj.optString("attributionText"),
                                licenceName = obj.optString("licenceName"),
                                savedAtEpochMs = obj.optLong("savedAtEpochMs", 0L)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun writeManifestToDisk(entries: Map<String, OfflineSavedTrackInfo>) {
        try {
            val arr = JSONArray()
            entries.values.forEach { item ->
                arr.put(
                    JSONObject().apply {
                        put("trackId", item.trackId)
                        put("surahNumber", item.surahNumber)
                        put("surahName", item.surahName)
                        put("reciterId", item.reciterId)
                        put("reciterName", item.reciterName)
                        put("localFilePath", item.localFilePath)
                        put("fileSizeBytes", item.fileSizeBytes)
                        put("providerCode", item.providerCode)
                        put("attributionText", item.attributionText)
                        put("licenceName", item.licenceName)
                        put("savedAtEpochMs", item.savedAtEpochMs)
                    }
                )
            }
            manifestFile.writeText(arr.toString())
        } catch (_: Exception) {
        }
    }
}
