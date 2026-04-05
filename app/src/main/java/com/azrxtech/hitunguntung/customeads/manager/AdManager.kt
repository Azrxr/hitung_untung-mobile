package com.azrxtech.hitunguntung.customeads.manager

import android.util.Log
import com.azrxtech.hitunguntung.customeads.repository.AdRepository
import com.azrxtech.hitunguntung.customeads.model.AdCampaign
import com.azrxtech.hitunguntung.customeads.model.AdConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Singleton manager yang mengatur kapan dan iklan mana yang ditampilkan.
 *
 * Alur:
 * 1. initialize() → fetch config & campaigns dari Firestore (async)
 * 2. onSplashFinished() → dipanggil setelah splash screen selesai
 *    - Jika show_on_first_open=true → langsung trigger iklan
 *    - Mulai timer jika strategi after_seconds/hybrid
 * 3. registerClick() → dipanggil setiap ada interaksi user (navigasi, dsb)
 *    - Count naik jika strategi after_clicks/hybrid
 *    - Trigger iklan jika count mencapai threshold
 * 4. closeAd() → user menutup iklan → reset counter & restart timer
 *
 * Reusable: Cukup salin folder customeads, panggil initialize() dan onSplashFinished().
 */
object AdManager {

    private const val TAG = "CustomAds.Manager"

    private val repository = AdRepository()
    private val scope = CoroutineScope(Dispatchers.Main)

    var config: AdConfig? = null
        private set
    private var campaigns: List<AdCampaign> = emptyList()

    // State untuk UI: campaign yang sedang ditampilkan (null = tidak ada iklan)
    private val _currentAd = MutableStateFlow<AdCampaign?>(null)
    val currentAd: StateFlow<AdCampaign?> = _currentAd

    // State menandakan data sudah siap dari Firestore
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private var clickCount = 0
    private var isFirstOpenHandled = false
    private var timerJob: Job? = null

