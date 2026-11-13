package com.example.data.remote

import com.example.R
import com.example.data.model.AmbientSoundscape
import com.example.data.model.AudioCategory
import com.example.data.model.AudioSourceProvider
import com.example.data.model.AudioTrack
import com.example.data.model.CollectionCategory
import com.example.data.model.CuratedCollection
import com.example.data.model.QuranVerse
import com.example.data.model.Reciter
import com.example.data.model.SoundCategory
import com.example.data.model.Surah
import com.example.data.model.TrackAvailability
import com.example.data.model.TranslationEdition
import com.example.data.model.defaultQuranStreamLicence

object AuthenticatedSeedCatalog {

    val ambientSoundscapes: List<AmbientSoundscape> = listOf(
        AmbientSoundscape(
            id = "none",
            nameKey = "off",
            defaultName = "Off (Pure Recitation)",
            subtitle = "No environmental layer active",
            category = SoundCategory.ALL,
            artworkResId = R.drawable.art_bg_painted_dream
        ),
        AmbientSoundscape(
            id = "painted_dream",
            nameKey = "painted_dream",
            defaultName = "Painted Dream",
            subtitle = "Serene valley air and soft harmonic stillness synthesized gently in real time",
            category = SoundCategory.AMBIENT,
            artworkResId = R.drawable.art_bg_painted_dream
        ),
        AmbientSoundscape(
            id = "rain",
            nameKey = "rain",
            defaultName = "Gentle Rain",
            subtitle = "Steady acoustic rainfall synthesized locally in real time",
            category = SoundCategory.RAIN,
            artworkResId = R.drawable.img_col_sleep_1793262966542
        ),
        AmbientSoundscape(
            id = "rain_window",
            nameKey = "rain_window",
            defaultName = "Light Rainfall on Windows",
            subtitle = "Soft pattering rain droplets on glass for quiet evening reading",
            category = SoundCategory.RAIN,
            artworkResId = R.drawable.art_col_emotional
        ),
        AmbientSoundscape(
            id = "stream",
            nameKey = "stream",
            defaultName = "Flowing Courtyard Water",
            subtitle = "Calm modulated courtyard water movement",
            category = SoundCategory.WATER,
            artworkResId = R.drawable.img_col_focus_1793262964938
        ),
        AmbientSoundscape(
            id = "ocean",
            nameKey = "ocean",
            defaultName = "Distant Ocean Waves",
            subtitle = "Slow coastal surf cadence for reflective listening",
            category = SoundCategory.WATER,
            artworkResId = R.drawable.img_col_beautiful_1793262967630
        ),
        AmbientSoundscape(
            id = "wind",
            nameKey = "wind",
            defaultName = "Gentle Desert Breeze",
            subtitle = "Low-frequency filtered evening wind without harshness",
            category = SoundCategory.WIND,
            artworkResId = R.drawable.img_col_duaa_1793262968390
        ),
        AmbientSoundscape(
            id = "forest",
            nameKey = "forest",
            defaultName = "Quiet Woodland Canopy",
            subtitle = "Subtle rustling foliage and warm natural air",
            category = SoundCategory.FOREST,
            artworkResId = R.drawable.art_bg_painted_dream
        ),
        AmbientSoundscape(
            id = "night",
            nameKey = "night",
            defaultName = "Still Night Ambience",
            subtitle = "Deep unobtrusive nocturnal stillness",
            category = SoundCategory.NIGHT,
            artworkResId = R.drawable.img_col_sleep_1793262966542
        )
    )

    val defaultTranslations: List<TranslationEdition> = listOf(
        TranslationEdition(
            id = 20,
            name = "Saheeh International",
            authorName = "Saheeh International",
            languageCode = "en",
            languageName = "English"
        ),
        TranslationEdition(
            id = 131,
            name = "The Clear Quran",
            authorName = "Dr. Mustafa Khattab",
            languageCode = "en",
            languageName = "English"
        ),
        TranslationEdition(
            id = 85,
            name = "Abdel Haleem",
            authorName = "M.A.S. Abdel Haleem",
            languageCode = "en",
            languageName = "English"
        ),
        TranslationEdition(
            id = 31,
            name = "French Translation",
            authorName = "Muhammad Hamidullah",
            languageCode = "fr",
            languageName = "French"
        ),
        TranslationEdition(
            id = 83,
            name = "Spanish Translation",
            authorName = "Sheikh Isa Garcia",
            languageCode = "es",
            languageName = "Spanish"
        ),
        TranslationEdition(
            id = 27,
            name = "German Translation",
            authorName = "Frank Bubenheim and Nadeem Elyas",
            languageCode = "de",
            languageName = "German"
        ),
        TranslationEdition(
            id = 33,
            name = "Indonesian Ministry of Religious Affairs",
            authorName = "Kemenag RI",
            languageCode = "id",
            languageName = "Indonesian"
        ),
        TranslationEdition(
            id = 45,
            name = "Russian Translation",
            authorName = "Elmir Kuliev",
            languageCode = "ru",
            languageName = "Russian"
        )
    )

