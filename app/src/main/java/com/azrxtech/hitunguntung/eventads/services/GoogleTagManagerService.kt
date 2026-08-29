package com.azrxtech.hitunguntung.eventads.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.azrxtech.hitunguntung.eventads.models.GoogleSdkConfig

/**
 * Service to handle Google Tag Manager (GTM) integration.
 * On modern Android (GTM V5), GTM relies entirely on Firebase Analytics events.
 * Logging events to Firebase Analytics automatically forwards them to Google Tag Manager.
 */
class GoogleTagManagerService {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var initialized = false
    private var config: GoogleSdkConfig? = null

    val isEnabled: Boolean
        get() = config?.gtmIsActive ?: false

    fun initialize(context: Context, config: GoogleSdkConfig) {
        this.config = config
        if (!config.gtmIsActive) {
            Log.d(TAG, "Google Tag Manager Service: Disabled by configuration.")
            return
        }

        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            initialized = true
            Log.i(TAG, "Google Tag Manager Service: Initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Google Tag Manager Service: Failed to initialize GTM: ${e.message}", e)
        }
    }

    /**
     * Log event to GTM stream. If Google Analytics is also active, we avoid duplicates
     * because both use Firebase Analytics under the hood for event routing.
     */
    fun logEvent(name: String, parameters: Map<String, Any>?, isGoogleAnalyticsEnabled: Boolean) {
        if (!initialized || !isEnabled || firebaseAnalytics == null) return

        // If Google Analytics is also enabled, it will log the event to Firebase Analytics,
        // and GTM will automatically capture it. We don't log it again to avoid duplicate events.
        if (isGoogleAnalyticsEnabled) {
            Log.d(TAG, "GTM: Event '$name' will be captured via Google Analytics stream (to avoid duplicates).")
            return
        }

        try {
            val bundle = Bundle()
            parameters?.forEach { (key, value) ->
                when (value) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Boolean -> bundle.putBoolean(key, value)
                    else -> bundle.putString(key, value.toString())
                }
            }

            firebaseAnalytics?.logEvent(name, bundle)
            Log.d(TAG, "Google Tag Manager Service: Logged Event -> $name via Firebase Analytics stream for GTM.")
        } catch (e: Exception) {
            Log.e(TAG, "Google Tag Manager Service: Failed to log event ($name) via GTM stream: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "EventAds.GoogleTagManager"
    }
}
