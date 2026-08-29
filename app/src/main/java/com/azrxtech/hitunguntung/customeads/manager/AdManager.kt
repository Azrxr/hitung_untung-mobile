package com.azrxtech.hitunguntung.customeads.manager

import android.content.Context
import android.util.Log
import com.azrxtech.hitunguntung.customeads.repository.AdRepository
import com.azrxtech.hitunguntung.customeads.model.AdCampaign
import com.azrxtech.hitunguntung.customeads.model.AdConfig
import com.azrxtech.hitunguntung.eventads.EventAdsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Singleton manager yang mengatur kapan dan iklan mana yang ditampilkan.
 * Optimized untuk Aplikasi Non-Game (Native Compose Android).
 */
object AdManager {

    private const val TAG = "CustomAds.Manager"

    private val repository = AdRepository()
    private val scope = CoroutineScope(Dispatchers.Main)

    var config: AdConfig? = null
        private set

    private var appContext: Context? = null

    // State untuk UI: campaign yang sedang ditampilkan (null = tidak ada iklan)
    private val _currentAd = MutableStateFlow<AdCampaign?>(null)
    val currentAd: StateFlow<AdCampaign?> = _currentAd

    // State menandakan data sudah siap
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private var clickCount = 0
    private var isFirstOpenHandled = false
    private var timerJob: Job? = null

    /**
     * Inisialisasi: Fetch config dari GitHub CDN dan init EventAds SDK.
     * Panggil di MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        Log.i(TAG, "🚀 initialize() dipanggil")
        appContext = context.applicationContext

        scope.launch(Dispatchers.IO) {
            try {
                config = repository.getConfig(context)

                launch(Dispatchers.Main) {
                    _isReady.value = true
                    Log.i(TAG, "✅ Data siap: config=${config != null}")

                    // Inisialisasi EventAds SDK jika aktif
                    val sdkConf = config?.eventSdk
                    if (sdkConf != null) {
                        EventAdsManager.initialize(context, sdkConf)
                    } else {
                        Log.w(TAG, "⚠️ event_sdk configuration is missing in config!")
                    }

                    if (config == null) {
                        Log.w(TAG, "⚠️ Config NULL → iklan tidak akan berjalan")
                    } else if (!config!!.isAdsEnabled) {
                        Log.i(TAG, "🔕 is_ads_enabled=false → semua iklan DIMATIKAN")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ initialize() ERROR: ${e.message}", e)
                launch(Dispatchers.Main) {
                    _isReady.value = true
                }
            }
        }
    }

    /**
     * Dipanggil setelah splash screen selesai & navigasi ke home.
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

        Log.i(TAG, "🎬 onSplashFinished() → strategy=${conf.triggerStrategy}, showOnFirstOpen=${conf.showOnFirstOpen}")

        if (conf.showOnFirstOpen && !isFirstOpenHandled) {
            Log.i(TAG, "📺 show_on_first_open=true → menampilkan iklan pertama")
            isFirstOpenHandled = true
            triggerAd(force = true)
        } else {
            isFirstOpenHandled = true
        }

        if (_currentAd.value == null) {
            startTimerIfNeeded()
        }
    }

    /**
     * Register interaksi user (navigasi, klik tombol, dsb).
     * Langsung memicu iklan jika ambang batas klik tercapai.
     */
    fun registerClick() {
        val conf = config ?: return
        if (!conf.isAdsEnabled) return

        when (conf.triggerStrategy) {
            "after_clicks", "hybrid" -> {
                clickCount++
                Log.d(TAG, "👆 registerClick() → count=$clickCount/${conf.triggerClicksCount} (strategy=${conf.triggerStrategy})")
                if (clickCount >= conf.triggerClicksCount) {
                    Log.i(TAG, "🎯 Click threshold tercapai! Memicu iklan...")
                    triggerAd(force = true)
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

        clickCount = 0
        startTimerIfNeeded()
    }

    /**
     * Memilih campaign dan menampilkan iklan.
     */
    fun triggerAd(force: Boolean = false) {
        if (_currentAd.value != null) {
            Log.d(TAG, "⏭️ triggerAd() → iklan sudah tampil, skip (hindari tumpuk)")
            return
        }

        Log.d(TAG, "🎬 Memulai proses triggerAd()...")
        scope.launch(Dispatchers.IO) {
            val context = appContext
            if (context != null) {
                val freshConfig = repository.getConfig(context)
                if (freshConfig != null) {
                    config = freshConfig
                }
            }

            val conf = config
            if (conf == null) {
                Log.w(TAG, "⏭️ triggerAd() → config NULL, skip")
                return@launch
            }
            if (!conf.isAdsEnabled) {
                Log.i(TAG, "⏭️ triggerAd() → ads disabled, skip")
                return@launch
            }

            val selectedCampaign = repository.fetchSelectedCampaign(conf)
            launch(Dispatchers.Main) {
                if (selectedCampaign != null) {
                    Log.i(TAG, "📺 Menampilkan iklan terpilih: ${selectedCampaign.adId}")
                    _currentAd.value = selectedCampaign
                    timerJob?.cancel() // Matikan timer selagi iklan muncul

                    // Log Ad Impression
                    EventAdsManager.logAdImpression(
                        campaignId = selectedCampaign.adId,
                        adType = selectedCampaign.adType,
                        triggerStrategy = conf.triggerStrategy
                    )
                } else {
                    Log.w(TAG, "⚠️ triggerAd() → tidak ada campaign yang didapatkan")
                }
            }
        }
    }

    /**
     * Mulai timer countdown untuk strategi after_seconds atau hybrid.
     */
    private fun startTimerIfNeeded() {
        val conf = config ?: return
        if (!conf.isAdsEnabled) return

        when (conf.triggerStrategy) {
            "after_seconds", "hybrid" -> {
                timerJob?.cancel()
                Log.d(TAG, "⏱️ Memulai timer ${conf.triggerSecondsDelay} detik...")
                timerJob = scope.launch {
                    delay(conf.triggerSecondsDelay * 1000L)
                    Log.i(TAG, "⏰ Timer habis! Memicu iklan...")
                    triggerAd(force = true)
                }
            }
        }
    }
}