    val reciters: List<Reciter> = listOf(
        Reciter(
            id = "yasser_dosari",
            chapterReciterId = null,
            displayName = "Yasser Al-Dosari",
            arabicName = "ياسر الدوسري",
            country = "Saudi Arabia",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = false,
            isFeaturedHero = true,
            neutralDescription = "Saudi reciter recognized for contemplative Haramain chapter and verse recitations in Hafs 'an 'Asim.",
            sourceProvenance = "QuranicAudio (yasser_ad-dussary) • EveryAyah (Yasser_Ad-Dussary_128kbps)",
            avatarAccentHex = 0xFF284234,
            alQuranCloudEdition = null,
            alQuranCloudBitrate = null,
            everyAyahFolder = "Yasser_Ad-Dussary_128kbps",
            everyAyahBitrate = 128
        ),
        Reciter(
            id = "mishary_alafasy",
            chapterReciterId = 7,
            displayName = "Mishary Rashid Alafasy",
            arabicName = "مشاري راشد العفاسي",
            country = "Kuwait",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = false,
            isFeaturedHero = false,
            neutralDescription = "Kuwaiti Qari with complete studio Murattal surah and verse recordings across Al Quran Cloud and EveryAyah.",
            sourceProvenance = "Al Quran Cloud (ar.alafasy 128kbps) • EveryAyah • Quran.com #7",
            avatarAccentHex = 0xFF243844,
            alQuranCloudEdition = "ar.alafasy",
            alQuranCloudBitrate = 128,
            everyAyahFolder = "Alafasy_128kbps",
            everyAyahBitrate = 128
        ),
        Reciter(
            id = "mahmoud_husary",
            chapterReciterId = 6,
            displayName = "Mahmoud Khalil Al-Husary",
            arabicName = "محمود خليل الحصري",
            country = "Egypt",
            recitationStyle = "Murattal (Tajwid Reference)",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = true,
            isFeaturedHero = false,
            neutralDescription = "Classical Egyptian Qari widely studied for clear articulation and measured pacing.",
            sourceProvenance = "Al Quran Cloud (ar.husary 128kbps) • EveryAyah • Quran.com #6",
            avatarAccentHex = 0xFF3E3524,
            alQuranCloudEdition = "ar.husary",
            alQuranCloudBitrate = 128,
            everyAyahFolder = "Husary_128kbps",
            everyAyahBitrate = 128
        ),
        Reciter(
            id = "siddiq_minshawi",
            chapterReciterId = 9,
            displayName = "Muhammad Siddiq Al-Minshawi",
            arabicName = "محمد صديق المنشاوي",
            country = "Egypt",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = true,
            isFeaturedHero = false,
            neutralDescription = "Renowned Egyptian reciter noted for balanced cadence and classical Tajwid clarity.",
            sourceProvenance = "Al Quran Cloud (ar.minshawi 128kbps) • EveryAyah • Quran.com #9",
            avatarAccentHex = 0xFF382C3E,
            alQuranCloudEdition = "ar.minshawi",
            alQuranCloudBitrate = 128,
            everyAyahFolder = "Minshawy_Murattal_128kbps",
            everyAyahBitrate = 128
        ),
        Reciter(
            id = "abdul_basit",
            chapterReciterId = 2,
            displayName = "Abdul Basit Abdul Samad",
            arabicName = "عبد الباسط عبد الصمد",
            country = "Egypt",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = true,
            isFeaturedHero = false,
            neutralDescription = "Historic Egyptian Qari celebrated across the Muslim world for resonant Murattal recordings.",
            sourceProvenance = "Al Quran Cloud (ar.abdulbasitmurattal) • EveryAyah • Quran.com #2",
            avatarAccentHex = 0xFF2D3A2E,
            alQuranCloudEdition = "ar.abdulbasitmurattal",
            alQuranCloudBitrate = 192,
            everyAyahFolder = "Abdul_Basit_Murattal_192kbps",
            everyAyahBitrate = 192
        ),
        Reciter(
            id = "abdulrahman_sudais",
            chapterReciterId = 3,
            displayName = "Abdulrahman Al-Sudais",
            arabicName = "عبد الرحمن السديس",
            country = "Saudi Arabia",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = false,
            isFeaturedHero = false,
            neutralDescription = "Haramain reciter with brisk, clear Murattal chapter and verse recordings.",
            sourceProvenance = "Al Quran Cloud (ar.abdurrahmaansudais) • EveryAyah • Quran.com #3",
            avatarAccentHex = 0xFF2A3642,
            alQuranCloudEdition = "ar.abdurrahmaansudais",
            alQuranCloudBitrate = 192,
            everyAyahFolder = "Abdurrahmaan_As-Sudais_192kbps",
            everyAyahBitrate = 192
        ),
        Reciter(
            id = "saad_al_ghamdi",
            chapterReciterId = null,
            displayName = "Saad Al-Ghamdi",
            arabicName = "سعد الغامدي",
            country = "Saudi Arabia",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = false,
            isFeaturedHero = false,
            neutralDescription = "Saudi Qari with widely recognized Murattal verse-by-verse and chapter recordings.",
            sourceProvenance = "QuranicAudio (sa3d_al-ghaamidi/complete) • EveryAyah (Ghamadi_40kbps)",
            avatarAccentHex = 0xFF2E3B33,
            alQuranCloudEdition = null,
            alQuranCloudBitrate = null,
            everyAyahFolder = "Ghamadi_40kbps",
            everyAyahBitrate = 40
        ),
        Reciter(
            id = "abu_bakr_shatri",
            chapterReciterId = 4,
            displayName = "Abu Bakr Al-Shatri",
            arabicName = "أبو بكر الشاطري",
            country = "Saudi Arabia",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = false,
            isFeaturedHero = false,
            neutralDescription = "Calm, steady Murattal recitations suited for evening reflection and study.",
            sourceProvenance = "Al Quran Cloud (ar.shaatree 128kbps) • EveryAyah • Quran.com #4",
            avatarAccentHex = 0xFF3A2E2B,
            alQuranCloudEdition = "ar.shaatree",
            alQuranCloudBitrate = 128,
            everyAyahFolder = "Abu_Bakr_Ash-Shaatree_128kbps",
            everyAyahBitrate = 128
        ),
        Reciter(
            id = "maher_muaiqly",
            chapterReciterId = null,
            displayName = "Maher Al-Muaiqly",
            arabicName = "ماهر المعيقلي",
            country = "Saudi Arabia",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = false,
            isFeaturedHero = false,
            neutralDescription = "Measured, serene Murattal recitations in Hafs 'an 'Asim.",
            sourceProvenance = "Al Quran Cloud (ar.mahermuaiqly 128kbps) • EveryAyah",
            avatarAccentHex = 0xFF263B38,
            alQuranCloudEdition = "ar.mahermuaiqly",
            alQuranCloudBitrate = 128,
            everyAyahFolder = "MaherAlMuaiqly128kbps",
            everyAyahBitrate = 128
        ),
        Reciter(
            id = "muhammad_luhaidan",
            chapterReciterId = null,
            displayName = "Muhammad Al-Luhaidan",
            arabicName = "محمد اللحيدان",
            country = "Saudi Arabia",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = false,
            isFeaturedHero = false,
            neutralDescription = "Demo catalog entry featuring selected contemplative surah metadata when external recordings are unconfigured.",
            sourceProvenance = "Demo Metadata Catalog",
            avatarAccentHex = 0xFF332D3B
        ),
        Reciter(
            id = "abdul_rahman_mossad",
            chapterReciterId = null,
            displayName = "Abdul Rahman Mossad",
            arabicName = "عبد الرحمن مسعد",
            country = "Egypt",
            recitationStyle = "Murattal",
            riwayah = "Hafs 'an 'Asim",
            isTajwidFeatured = false,
            isFeaturedHero = false,
            neutralDescription = "Demo catalog entry highlighting calm recitation metadata when external recordings are unconfigured.",
            sourceProvenance = "Demo Metadata Catalog",
            avatarAccentHex = 0xFF27352F
        )
    )

