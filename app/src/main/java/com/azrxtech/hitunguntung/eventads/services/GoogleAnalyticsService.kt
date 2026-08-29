package com.azrxtech.hitunguntung.eventads.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.azrxtech.hitunguntung.eventads.models.GoogleSdkConfig

/**
 * Service to handle Google Analytics (Firebase) integration.
 */
class GoogleAnalyticsService {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var initialized = false
    private var config: GoogleSdkConfig? = null

    val isEnabled: Boolean
        get() = config?.isActive ?: false

    fun initialize(context: Context, config: GoogleSdkConfig) {
        this.config = config
        if (!config.isActive) {
            Log.d(TAG, "Google Analytics Service: Disabled by configuration.")
            return
        }

        try {
            // Ensure Firebase Core has been initialized
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            initialized = true

            // Enable analytics collection if active
            firebaseAnalytics?.setAnalyticsCollectionEnabled(true)

            Log.i(TAG, "Google Analytics Service: Initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Google Analytics Service: Failed to initialize Firebase Analytics: ${e.message}", e)
        }
    }

    fun logEvent(name: String, parameters: Map<String, Any>?) {
        if (!initialized || !isEnabled || firebaseAnalytics == null) return

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
            Log.d(TAG, "Google Analytics Service: Logged Event -> $name with params $parameters")
        } catch (e: Exception) {
            Log.e(TAG, "Google Analytics Service: Failed to log event ($name): ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "EventAds.GoogleAnalytics"
    }
}
