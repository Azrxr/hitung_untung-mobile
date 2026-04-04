package com.azrxtech.hitunguntung.customeads.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Singleton Object agar bisa dipanggil dari mana saja tanpa inisialisasi berulang
object AdManager {

    // State untuk memberi tahu UI apakah harus memunculkan iklan sekarang
    private val _showInterstitialAd = MutableStateFlow(false)
    val showInterstitialAd: StateFlow<Boolean> = _showInterstitialAd

    // Variabel internal untuk melacak state pengguna
    private var clickCount = 0
    private var isFirstOpenDone = false

    // Konfigurasi dari Firestore (Akan diisi oleh Repository nanti)
    var isAdsEnabled: Boolean = false
    var triggerStrategy: String = "after_clicks"
    var targetClicks: Int = 5
    var showOnFirstOpen: Boolean = false

    /**
     * Panggil fungsi ini saat Splash Screen selesai
     */
    fun onAppStarted() {
        if (!isAdsEnabled) return

        if (showOnFirstOpen && !isFirstOpenDone) {
            _showInterstitialAd.value = true
            isFirstOpenDone = true
        }

        // TODO: Jika triggerStrategy == "after_seconds", jalankan Coroutine Timer di sini
    }

    /**
     * Panggil fungsi ini di setiap tombol yang diklik di aplikasi (misal: klik menu Home)
     */
    fun registerClick() {
        if (!isAdsEnabled) return
        if (triggerStrategy != "after_clicks" && triggerStrategy != "hybrid") return

        clickCount++
        if (clickCount >= targetClicks) {
            _showInterstitialAd.value = true
            clickCount = 0 // Reset hitungan
        }
    }

    /**
     * Panggil fungsi ini saat user klik "Tutup/Skip" di layar iklan
     */
    fun closeAd() {
        _showInterstitialAd.value = false
    }
}