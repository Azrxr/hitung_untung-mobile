package com.azrxtech.hitunguntung.eventads

import android.content.Context
import android.util.Log
import com.azrxtech.hitunguntung.eventads.models.EventSdkConfig
import com.azrxtech.hitunguntung.eventads.services.GoogleAnalyticsService
import com.azrxtech.hitunguntung.eventads.services.GoogleTagManagerService
import com.azrxtech.hitunguntung.eventads.services.MetaAnalyticsService
import com.azrxtech.hitunguntung.eventads.services.TiktokAnalyticsService

/**
 * Centrally managed Facade singleton class to handle initialization and event logging
 * across Google Analytics (Firebase), Google Tag Manager (GTM), Meta (Facebook), and TikTok.
 */
object EventAdsManager {
    private const val TAG = "EventAds.Manager"

    private val googleService = GoogleAnalyticsService()
    private val gtmService = GoogleTagManagerService()
    private val metaService = MetaAnalyticsService()
    private val tiktokService = TiktokAnalyticsService()

    private var initialized = false

    fun initialize(context: Context, config: EventSdkConfig) {
        if (initialized) {
            Log.d(TAG, "EventAdsManager is already initialized.")
            return
        }

        Log.i(TAG, "EventAdsManager: Starting initialization of all active SDKs...")

        // Initialize each analytics provider
        googleService.initialize(context, config.google)
        gtmService.initialize(context, config.google)
        metaService.initialize(context, config.meta)
        tiktokService.initialize(context, config.tiktok)

        initialized = true
        Log.i(TAG, "EventAdsManager: All active SDKs have been initialized.")
    }

    /**
     * Common event logging method to dispatch events to all active providers.
     */
    fun logEvent(name: String, parameters: Map<String, Any>? = null) {
        if (!initialized) {
            Log.w(TAG, "EventAdsManager: Cannot log event '$name'. SDK is not initialized yet.")
            return
        }

        googleService.logEvent(name, parameters)
        gtmService.logEvent(name, parameters, googleService.isEnabled)
        metaService.logEvent(name, parameters)
        tiktokService.logEvent(name, parameters)
    }

    // ===========================================================================
    // ADVERTISING EVENTS (CUSTOM ADS)
    // ===========================================================================

    /**
     * Tracks a custom ad view (Ad Impression).
     */
    fun logAdImpression(campaignId: String, adType: String, triggerStrategy: String) {
        val params = mapOf(
            "campaign_id" to campaignId,
            "ad_type" to adType,
            "trigger_strategy" to triggerStrategy,
            "content_name" to "Custom Ad Impression"
        )
        logEvent("ad_impression", params)
    }

    /**
     * Tracks a custom ad CTA/target click (Ad Click).
     */
    fun logAdClick(campaignId: String, adType: String, targetUrl: String) {
        val params = mapOf(
            "campaign_id" to campaignId,
            "ad_type" to adType,
            "target_url" to targetUrl,
            "content_name" to "Custom Ad Click"
        )
        logEvent("ad_click", params)
    }
}
