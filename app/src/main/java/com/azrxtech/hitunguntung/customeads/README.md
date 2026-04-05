# Custom Ads System — Hitung Untung
Sistem iklan kustom mandiri berbasis Firebase Firestore.
Dibuat untuk menampilkan iklan interstitial (gambar, video, webview) tanpa memerlukan SDK iklan pihak ketiga.

## 🏗️ Arsitektur

```
customeads/
├── model/
│   ├── AdConfig.kt       # Representasi konfigurasi dari ad_settings/hitunguntung_config
│   └── AdCampaign.kt     # Representasi data kampanye dari collection campaigns
├── repository/
│   └── AdRepository.kt   # Akses data Firestore (fetch config & campaigns)
├── manager/
│   └── AdManager.kt      # Singleton: logic kapan iklan tampil (trigger, timer, klik)
├── view/
│   ├── InterstitialAdScreen.kt  # Layar overlay iklan utama (Dialog full-screen)
│   ├── AdImageComponent.kt      # Komponen gambar (Coil SubcomposeAsyncImage)
│   ├── AdVideoComponent.kt      # Komponen video (placeholder, siap ExoPlayer)
│   └── AdWebViewComponent.kt    # Komponen webview interaktif
├── info.txt              # Catatan teknis asli
└── README.md             # ← File ini
```

## 📋 Struktur Firestore

### Collection: `ad_settings` → Document: `hitunguntung_config`

| Field                   | Tipe    | Deskripsi                                                               |
| ----------------------- | ------- | ----------------------------------------------------------------------- |
| `is_ads_enabled`        | boolean | Master switch — false matikan semua iklan                               |
| `trigger_strategy`      | string  | `"first_open_only"`, `"after_clicks"`, `"after_seconds"`, `"hybrid"`    |
| `trigger_clicks_count`  | number  | Jumlah klik sebelum iklan muncul (untuk `after_clicks`/`hybrid`)        |
| `trigger_seconds_delay` | number  | Delay dalam detik sebelum iklan muncul (untuk `after_seconds`/`hybrid`) |
| `show_on_first_open`    | boolean | Tampilkan iklan saat splash screen selesai (bisa dikombinasikan)        |
| `skip_duration_seconds` | number  | Durasi countdown sebelum tombol skip muncul                             |

### Collection: `campaigns` → Document: (auto-ID Firestore)

| Field             | Tipe      | Deskripsi                                                   |
| ----------------- | --------- | ----------------------------------------------------------- |
| `is_active`       | boolean   | Campaign aktif atau tidak                                   |
| `ad_type`         | string    | `"image"`, `"video"`, atau `"webview"`                      |
| `title`           | string    | Judul internal campaign                                     |
| `media_url`       | string    | URL gambar/video/halaman web                                |
| `target_url`      | string    | URL tujuan saat iklan diklik                                |
| `weight`          | number    | Bobot pemilihan (semakin tinggi, semakin sering terpilih)   |
| `open_target_in`  | string    | `"internal"` (Chrome Custom Tab) atau `"external"` (browser)|
| `button_text`     | string    | Teks tombol CTA (contoh: "Buka", "Selengkapnya")            |
| `schedule_start`  | Timestamp | Jam mulai tayang (hanya jam:menit yang dievaluasi)          |
| `schedule_end`    | Timestamp | Jam selesai tayang (hanya jam:menit yang dievaluasi)        |

> **Catatan**: `ad_id` menggunakan Document ID Firestore (bukan field manual dalam document).

## 🔌 Integrasi ke Proyek Baru

### 1. Gradle Dependencies
```kotlin
// libs.versions.toml sudah mendefinisikan:
// firebase-bom, firebase-firestore, coil-compose, androidx-browser, google-services

// app/build.gradle.kts
plugins {
    alias(libs.plugins.google.services) // WAJIB untuk Firebase
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.coil.compose)
    implementation(libs.androidx.browser)
}
```

### 2. Salin Folder
Salin seluruh folder `customeads/` ke `app/src/main/java/com/<package>/customeads/`.
Ganti package name di setiap file sesuai proyek baru.

### 3. Inisialisasi di MainActivity
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        FirebaseApp.initializeApp(this)
        AdManager.initialize()  // Fetch config & campaigns dari Firestore
        
        setContent {
            val currentAd by AdManager.currentAd.collectAsState()
            
            // ... NavHost setup ...
            
            // Panggil setelah splash screen selesai:
            // AdManager.onSplashFinished()
            
            // Pasang registerClick() pada setiap navigasi:
            // AdManager.registerClick()
            
            // Overlay iklan di atas semua layar:
            currentAd?.let { campaign ->
                InterstitialAdScreen(
                    campaign = campaign,
                    onClose = { AdManager.closeAd() }
                )
            }
        }
    }
}
```

### 4. Pasang Trigger Klik
Panggil `AdManager.registerClick()` di setiap titik interaksi:
- Navigasi dari HomeScreen ke halaman lain
- Tombol back dari halaman kalkulator
- Tombol share/salin

## 🎨 Behavior per Tipe Iklan

| Tipe     | Konten                  | Batas Waktu | Klik Area          | Tombol                        |
| -------- | ----------------------- | ----------- | ------------------ | ----------------------------- |
| `image`  | Gambar full-screen      | Countdown   | Buka target_url    | CTA bawah + Skip kanan atas   |
| `video`  | Placeholder (soon)      | Countdown   | Buka target_url    | CTA bawah + Skip kanan atas   |
| `webview`| Halaman web interaktif  | **Tanpa**   | Dalam webview      | Skip + CTA berdampingan bawah |

## 🐛 Debugging
Semua log menggunakan tag prefix `CustomAds.`:
- `CustomAds.Repository` — Fetch data Firestore
- `CustomAds.Manager` — Logic trigger & state
- `CustomAds.Interstitial` — UI interstitial
- `CustomAds.Main` — Integrasi MainActivity

Filter logcat: `CustomAds.` untuk melihat seluruh alur iklan.
