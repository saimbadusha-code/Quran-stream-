# Asset Attribution & Licensing Register — Tilawah

This register documents the origin, licence terms, attribution requirements, and caching/redistribution rules for every Quran audio provider, spoken supplication/Ruqya source, environmental background soundscape, font, and artwork asset used in **Tilawah**.

---

## 1. Quran Recitation & Verse Audio Sources

| Provider ID | Source Name & Documentation | Supported URL Patterns | Licence & Terms Summary | Attribution Displayed in UI | Offline Caching / Download Policy |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `AL_QURAN_CLOUD` | **Al Quran Cloud / Islamic Network CDN** (`https://alquran.cloud/cdn`, `https://api.alquran.cloud/v1/edition/format/audio`, `https://cdn.islamic.network/quran/info/by-surah/info.json`) | Surah: `https://cdn.islamic.network/quran/audio-surah/{bitrate}/{edition}/{surah}.mp3`<br>Verse: `https://cdn.islamic.network/quran/audio/{bitrate}/{edition}/{globalAyah}.mp3` | Open public Quran API & CDN operated by Islamic Network for personal/educational Quran streaming. Individual reciter audio copyrights remain with their respective studios/reciters (not public domain). | `"Al Quran Cloud / Islamic Network CDN"` + Edition identifier & bitrate | **Personal Offline Caching Permitted (`PERSONAL_OFFLINE_PERMITTED`)**: Single-surah personal offline storage is allowed in app-private storage with full attribution preserved; standalone commercial redistribution is prohibited. |
| `EVERY_AYAH` | **EveryAyah Verse-by-Verse Project** (`https://www.everyayah.com/recitations_pages.html`, `https://everyayah.com/data/`) | Ayah: `https://everyayah.com/data/{ReciterFolder}/{SSS}{AAA}.mp3` | Publicly accessible Quranic verse audio directory. Recording licences vary by studio/reciter; commercial redistribution rights are unconfirmed (`permissionConfirmed = false`). | `"EveryAyah.com"` + Verified folder & bitrate (`Alafasy_128kbps`, `Husary_128kbps`, `Abdul_Basit_Murattal_192kbps`, `Minshawy_Murattal_128kbps`, `Abdurrahmaan_As-Sudais_192kbps`, `Ghamadi_40kbps`, `Yasser_Ad-Dussary_128kbps`) | **Stream Only (`STREAM_ONLY`)**: Offline downloads are disabled because uniform redistribution rights across directories are not confirmed. |
| `QURAN_FOUNDATION_API` | **Quran.com / Quran Foundation API v4 & QuranicAudio QDC** (`https://api-docs.quran.com/docs/api-reference/`, `https://api.quran.com/api/v4/`) | API-returned HTTPS `audio_url` + `download.quranicaudio.com/qdc/{slug}/{surah}.mp3` | Public API v4 & QuranicAudio streaming access; OAuth2 credentials supported via server-side proxy (`QURAN_FOUNDATION_PROXY_URL`) so secrets never enter client bundles. | `"Quran.com API v4 / QuranicAudio"` + Reciter ID & translation author attribution | **Stream Only (`STREAM_ONLY`)**: Streamed directly over HTTPS; bulk/offline redistribution disabled unless authenticated enterprise agreement is configured. |

---

## 2. Spoken Adhkar, Duas & Ruqya Sessions

To uphold strict religious content authenticity and licensing compliance:
- **Zero fabricated recordings or synthetic voices** are used.
- Spoken Ruqya, Morning/Evening Adhkar passages, and Quranic Supplications (`Rabbana` & `Rabbi` duas) reference **verified canonical Surah and Ayah recordings** from `Al Quran Cloud` (`ar.alafasy`, `ar.husary`) and `EveryAyah` (`Alafasy_128kbps`, `Husary_128kbps`, `Abdul_Basit_Murattal_192kbps`).
- **Channel Separation**: `SPOKEN_RUQYA` and `SPOKEN_SUPPLICATION_ADHKAR` tracks run on the primary voice channel (replacing any active Quran chapter stream only upon explicit user tap) and **never** play simultaneously over Quran recitation.

---

## 3. Environmental Background Soundscapes

### Third-Party Sound Library Evaluation
- **Mixkit** (`https://mixkit.co/license/modal/sfxFree/`): Prohibits redistributing sound files as standalone downloadable assets or stock sound libraries; direct hotlinking from third-party pages is unreliable.
- **Pixabay** (`https://pixabay.com/service/license-summary/`): Prohibits standalone redistribution of unaltered audio files; requires individual verification per uploader.
- **Freesound** (`https://freesound.org/help/faq/`): Mixed per-file licences (`CC0`, `CC BY`, `CC BY-NC`); `CC BY-NC` is incompatible with commercial/Plus tiers.

### Selected Production Implementation (100% First-Party Verified Rights)
Following Asset-Selection Rule #4 (*replace uncertain third-party hotlinks with a properly licensed, reliable delivery method*), all environmental soundscapes in **Tilawah** are generated locally on-device in real time using Tilawah's **First-Party Procedural Acoustic DSP Engine (`AmbientSoundEngine`)**:

| Sound ID | Title | Category | Creator / Origin | Licence | Local Caching / Redistribution |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `rain` | Gentle Rain | Rain | Tilawah First-Party DSP | Proprietary / App-Owned (100% Verified) | Synthesized in-memory (`NOT_APPLICABLE`) |
| `rain_window` | Light Rainfall on Windows | Rain | Tilawah First-Party DSP | Proprietary / App-Owned (100% Verified) | Synthesized in-memory (`NOT_APPLICABLE`) |
| `stream` | Flowing Courtyard Water | Water | Tilawah First-Party DSP | Proprietary / App-Owned (100% Verified) | Synthesized in-memory (`NOT_APPLICABLE`) |
| `ocean` | Distant Ocean Waves | Water | Tilawah First-Party DSP | Proprietary / App-Owned (100% Verified) | Synthesized in-memory (`NOT_APPLICABLE`) |
| `wind` | Gentle Desert Breeze | Wind | Tilawah First-Party DSP | Proprietary / App-Owned (100% Verified) | Synthesized in-memory (`NOT_APPLICABLE`) |
| `forest` | Quiet Woodland Canopy | Forest | Tilawah First-Party DSP | Proprietary / App-Owned (100% Verified) | Synthesized in-memory (`NOT_APPLICABLE`) |
| `night` | Still Night Ambience | Night Ambience | Tilawah First-Party DSP | Proprietary / App-Owned (100% Verified) | Synthesized in-memory (`NOT_APPLICABLE`) |

---

## 4. Typography & Visual Artwork

| Asset | Source | Licence | Permitted Use |
| :--- | :--- | :--- | :--- |
| `inter.ttf` | Google Fonts (`Inter`) | SIL Open Font License 1.1 (OFL) | Bundled in `res/font/` |
| `amiri.ttf` | Google Fonts (`Amiri`) | SIL Open Font License 1.1 (OFL) | Bundled in `res/font/` for Uthmani Arabic |
| `noto_naskh_arabic.ttf` | Google Fonts (`Noto Naskh Arabic`) | SIL Open Font License 1.1 (OFL) | Bundled in `res/font/` |
| Collection Cover Art & Launcher Emblem | Original generated project assets (`res/drawable/`, `res/mipmap-*/`) | Original First-Party Project Assets | Bundled in APK; zero third-party portraits or copyrighted logos used |
