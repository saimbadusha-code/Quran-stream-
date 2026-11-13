package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class ChaptersResponseDto(
    @property:Json(name = "chapters") val chapters: List<ChapterDto> = emptyList()
)

data class ChapterDto(
    @property:Json(name = "id") val id: Int,
    @property:Json(name = "revelation_place") val revelationPlace: String? = null,
    @property:Json(name = "name_simple") val nameSimple: String,
    @property:Json(name = "name_arabic") val nameArabic: String,
    @property:Json(name = "verses_count") val versesCount: Int,
    @property:Json(name = "translated_name") val translatedName: TranslatedNameDto? = null
)

data class TranslatedNameDto(
    @property:Json(name = "name") val name: String? = null,
    @property:Json(name = "language_name") val languageName: String? = null
)

data class ChapterRecitersResponseDto(
    @property:Json(name = "reciters") val reciters: List<ChapterReciterDto> = emptyList()
)

data class ChapterReciterDto(
    @property:Json(name = "id") val id: Int,
    @property:Json(name = "name") val name: String,
    @property:Json(name = "arabic_name") val arabicName: String? = null,
    @property:Json(name = "relative_path") val relativePath: String? = null,
    @property:Json(name = "format") val format: String? = null
)

data class ChapterRecitationResponseDto(
    @property:Json(name = "audio_file") val audioFile: AudioFileDto? = null
)

data class AudioFileDto(
    @property:Json(name = "id") val id: Int? = null,
    @property:Json(name = "chapter_id") val chapterId: Int? = null,
    @property:Json(name = "file_size") val fileSize: Double? = null,
    @property:Json(name = "format") val format: String? = null,
    @property:Json(name = "audio_url") val audioUrl: String? = null,
    @property:Json(name = "timestamps") val timestamps: List<VerseTimestampDto>? = null
)

data class VerseTimestampDto(
    @property:Json(name = "verse_key") val verseKey: String,
    @property:Json(name = "timestamp_from") val timestampFrom: Long,
    @property:Json(name = "timestamp_to") val timestampTo: Long,
    @property:Json(name = "duration") val duration: Long? = null
)

data class VersesResponseDto(
    @property:Json(name = "verses") val verses: List<VerseDto> = emptyList(),
    @property:Json(name = "pagination") val pagination: PaginationDto? = null
)

data class VerseDto(
    @property:Json(name = "id") val id: Int,
    @property:Json(name = "verse_number") val verseNumber: Int? = null,
    @property:Json(name = "verse_key") val verseKey: String,
    @property:Json(name = "text_uthmani") val textUthmani: String? = null,
    @property:Json(name = "translations") val translations: List<VerseTranslationDto>? = null
)

data class VerseTranslationDto(
    @property:Json(name = "id") val id: Int? = null,
    @property:Json(name = "resource_id") val resourceId: Int? = null,
    @property:Json(name = "text") val text: String? = null
)

data class PaginationDto(
    @property:Json(name = "per_page") val perPage: Int = 50,
    @property:Json(name = "current_page") val currentPage: Int = 1,
    @property:Json(name = "next_page") val nextPage: Int? = null,
    @property:Json(name = "total_pages") val totalPages: Int = 1,
    @property:Json(name = "total_records") val totalRecords: Int = 0
)

data class TranslationsResponseDto(
    @property:Json(name = "translations") val translations: List<TranslationResourceDto> = emptyList()
)

data class TranslationResourceDto(
    @property:Json(name = "id") val id: Int,
    @property:Json(name = "name") val name: String,
    @property:Json(name = "author_name") val authorName: String? = null,
    @property:Json(name = "language_name") val languageName: String? = null
)

interface QuranApiService {
    @GET("chapters")
    suspend fun getChapters(
        @Query("language") language: String = "en"
    ): ChaptersResponseDto

    @GET("resources/chapter_reciters")
    suspend fun getChapterReciters(
        @Query("language") language: String = "en"
    ): ChapterRecitersResponseDto

    @GET("chapter_recitations/{reciterId}/{chapterNumber}")
    suspend fun getChapterRecitation(
        @Path("reciterId") reciterId: Int,
        @Path("chapterNumber") chapterNumber: Int,
        @Query("segments") segments: Boolean = true
    ): ChapterRecitationResponseDto

    @GET("verses/by_chapter/{chapterNumber}")
    suspend fun getVersesByChapter(
        @Path("chapterNumber") chapterNumber: Int,
        @Query("translations") translationId: Int,
        @Query("fields") fields: String = "text_uthmani",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): VersesResponseDto

    @GET("resources/translations")
    suspend fun getTranslations(
        @Query("language") language: String = "en"
    ): TranslationsResponseDto

    companion object {
        private const val BASE_URL = "https://api.quran.com/api/v4/"

        fun create(): QuranApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(QuranApiService::class.java)
        }
    }
}