    // Separate Spoken / Ruqya / Morning & Evening Adhkar & Verified Quranic Supplication Sessions.
    // Uses ONLY authentic verified recordings from Al Quran Cloud & EveryAyah; never auto-mixed over Quran recitation.
    val spokenIslamicSessions: List<AudioTrack> = listOf(
        AudioTrack(
            id = "voice_ruqya_fatihah",
            surahNumber = 1,
            surahName = "Ruqya Opening — Surah Al-Fatihah",
            surahArabicName = "الفاتحة — رقية شرعية",
            surahMeaning = "Complete Opening Chapter for Ruqya",
            verseCount = 7,
            reciterId = "mishary_alafasy",
            reciterName = "Mishary Rashid Alafasy",
            chapterReciterId = 7,
            audioUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/1.mp3",
            durationSec = null,
            sourceMetadata = "Al Quran Cloud (ar.alafasy 128kbps)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_RUQYA,
            sourceProvider = AudioSourceProvider.AL_QURAN_CLOUD,
            bitrateKbps = 128,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.AL_QURAN_CLOUD, "Mishary Rashid Alafasy")
        ),
        AudioTrack(
            id = "voice_ruqya_kursi",
            surahNumber = 2,
            surahName = "Ayat Al-Kursi (2:255)",
            surahArabicName = "آية الكرسي",
            surahMeaning = "Morning, Evening & Ruqya Protection Ayah",
            verseCount = 1,
            reciterId = "mishary_alafasy",
            reciterName = "Mishary Rashid Alafasy",
            chapterReciterId = 7,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            durationSec = null,
            sourceMetadata = "EveryAyah (Alafasy_128kbps/002255.mp3)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_RUQYA,
            specificVerseNumber = 255,
            sourceProvider = AudioSourceProvider.EVERY_AYAH,
            bitrateKbps = 128,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.EVERY_AYAH, "Mishary Rashid Alafasy")
        ),
        AudioTrack(
            id = "voice_ruqya_baqarah_285",
            surahNumber = 2,
            surahName = "Al-Baqarah Closing Ayah (2:285)",
            surahArabicName = "خواتيم سورة البقرة ٢٨٥",
            surahMeaning = "Evening Remembrance & Ruqya",
            verseCount = 1,
            reciterId = "abdul_basit",
            reciterName = "Abdul Basit Abdul Samad",
            chapterReciterId = 2,
            audioUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/002285.mp3",
            durationSec = null,
            sourceMetadata = "EveryAyah (Abdul_Basit_Murattal_192kbps/002285.mp3)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_RUQYA,
            specificVerseNumber = 285,
            sourceProvider = AudioSourceProvider.EVERY_AYAH,
            bitrateKbps = 192,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.EVERY_AYAH, "Abdul Basit Abdul Samad")
        ),
        AudioTrack(
            id = "voice_ruqya_baqarah_286",
            surahNumber = 2,
            surahName = "Al-Baqarah Closing Supplication (2:286)",
            surahArabicName = "لا يكلف الله نفساً إلا وسعها",
            surahMeaning = "Evening Supplication & Relief",
            verseCount = 1,
            reciterId = "abdul_basit",
            reciterName = "Abdul Basit Abdul Samad",
            chapterReciterId = 2,
            audioUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/002286.mp3",
            durationSec = null,
            sourceMetadata = "EveryAyah (Abdul_Basit_Murattal_192kbps/002286.mp3)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_RUQYA,
            specificVerseNumber = 286,
            sourceProvider = AudioSourceProvider.EVERY_AYAH,
            bitrateKbps = 192,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.EVERY_AYAH, "Abdul Basit Abdul Samad")
        ),
        AudioTrack(
            id = "voice_adhkar_ikhlas",
            surahNumber = 112,
            surahName = "Morning & Evening Adhkar — Al-Ikhlas",
            surahArabicName = "سورة الإخلاص — أذكار الصباح والمساء",
            surahMeaning = "Al-Mu'awwidhat (1 of 3)",
            verseCount = 4,
            reciterId = "mahmoud_husary",
            reciterName = "Mahmoud Khalil Al-Husary",
            chapterReciterId = 6,
            audioUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.husary/112.mp3",
            durationSec = null,
            sourceMetadata = "Al Quran Cloud (ar.husary 128kbps)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_SUPPLICATION_ADHKAR,
            sourceProvider = AudioSourceProvider.AL_QURAN_CLOUD,
            bitrateKbps = 128,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.AL_QURAN_CLOUD, "Mahmoud Khalil Al-Husary")
        ),
        AudioTrack(
            id = "voice_adhkar_falaq",
            surahNumber = 113,
            surahName = "Morning & Evening Adhkar — Al-Falaq",
            surahArabicName = "سورة الفلق — أذكار الصباح والمساء",
            surahMeaning = "Al-Mu'awwidhat (2 of 3)",
            verseCount = 5,
            reciterId = "mahmoud_husary",
            reciterName = "Mahmoud Khalil Al-Husary",
            chapterReciterId = 6,
            audioUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.husary/113.mp3",
            durationSec = null,
            sourceMetadata = "Al Quran Cloud (ar.husary 128kbps)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_SUPPLICATION_ADHKAR,
            sourceProvider = AudioSourceProvider.AL_QURAN_CLOUD,
            bitrateKbps = 128,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.AL_QURAN_CLOUD, "Mahmoud Khalil Al-Husary")
        ),
        AudioTrack(
            id = "voice_adhkar_nas",
            surahNumber = 114,
            surahName = "Morning & Evening Adhkar — An-Nas",
            surahArabicName = "سورة الناس — أذكار الصباح والمساء",
            surahMeaning = "Al-Mu'awwidhat (3 of 3)",
            verseCount = 6,
            reciterId = "mahmoud_husary",
            reciterName = "Mahmoud Khalil Al-Husary",
            chapterReciterId = 6,
            audioUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.husary/114.mp3",
            durationSec = null,
            sourceMetadata = "Al Quran Cloud (ar.husary 128kbps)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_SUPPLICATION_ADHKAR,
            sourceProvider = AudioSourceProvider.AL_QURAN_CLOUD,
            bitrateKbps = 128,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.AL_QURAN_CLOUD, "Mahmoud Khalil Al-Husary")
        ),
        AudioTrack(
            id = "voice_dua_rabbana_atina",
            surahNumber = 2,
            surahName = "Quranic Dua — Rabbana Atina (2:201)",
            surahArabicName = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً",
            surahMeaning = "Verified Supplication for Good in Both Worlds",
            verseCount = 1,
            reciterId = "mahmoud_husary",
            reciterName = "Mahmoud Khalil Al-Husary",
            chapterReciterId = 6,
            audioUrl = "https://everyayah.com/data/Husary_128kbps/002201.mp3",
            durationSec = null,
            sourceMetadata = "EveryAyah (Husary_128kbps/002201.mp3)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_SUPPLICATION_ADHKAR,
            specificVerseNumber = 201,
            sourceProvider = AudioSourceProvider.EVERY_AYAH,
            bitrateKbps = 128,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.EVERY_AYAH, "Mahmoud Khalil Al-Husary")
        ),
        AudioTrack(
            id = "voice_dua_la_tuzigh",
            surahNumber = 3,
            surahName = "Quranic Dua — Steadfast Hearts (3:8)",
            surahArabicName = "رَبَّنَا لَا تُزِغْ قُلُوبَنَا",
            surahMeaning = "Verified Supplication for Guidance & Mercy",
            verseCount = 1,
            reciterId = "mishary_alafasy",
            reciterName = "Mishary Rashid Alafasy",
            chapterReciterId = 7,
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/003008.mp3",
            durationSec = null,
            sourceMetadata = "EveryAyah (Alafasy_128kbps/003008.mp3)",
            availabilityStatus = TrackAvailability.VERIFIED_STREAM,
            audioCategory = AudioCategory.SPOKEN_SUPPLICATION_ADHKAR,
            specificVerseNumber = 8,
            sourceProvider = AudioSourceProvider.EVERY_AYAH,
            bitrateKbps = 128,
            licenceMetadata = defaultQuranStreamLicence(AudioSourceProvider.EVERY_AYAH, "Mishary Rashid Alafasy")
        )
    )

    val curatedCollections: List<CuratedCollection> = listOf(
        CuratedCollection(
            id = "col_focus_work",
            title = "Focus & Work",
            description = "Steady, measured Murattal recitations curated for deep study, reading, and calm concentration.",
            category = CollectionCategory.FOCUS_WORK,
            coverResId = R.drawable.img_col_focus_1793262964938,
            trackIds = listOf(
                "mahmoud_husary_1",
                "mishary_alafasy_18",
                "abu_bakr_shatri_36",
                "siddiq_minshawi_55",
                "mahmoud_husary_103"
            ),
            provenance = "Tilawah Editorial"
        ),
        CuratedCollection(
            id = "col_sleep_mode",
            title = "Sleep Mode",
            description = "Unhurried, tranquil evening surahs to wind down the heart and mind before rest.",
            category = CollectionCategory.SLEEP,
            coverResId = R.drawable.img_col_sleep_1793262966542,
            trackIds = listOf(
                "abu_bakr_shatri_67",
                "mishary_alafasy_56",
                "siddiq_minshawi_112",
                "siddiq_minshawi_113",
                "siddiq_minshawi_114"
            ),
            provenance = "Tilawah Editorial"
        ),
        CuratedCollection(
            id = "col_duaa_ruqia",
            title = "Duaa & Ruqia",
            description = "Foundational surahs traditionally recited for protection, remembrance, and serenity.",
            category = CollectionCategory.DUAA_RUQIA,
            coverResId = R.drawable.img_col_duaa_1793262968390,
            trackIds = listOf(
                "mishary_alafasy_1",
                "mishary_alafasy_2",
                "mishary_alafasy_112",
                "mishary_alafasy_113",
                "mishary_alafasy_114"
            ),
            provenance = "Tilawah Editorial"
        ),
        CuratedCollection(
            id = "col_emotional",
            title = "Emotional Recitations",
            description = "Heart-softening recitations of Surah Maryam, Ya-Sin, Ar-Rahman, and Al-Mulk.",
            category = CollectionCategory.EMOTIONAL,
            coverResId = R.drawable.art_col_emotional,
            trackIds = listOf(
                "siddiq_minshawi_19",
                "abdul_basit_55",
                "abu_bakr_shatri_78",
                "mishary_alafasy_67"
            ),
            provenance = "Tilawah Editorial"
        ),
        CuratedCollection(
            id = "col_most_beautiful",
            title = "Most Beautiful Recitations",
            description = "Timeless chapter recitations across classical and contemporary Qaris.",
            category = CollectionCategory.EMOTIONAL,
            coverResId = R.drawable.img_col_beautiful_1793262967630,
            trackIds = listOf(
                "mishary_alafasy_1",
                "mishary_alafasy_55",
                "abdul_basit_19",
                "siddiq_minshawi_36",
                "yasser_dosari_1",
                "abdulrahman_sudais_67"
            ),
            provenance = "Tilawah Editorial"
        ),
        CuratedCollection(
            id = "col_reciter_dosari",
            title = "Yasser Al-Dosari Selections",
            description = "Opening chapters and reflective surahs associated with Yasser Al-Dosari.",
            category = CollectionCategory.RECITER,
            coverResId = R.drawable.img_col_beautiful_1793262967630,
            trackIds = listOf(
                "yasser_dosari_1",
                "yasser_dosari_2",
                "yasser_dosari_3",
                "yasser_dosari_36",
                "yasser_dosari_67"
            ),
            provenance = "Reciter Spotlight",
            associatedReciterId = "yasser_dosari"
        ),
        CuratedCollection(
            id = "col_reciter_luhaidan",
            title = "Muhammad Al-Luhaidan Focus",
            description = "Selected surah entries curated around Muhammad Al-Luhaidan's contemplative style.",
            category = CollectionCategory.RECITER,
            coverResId = R.drawable.img_col_focus_1793262964938,
            trackIds = listOf(
                "muhammad_luhaidan_1",
                "muhammad_luhaidan_18",
                "muhammad_luhaidan_50"
            ),
            provenance = "Reciter Spotlight",
            associatedReciterId = "muhammad_luhaidan"
        ),
        CuratedCollection(
            id = "col_reciter_mossad",
            title = "Abdul Rahman Mossad Evening",
            description = "Gentle evening selections associated with Abdul Rahman Mossad.",
            category = CollectionCategory.RECITER,
            coverResId = R.drawable.img_col_sleep_1793262966542,
            trackIds = listOf(
                "abdul_rahman_mossad_1",
                "abdul_rahman_mossad_55",
                "abdul_rahman_mossad_67"
            ),
            provenance = "Reciter Spotlight",
            associatedReciterId = "abdul_rahman_mossad"
        ),
        CuratedCollection(
            id = "col_tajwid_masters",
            title = "Classical Tajwid Reference",
            description = "Editorial selection featuring Al-Husary, Al-Minshawi, and Abdul Basit for attentive listening.",
            category = CollectionCategory.FOCUS_WORK,
            coverResId = R.drawable.art_col_tajwid,
            trackIds = listOf(
                "mahmoud_husary_1",
                "siddiq_minshawi_1",
                "abdul_basit_1",
                "mahmoud_husary_112",
                "siddiq_minshawi_108",
                "abdul_basit_103"
            ),
            provenance = "Editorial Discovery"
        )
    )

    // Verified QuranicAudio QDC slugs (unpadded 1..114.mp3)
    val verifiedCdnPaths: Map<String, String> = mapOf(
        "mishary_alafasy" to "mishari_al_afasy/murattal",
        "mahmoud_husary" to "mahmood_khaleel_al-husaree/murattal",
        "siddiq_minshawi" to "muhammad_siddeeq_al-minshaawee/murattal",
        "abdul_basit" to "abdul_baset/murattal",
        "abdulrahman_sudais" to "abdurrahmaan_as-sudais/murattal",
        "abu_bakr_shatri" to "abu_bakr_shatri/murattal"
    )

    // Verified QuranicAudio Qari directory paths (3-digit zero-padded 001..114.mp3 from quranicaudio.com/api/qaris)
    val verifiedQuranicAudioPaddedPaths: Map<String, String> = mapOf(
        "yasser_dosari" to "yasser_ad-dussary",
        "saad_al_ghamdi" to "sa3d_al-ghaamidi/complete",
        "maher_muaiqly" to "maher_256"
    )

    fun hasVerifiedQuranicAudioSurahStream(reciterId: String): Boolean =
        reciterId in verifiedCdnPaths || reciterId in verifiedQuranicAudioPaddedPaths

    fun buildQuranicAudioDirectUrl(reciterId: String, surahNumber: Int): String? {
        if (surahNumber !in 1..114) return null
        verifiedCdnPaths[reciterId]?.let { qdcSlug ->
            return "https://download.quranicaudio.com/qdc/$qdcSlug/$surahNumber.mp3"
        }
        verifiedQuranicAudioPaddedPaths[reciterId]?.let { qariFolder ->
            val paddedSurah = surahNumber.toString().padStart(3, '0')
            return "https://download.quranicaudio.com/quran/$qariFolder/$paddedSurah.mp3"
        }
        return null
    }

    // Canonical 114 Surahs of the Holy Quran (Number, Arabic Name, Transliteration, English Meaning, Place, Verses)
    val canonicalSurahs: List<Surah> by lazy {
        val raw = listOf(
            SurahMeta(1, "الفاتحة", "Al-Fatihah", "The Opener", "Makkah", 7),
            SurahMeta(2, "البقرة", "Al-Baqarah", "The Cow", "Madinah", 286),
            SurahMeta(3, "آل عمران", "Ali 'Imran", "Family of Imran", "Madinah", 200),
            SurahMeta(4, "النساء", "An-Nisa", "The Women", "Madinah", 176),
            SurahMeta(5, "المائدة", "Al-Ma'idah", "The Table Spread", "Madinah", 120),
            SurahMeta(6, "الأنعام", "Al-An'am", "The Cattle", "Makkah", 165),
            SurahMeta(7, "الأعراف", "Al-A'raf", "The Heights", "Makkah", 206),
            SurahMeta(8, "الأنفال", "Al-Anfal", "The Spoils of War", "Madinah", 75),
            SurahMeta(9, "التوبة", "At-Tawbah", "The Repentance", "Madinah", 129),
            SurahMeta(10, "يونس", "Yunus", "Jonah", "Makkah", 109),
            SurahMeta(11, "هود", "Hud", "Hud", "Makkah", 123),
            SurahMeta(12, "يوسف", "Yusuf", "Joseph", "Makkah", 111),
            SurahMeta(13, "الرعد", "Ar-Ra'd", "The Thunder", "Madinah", 43),
            SurahMeta(14, "إبراهيم", "Ibrahim", "Abraham", "Makkah", 52),
            SurahMeta(15, "الحجر", "Al-Hijr", "The Rocky Tract", "Makkah", 99),
            SurahMeta(16, "النحل", "An-Nahl", "The Bee", "Makkah", 128),
            SurahMeta(17, "الإسراء", "Al-Isra", "The Night Journey", "Makkah", 111),
            SurahMeta(18, "الكهف", "Al-Kahf", "The Cave", "Makkah", 110),
            SurahMeta(19, "مريم", "Maryam", "Mary", "Makkah", 98),
            SurahMeta(20, "طه", "Taha", "Ta-Ha", "Makkah", 135),
            SurahMeta(21, "الأنبياء", "Al-Anbiya", "The Prophets", "Makkah", 112),
            SurahMeta(22, "الحج", "Al-Hajj", "The Pilgrimage", "Madinah", 78),
            SurahMeta(23, "المؤمنون", "Al-Mu'minun", "The Believers", "Makkah", 118),
            SurahMeta(24, "النور", "An-Nur", "The Light", "Madinah", 64),
            SurahMeta(25, "الفرقان", "Al-Furqan", "The Criterion", "Makkah", 77),
            SurahMeta(26, "الشعراء", "Ash-Shu'ara", "The Poets", "Makkah", 227),
            SurahMeta(27, "النمل", "An-Naml", "The Ant", "Makkah", 93),
            SurahMeta(28, "القصص", "Al-Qasas", "The Stories", "Makkah", 88),
            SurahMeta(29, "العنكبوت", "Al-'Ankabut", "The Spider", "Makkah", 69),
            SurahMeta(30, "الروم", "Ar-Rum", "The Romans", "Makkah", 60),
            SurahMeta(31, "لقمان", "Luqman", "Luqman", "Makkah", 34),
            SurahMeta(32, "السجدة", "As-Sajdah", "The Prostration", "Makkah", 30),
            SurahMeta(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", "Madinah", 73),
            SurahMeta(34, "سبأ", "Saba", "Sheba", "Makkah", 54),
            SurahMeta(35, "فاطر", "Fatir", "Originator", "Makkah", 45),
            SurahMeta(36, "يس", "Ya-Sin", "Ya Sin", "Makkah", 83),
            SurahMeta(37, "الصافات", "As-Saffat", "Those Who Set The Ranks", "Makkah", 182),
            SurahMeta(38, "ص", "Sad", "The Letter Saad", "Makkah", 88),
            SurahMeta(39, "الزمر", "Az-Zumar", "The Troops", "Makkah", 75),
            SurahMeta(40, "غافر", "Ghafir", "The Forgiver", "Makkah", 85),
            SurahMeta(41, "فصلت", "Fussilat", "Explained in Detail", "Makkah", 54),
            SurahMeta(42, "الشورى", "Ash-Shuraa", "The Consultation", "Makkah", 53),
            SurahMeta(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", "Makkah", 89),
            SurahMeta(44, "الدخان", "Ad-Dukhan", "The Smoke", "Makkah", 59),
            SurahMeta(45, "الجاثية", "Al-Jathiyah", "The Crouching", "Makkah", 37),
            SurahMeta(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", "Makkah", 35),
            SurahMeta(47, "محمد", "Muhammad", "Muhammad", "Madinah", 38),
            SurahMeta(48, "الفتح", "Al-Fath", "The Victory", "Madinah", 29),
            SurahMeta(49, "الحجرات", "Al-Hujurat", "The Rooms", "Madinah", 18),
            SurahMeta(50, "ق", "Qaf", "The Letter Qaf", "Makkah", 45),
            SurahMeta(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", "Makkah", 60),
            SurahMeta(52, "الطور", "At-Tur", "The Mount", "Makkah", 49),
            SurahMeta(53, "النجم", "An-Najm", "The Star", "Makkah", 62),
            SurahMeta(54, "القمر", "Al-Qamar", "The Moon", "Makkah", 55),
            SurahMeta(55, "الرحمن", "Ar-Rahman", "The Beneficent", "Madinah", 78),
            SurahMeta(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", "Makkah", 96),
            SurahMeta(57, "الحديد", "Al-Hadid", "The Iron", "Madinah", 29),
            SurahMeta(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", "Madinah", 22),
            SurahMeta(59, "الحشر", "Al-Hashr", "The Exile", "Madinah", 24),
            SurahMeta(60, "الممتحنة", "Al-Mumtahanah", "She That Is To Be Examined", "Madinah", 13),
            SurahMeta(61, "الصف", "As-Saf", "The Ranks", "Madinah", 14),
            SurahMeta(62, "الجمعة", "Al-Jumu'ah", "The Congregation", "Madinah", 11),
            SurahMeta(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", "Madinah", 11),
            SurahMeta(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", "Madinah", 18),
            SurahMeta(65, "الطلاق", "At-Talaq", "The Divorce", "Madinah", 12),
            SurahMeta(66, "التحريم", "At-Tahrim", "The Prohibition", "Madinah", 12),
            SurahMeta(67, "الملك", "Al-Mulk", "The Sovereignty", "Makkah", 30),
            SurahMeta(68, "القلم", "Al-Qalam", "The Pen", "Makkah", 52),
            SurahMeta(69, "الحاقة", "Al-Haqqah", "The Reality", "Makkah", 52),
            SurahMeta(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", "Makkah", 44),
            SurahMeta(71, "نوح", "Nuh", "Noah", "Makkah", 28),
            SurahMeta(72, "الجن", "Al-Jinn", "The Jinn", "Makkah", 28),
            SurahMeta(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", "Makkah", 20),
            SurahMeta(74, "المدثر", "Al-Muddaththir", "The Cloaked One", "Makkah", 56),
            SurahMeta(75, "القيامة", "Al-Qiyamah", "The Resurrection", "Makkah", 40),
            SurahMeta(76, "الإنسان", "Al-Insan", "The Man", "Madinah", 31),
            SurahMeta(77, "المرسلات", "Al-Mursalat", "The Emissaries", "Makkah", 50),
            SurahMeta(78, "النبأ", "An-Naba", "The Tidings", "Makkah", 40),
            SurahMeta(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", "Makkah", 46),
            SurahMeta(80, "عبس", "'Abasa", "He Frowned", "Makkah", 42),
            SurahMeta(81, "التكوير", "At-Takwir", "The Overthrowing", "Makkah", 29),
            SurahMeta(82, "الانفطار", "Al-Infitar", "The Cleaving", "Makkah", 19),
            SurahMeta(83, "المطففين", "Al-Mutaffifin", "The Defrauding", "Makkah", 36),
            SurahMeta(84, "الانشقاق", "Al-Inshiqaq", "The Sundering", "Makkah", 25),
            SurahMeta(85, "البروج", "Al-Buruj", "The Mansions of the Stars", "Makkah", 22),
            SurahMeta(86, "الطارق", "At-Tariq", "The Nightcommer", "Makkah", 17),
            SurahMeta(87, "الأعلى", "Al-A'la", "The Most High", "Makkah", 19),
            SurahMeta(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", "Makkah", 26),
            SurahMeta(89, "الفجر", "Al-Fajr", "The Dawn", "Makkah", 30),
            SurahMeta(90, "البلد", "Al-Balad", "The City", "Makkah", 20),
            SurahMeta(91, "الشمس", "Ash-Shams", "The Sun", "Makkah", 15),
            SurahMeta(92, "الليل", "Al-Layl", "The Night", "Makkah", 21),
            SurahMeta(93, "الضحى", "Ad-Duhaa", "The Morning Hours", "Makkah", 11),
            SurahMeta(94, "الشرح", "Ash-Sharh", "The Relief", "Makkah", 8),
            SurahMeta(95, "التين", "At-Tin", "The Fig", "Makkah", 8),
            SurahMeta(96, "العلق", "Al-'Alaq", "The Clot", "Makkah", 19),
            SurahMeta(97, "القدر", "Al-Qadr", "The Power", "Makkah", 5),
            SurahMeta(98, "البينة", "Al-Bayyinah", "The Clear Proof", "Madinah", 8),
            SurahMeta(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", "Madinah", 8),
            SurahMeta(100, "العاديات", "Al-'Adiyat", "The Courser", "Makkah", 11),
            SurahMeta(101, "القارعة", "Al-Qari'ah", "The Calamity", "Makkah", 11),
            SurahMeta(102, "التكاثر", "At-Takathur", "The Rivalry in World Increase", "Makkah", 8),
            SurahMeta(103, "العصر", "Al-'Asr", "The Declining Day", "Makkah", 3),
            SurahMeta(104, "الهمزة", "Al-Humazah", "The Traducer", "Makkah", 9),
            SurahMeta(105, "الفيل", "Al-Fil", "The Elephant", "Makkah", 5),
            SurahMeta(106, "قريش", "Quraysh", "Quraysh", "Makkah", 4),
            SurahMeta(107, "الماعون", "Al-Ma'un", "The Small Kindnesses", "Makkah", 7),
            SurahMeta(108, "الكوثر", "Al-Kawthar", "The Abundance", "Makkah", 3),
            SurahMeta(109, "الكافرون", "Al-Kafirun", "The Disbelievers", "Makkah", 6),
            SurahMeta(110, "النصر", "An-Nasr", "The Divine Support", "Madinah", 3),
            SurahMeta(111, "المسد", "Al-Masad", "The Palm Fiber", "Makkah", 5),
            SurahMeta(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", "Makkah", 4),
            SurahMeta(113, "الفلق", "Al-Falaq", "The Daybreak", "Makkah", 5),
            SurahMeta(114, "الناس", "An-Nas", "Mankind", "Makkah", 6)
        )
        raw.map { m ->
            Surah(
                id = "surah_${m.num}",
                number = m.num,
                arabicName = m.arabic,
                displayName = m.translit,
                transliteration = m.translit,
                englishMeaning = m.meaning,
                revelationPlace = m.place,
                verseCount = m.verses,
                sourceIdentifier = "Tanzil / Quran.com Canonical Metadata"
            )
        }
    }

    private data class SurahMeta(
        val num: Int,
        val arabic: String,
        val translit: String,
        val meaning: String,
        val place: String,
        val verses: Int
    )

    // Verbatim canonical Uthmani scripture & Saheeh International translation for offline fallback on short chapters
    private const val SAHEEH_ATTRIBUTION = "Saheeh International (via Tanzil / Quran.com)"

    val verifiedOfflineVerses: Map<Int, List<QuranVerse>> = mapOf(
        1 to listOf(
            QuranVerse(1, "1:1", 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", SAHEEH_ATTRIBUTION),
            QuranVerse(2, "1:2", 2, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", SAHEEH_ATTRIBUTION),
            QuranVerse(3, "1:3", 3, "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", SAHEEH_ATTRIBUTION),
            QuranVerse(4, "1:4", 4, "مَـٰلِكِ يَوْمِ ٱلدِّينِ", "Sovereign of the Day of Recompense.", SAHEEH_ATTRIBUTION),
            QuranVerse(5, "1:5", 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", SAHEEH_ATTRIBUTION),
            QuranVerse(6, "1:6", 6, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", "Guide us to the straight path -", SAHEEH_ATTRIBUTION),
            QuranVerse(7, "1:7", 7, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", SAHEEH_ATTRIBUTION)
        ),
        103 to listOf(
            QuranVerse(6177, "103:1", 1, "وَٱلْعَصْرِ", "By time,", SAHEEH_ATTRIBUTION),
            QuranVerse(6178, "103:2", 2, "إِنَّ ٱلْإِنسَـٰنَ لَفِى خُسْرٍ", "Indeed, mankind is in loss,", SAHEEH_ATTRIBUTION),
            QuranVerse(6179, "103:3", 3, "إِلَّا ٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّـٰلِحَـٰتِ وَتَوَاصَوْا۟ بِٱلْحَقِّ وَتَوَاصَوْا۟ بِٱلصَّبْرِ", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", SAHEEH_ATTRIBUTION)
        ),
        108 to listOf(
            QuranVerse(6205, "108:1", 1, "إِنَّآ أَعْطَيْنَـٰكَ ٱلْكَوْثَرَ", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", SAHEEH_ATTRIBUTION),
            QuranVerse(6206, "108:2", 2, "فَصَلِّ لِرَبِّكَ وَٱنْحَرْ", "So pray to your Lord and sacrifice [to Him alone].", SAHEEH_ATTRIBUTION),
            QuranVerse(6207, "108:3", 3, "إِنَّ شَانِئَكَ هُوَ ٱلْأَبْتَرُ", "Indeed, your enemy is the one cut off.", SAHEEH_ATTRIBUTION)
        ),
        112 to listOf(
            QuranVerse(6222, "112:1", 1, "قُلْ هُوَ ٱللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", SAHEEH_ATTRIBUTION),
            QuranVerse(6223, "112:2", 2, "ٱللَّهُ ٱلصَّمَدُ", "Allah, the Eternal Refuge.", SAHEEH_ATTRIBUTION),
            QuranVerse(6224, "112:3", 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", SAHEEH_ATTRIBUTION),
            QuranVerse(6225, "112:4", 4, "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ", "Nor is there to Him any equivalent.\"", SAHEEH_ATTRIBUTION)
        ),
        113 to listOf(
            QuranVerse(6226, "113:1", 1, "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", SAHEEH_ATTRIBUTION),
            QuranVerse(6227, "113:2", 2, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", SAHEEH_ATTRIBUTION),
            QuranVerse(6228, "113:3", 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", SAHEEH_ATTRIBUTION),
            QuranVerse(6229, "113:4", 4, "وَمِن شَرِّ ٱلنَّفَّـٰثَـٰتِ فِى ٱلْعُقَدِ", "And from the evil of the blowers in knots", SAHEEH_ATTRIBUTION),
            QuranVerse(6230, "113:5", 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", SAHEEH_ATTRIBUTION)
        ),
        114 to listOf(
            QuranVerse(6231, "114:1", 1, "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", SAHEEH_ATTRIBUTION),
            QuranVerse(6232, "114:2", 2, "مَلِكِ ٱلنَّاسِ", "The Sovereign of mankind,", SAHEEH_ATTRIBUTION),
            QuranVerse(6233, "114:3", 3, "إِلَـٰهِ ٱلنَّاسِ", "The God of mankind,", SAHEEH_ATTRIBUTION),
            QuranVerse(6234, "114:4", 4, "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ", "From the evil of the retreating whisperer -", SAHEEH_ATTRIBUTION),
            QuranVerse(6235, "114:5", 5, "ٱلَّذِى يُوَسْوِسُ فِى صُدُورِ ٱلنَّاسِ", "Who whispers [evil] into the breasts of mankind -", SAHEEH_ATTRIBUTION),
            QuranVerse(6236, "114:6", 6, "مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ", "From among the jinn and mankind.\"", SAHEEH_ATTRIBUTION)
        )
    )
}