    /**
     * Inisialisasi: Fetch config & campaigns dari Firestore.
     * Panggil di MainActivity.onCreate() atau Application.onCreate().
     */
    fun initialize() {
        Log.i(TAG, "🚀 initialize() dipanggil")
        scope.launch(Dispatchers.IO) {
            try {
                config = repository.getConfig()
                campaigns = repository.getActiveCampaigns()

                launch(Dispatchers.Main) {
                    _isReady.value = true
                    Log.i(TAG, "✅ Data siap: config=${config != null}, campaigns=${campaigns.size}")

                    if (config == null) {
                        Log.w(TAG, "⚠️ Config NULL → iklan tidak akan berjalan")
                    } else if (!config!!.isAdsEnabled) {
                        Log.i(TAG, "🔕 is_ads_enabled=false → semua iklan DIMATIKAN")
                    } else if (campaigns.isEmpty()) {
                        Log.w(TAG, "⚠️ Tidak ada campaign aktif yang lolos filter")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ initialize() ERROR: ${e.message}", e)
                launch(Dispatchers.Main) {
                    _isReady.value = true // tetap true agar app tidak stuck
                }
            }
        }
    }

    /**
     * Dipanggil setelah splash screen selesai & navigasi ke home.
     * Ini adalah entry point utama untuk logika iklan pertama kali.
     */
    fun onSplashFinished() {
        val conf = config
        if (conf == null) {
            Log.w(TAG, "⏭️ onSplashFinished() → config NULL, skip")
            return
        }
        if (!conf.isAdsEnabled) {
            Log.i(TAG, "⏭️ onSplashFinished() → ads disabled, skip")
            return
        }
        if (campaigns.isEmpty()) {
            Log.w(TAG, "⏭️ onSplashFinished() → campaigns kosong, skip")
            return
        }

        Log.i(TAG, "🎬 onSplashFinished() → strategy=${conf.triggerStrategy}, showOnFirstOpen=${conf.showOnFirstOpen}")

        // Handle show_on_first_open (hanya sekali)
        if (conf.showOnFirstOpen && !isFirstOpenHandled) {
            Log.i(TAG, "📺 show_on_first_open=true → menampilkan iklan pertama")
            isFirstOpenHandled = true
            triggerAd()
        } else {
            isFirstOpenHandled = true
        }

        // Mulai timer jika diperlukan (hanya jika iklan tidak sedang tampil)
        if (_currentAd.value == null) {
            startTimerIfNeeded()
        }
    }

    /**
     * Register interaksi user (navigasi, klik tombol, dsb).
     * Hanya menghitung klik jika strategi after_clicks atau hybrid.
     */
    fun registerClick() {
        val conf = config ?: return
        if (!conf.isAdsEnabled || campaigns.isEmpty()) return

        when (conf.triggerStrategy) {
            "after_clicks", "hybrid" -> {
                clickCount++
                Log.d(TAG, "👆 registerClick() → count=$clickCount/${conf.triggerClicksCount} (strategy=${conf.triggerStrategy})")
                if (clickCount >= conf.triggerClicksCount) {
                    Log.i(TAG, "🎯 Click threshold tercapai! Trigger iklan...")
                    triggerAd()
                }
            }
            else -> {
                Log.d(TAG, "👆 registerClick() → strategi '${conf.triggerStrategy}' tidak menghitung klik, diabaikan")
            }
        }
    }

    /**
     * Menutup iklan yang sedang tampil dan reset counter.
     */
    fun closeAd() {
        val closedAd = _currentAd.value
        _currentAd.value = null
        Log.i(TAG, "❎ closeAd() → iklan '${closedAd?.adId}' ditutup")

        // Reset hitungan & restart timer
        clickCount = 0
        startTimerIfNeeded()
    }

    /**
     * Memilih campaign dan menampilkan iklan.
     */
    private fun triggerAd() {
        if (_currentAd.value != null) {
            Log.d(TAG, "⏭️ triggerAd() → iklan sudah tampil, skip (hindari tumpuk)")
            return
        }

        val selectedCampaign = selectCampaignByWeight()
        if (selectedCampaign != null) {
            Log.i(TAG, "📺 Menampilkan iklan:")
            Log.i(TAG, "   ├─ id: ${selectedCampaign.adId}")
            Log.i(TAG, "   ├─ type: ${selectedCampaign.adType}")
            Log.i(TAG, "   ├─ title: ${selectedCampaign.title}")
            Log.i(TAG, "   ├─ media: ${selectedCampaign.mediaUrl}")
            Log.i(TAG, "   └─ target: ${selectedCampaign.targetUrl}")
            _currentAd.value = selectedCampaign
            timerJob?.cancel() // Matikan timer selagi iklan muncul
        } else {
            Log.w(TAG, "⚠️ triggerAd() → tidak ada campaign yang terpilih")
        }
    }

    /**
     * Mulai timer countdown untuk strategi after_seconds atau hybrid.
     */
    private fun startTimerIfNeeded() {
        val conf = config ?: return
        if (!conf.isAdsEnabled || campaigns.isEmpty()) return

        when (conf.triggerStrategy) {
            "after_seconds", "hybrid" -> {
                timerJob?.cancel()
                Log.d(TAG, "⏱️ Memulai timer ${conf.triggerSecondsDelay} detik...")
                timerJob = scope.launch {
                    delay(conf.triggerSecondsDelay * 1000L)
                    Log.i(TAG, "⏰ Timer habis! Trigger iklan...")
                    triggerAd()
                }
            }
        }
    }

    /**
     * Algoritma pemilihan iklan berdasarkan bobot (weight).
     * Campaign dengan weight lebih tinggi lebih sering terpilih.
     */
    private fun selectCampaignByWeight(): AdCampaign? {
        if (campaigns.isEmpty()) return null

        val totalWeight = campaigns.sumOf { it.weight }
        if (totalWeight <= 0) {
            Log.d(TAG, "🎲 Semua weight=0, memilih random")
            return campaigns.random()
        }

        var randomValue = Random.nextInt(totalWeight)
        Log.d(TAG, "🎲 Seleksi: randomValue=$randomValue dari totalWeight=$totalWeight")
        for (campaign in campaigns) {
            randomValue -= campaign.weight
            if (randomValue < 0) {
                return campaign
            }
        }
        return campaigns.last()
    }
}