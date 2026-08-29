# Panduan Preview & Pengujian Event Ads SDK (Sandbox & Produksi)

Dokumentasi ini menjelaskan langkah-langkah untuk memantau, menguji, dan memverifikasi pengiriman event dari game Dax-Racing ke Google Analytics (Firebase), Google Tag Manager (GTM), Meta (Facebook), dan TikTok SDK baik pada mode **Sandbox/Development** maupun **Produksi**.

---

## 1. Google Analytics (Firebase) & Google Tag Manager (GTM)

GTM versi terbaru di platform Android berjalan secara paralel dengan Firebase Analytics. Semua event yang Anda kirim ke Firebase Analytics secara otomatis diteruskan ke container Google Tag Manager Anda.

### A. Cara Mengaktifkan Firebase DebugView (Real-time Event Preview)
Untuk melihat event yang dikirimkan secara langsung (real-time) di dasbor Firebase Console:
1. Sambungkan perangkat Android atau emulator ke komputer.
2. Pastikan USB Debugging aktif.
3. Jalankan perintah ADB berikut pada terminal/command prompt Anda untuk mengaktifkan mode debug:
   ```bash
   adb shell setprop debug.firebase.analytics.app com.azroralabs.dax69
   ```
   *(Ganti `com.azroralabs.dax69` dengan Package Name yang sedang Anda jalankan jika berbeda)*
4. Buka **[Firebase Console](https://console.firebase.google.com/)** -> **Analytics** -> **DebugView**.
5. Jalankan game dan lakukan interaksi (misal memicu iklan tampil atau klik). Event akan langsung muncul di dasbor dalam waktu kurang dari 15 detik.
6. Untuk mematikan mode debug setelah selesai pengujian:
   ```bash
   adb shell setprop debug.firebase.analytics.app .none.
   ```

### B. Cara Melihat Log Google Tag Manager (Verbose Logging)
Untuk melihat bagaimana GTM mengevaluasi aturan (rules) kontainer dan memicu tag saat event Firebase masuk, aktifkan log verbose GTM di Logcat:
1. Jalankan perintah ADB berikut:
   ```bash
   adb shell setprop log.tag.GoogleTagManager VERBOSE
   ```
2. Buka Logcat di Android Studio atau terminal, lalu filter log menggunakan tag:
   ```text
   GoogleTagManager
   ```
3. Anda akan melihat log detail ketika kontainer GTM memuat file `.json` dari folder `assets/containers/` dan saat ia memproses event-event Firebase.

---

## 2. Meta App Events (Facebook SDK)

Meta SDK dikonfigurasi menggunakan ID formalitas developer pada `AndroidManifest.xml` / `strings.xml` sebagai bumper awal (mencegah crash) dan dimatikan secara default (`AutoInitEnabled=false`, `AutoLogAppEventsEnabled=false`).
Begitu aplikasi berhasil memuat konfigurasi remote, ID asli dari klien akan diinisialisasi secara dinamis dari sisi aplikasi.

### A. Format Konfigurasi Remote (JSON)
Masukkan App ID dan Client Token asli dari klien di konfigurasi JSON remote Anda pada properti berikut:
   ```json
   "meta": {
     "is_active": true,
     "meta_app_id": "YOUR_META_APP_ID",
     "meta_client_token": "YOUR_META_CLIENT_TOKEN"
   }
   ```
 6. Jalankan game dan picu event (misal klik iklan custom). Verifikasi pengiriman event pada dasbor Meta Events Manager Anda.

### B. Memantau via Logcat Android
Untuk melihat aktivitas SDK Meta di Logcat, filter log dengan tag berikut:
```text
EventAds.MetaAnalytics
```
Anda juga bisa mengaktifkan logging behavior SDK Meta menggunakan perintah:
```bash
adb shell setprop log.tag.FacebookSdk VERBOSE
```

---

## 3. TikTok Business SDK

TikTok SDK melacak event secara real-time dan menyediakan dasbor debug jika dikonfigurasi menggunakan stage `sandbox` atau `debug`.

### A. Cara Menguji Event Sandbox
1. Atur konfigurasi stage TikTok ke `sandbox` atau `debug` pada JSON konfigurasi remote Anda:
   ```json
   "tiktok": {
     "is_active": true,
     "android_id": "com.azroralabs.dax69",
     "android_tiktok_id": "YOUR_TIKTOK_APP_ID",
     "stage": "sandbox"
   }
   ```
2. TikTok SDK akan secara otomatis mengubah log level ke `DEBUG` dan mengaktifkan internal debug mode.
3. Sambungkan perangkat Anda dan jalankan game.
4. Buka dasbor **TikTok Events Manager** Anda, masuk ke menu pengujian aplikasi untuk memantau event masuk.

### B. Memantau Logcat TikTok
Semua log inisialisasi, pengantrean event (queueing), dan pengiriman TikTok SDK ditandai dengan tag berikut pada Logcat:
```text
EventAds.TiktokAnalytics
```
Anda juga dapat melihat output internal dari TikTok Business SDK dengan memfilter log untuk string:
```text
TikTokBusinessSdk
```
Saat event sukses terkirim atau ditambahkan ke antrean, Anda akan melihat log seperti:
* `⚠️ TikTok SDK is NOT initialized yet! Event 'ad_shown' is queued.`
* `✅ Manually tracked event: 'ad_clicked' with properties: ...`

---

## Ringkasan Perintah Debugging (Logcat Filters)

Untuk mempermudah memantau semua SDK berjalan bersamaan saat pengujian di Android Studio, buat filter Logcat dengan regex berikut:
```text
EventAds|GoogleTagManager|TikTokBusinessSdk|FacebookSdk
```
Ini akan menampilkan seluruh alur inisialisasi dan log event dari keempat provider secara real-time!
