package com.azrxtech.hitunguntung.eventads.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.azrxtech.hitunguntung.eventads.models.MetaSdkConfig

/**
 * Service to handle Meta (Facebook SDK App Events) integration.
 */
class MetaAnalyticsService {
    private var logger: AppEventsLogger? = null
    private var initialized = false
    private var config: MetaSdkConfig? = null

    val isEnabled: Boolean
        get() = config?.isActive ?: false

    fun initialize(context: Context, config: MetaSdkConfig) {
        this.config = config
        if (!config.isActive) {
            Log.d(TAG, "Meta Analytics Service: Disabled by configuration.")
            return
        }

        try {
            if (config.metaAppId.isNotEmpty()) {
                FacebookSdk.setApplicationId(config.metaAppId)
            }
            if (config.metaClientToken.isNotEmpty()) {
                FacebookSdk.setClientToken(config.metaClientToken)
            }

            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.sdkInitialize(context.applicationContext)
            
            // Enable auto logging and activity tracking
            AppEventsLogger.activateApp(context.applicationContext as android.app.Application)

            logger = AppEventsLogger.newLogger(context)
            initialized = true
            Log.i(TAG, "Meta Analytics Service: Initialized successfully. App ID: ${config.metaAppId}")
        } catch (e: Exception) {
            Log.e(TAG, "Meta Analytics Service: Failed to initialize Meta SDK: ${e.message}", e)
        }
    }

    fun logEvent(name: String, parameters: Map<String, Any>?) {
        if (!initialized || !isEnabled || logger == null) return

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

            logger?.logEvent(name, bundle)
            Log.d(TAG, "Meta Analytics Service: Logged Event -> $name with params $parameters")
        } catch (e: Exception) {
            Log.e(TAG, "Meta Analytics Service: Failed to log event ($name): ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "EventAds.MetaAnalytics"
    }
}
