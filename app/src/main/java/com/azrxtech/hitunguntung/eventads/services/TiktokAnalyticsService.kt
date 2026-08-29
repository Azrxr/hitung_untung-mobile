package com.azrxtech.hitunguntung.eventads.services

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.tiktok.TikTokBusinessSdk
import com.azrxtech.hitunguntung.eventads.models.TiktokSdkConfig
import org.json.JSONObject
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Service to handle TikTok SDK Event Tracking and Configuration.
 */
class TiktokAnalyticsService {
    private var isInitialized = false
    private var isInitializing = false
    private var config: TiktokSdkConfig? = null

    private class QueuedEvent(val name: String, val properties: JSONObject?)
    private val eventQueue = ConcurrentLinkedQueue<QueuedEvent>()
    private val mainHandler = Handler(Looper.getMainLooper())

    val isEnabled: Boolean
        get() = config?.isActive ?: false

    fun initialize(context: Context, config: TiktokSdkConfig) {
        this.config = config
        if (!config.isActive) {
            Log.i(TAG, "TikTok SDK is disabled via config")
            return
        }

        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return
        }
        if (isInitializing) {
            Log.d(TAG, "TikTok SDK initialization is already in progress")
            return
        }

        try {
            val appIdToUse = if (config.androidId.isNotEmpty()) config.androidId else context.packageName
            val tiktokAppIdToUse = if (config.androidTiktokId.isNotEmpty()) config.androidTiktokId else "7651025918617616391"
            val stage = config.stage
            val sandboxMode = stage.equals("sandbox", ignoreCase = true) || stage.equals("debug", ignoreCase = true)

            Log.i(TAG, "Initializing TikTok SDK... App ID: $appIdToUse, TikTok App ID: $tiktokAppIdToUse, Stage: $stage")

            val ttConfig = TikTokBusinessSdk.TTConfig(context.applicationContext)
                .setAppId(appIdToUse)
                .setTTAppId(tiktokAppIdToUse)
                .setFlushTimeInterval(1)

            if (sandboxMode) {
                ttConfig.setLogLevel(TikTokBusinessSdk.LogLevel.DEBUG)
                ttConfig.openDebugMode()
            } else {
                ttConfig.setLogLevel(TikTokBusinessSdk.LogLevel.NONE)
            }

            isInitializing = true
            TikTokBusinessSdk.initializeSdk(ttConfig, object : TikTokBusinessSdk.TTInitCallback {
                override fun success() {
                    mainHandler.post {
                        isInitializing = false
                        isInitialized = true
                        Log.i(TAG, "TikTok SDK initialized successfully. Starting tracker and sending app open event...")
                        startTrackingAndFlush()
                    }
                }

                override fun fail(code: Int, msg: String?) {
                    mainHandler.post {
                        isInitializing = false
                        Log.e(TAG, "TikTok SDK initialization failed. Code=$code, Message=$msg")
                    }
                }
            })
        } catch (e: Throwable) {
            isInitializing = false
            Log.e(TAG, "❌ Error/Throwable initializing TikTok SDK: ${e.message}", e)
        }
    }

    fun logEvent(name: String, parameters: Map<String, Any>?) {
        if (!isEnabled) {
            Log.i(TAG, "⏭️ Skip tracking event '$name': TikTok SDK is disabled via config (isActive = false)")
            return
        }

        val properties = if (parameters != null) mapToJson(parameters) else null

        if (!isInitialized) {
            Log.w(TAG, "⚠️ TikTok SDK is NOT initialized yet! Event '$name' is queued.")
            eventQueue.add(QueuedEvent(name, properties))
            return
        }

        sendEventToSdk(name, properties)
        flushSdk()
    }

    private fun startTrackingAndFlush() {
        try {
            TikTokBusinessSdk.startTrack()
            Log.i(TAG, "TikTok SDK tracking started")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error starting TikTok tracking: ${e.message}", e)
        }

        sendEventToSdk("app_open", null)

        Log.i(TAG, "🔄 Flushing ${eventQueue.size} queued TikTok events...")
        while (!eventQueue.isEmpty()) {
            val event = eventQueue.poll()
            if (event != null) {
                sendEventToSdk(event.name, event.properties)
            }
        }
        flushSdk()
    }

    private fun sendEventToSdk(eventName: String, properties: JSONObject?) {
        try {
            if (properties != null) {
                TikTokBusinessSdk.trackEvent(eventName, properties)
                Log.i(TAG, "✅ Manually tracked event: '$eventName' with properties: $properties")
            } else {
                TikTokBusinessSdk.trackEvent(eventName)
                Log.i(TAG, "✅ Manually tracked event: '$eventName'")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error tracking event '$eventName': ${e.message}", e)
        }
    }

    private fun flushSdk() {
        try {
            TikTokBusinessSdk.flush()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error flushing TikTok events: ${e.message}", e)
        }
    }

    private fun mapToJson(map: Map<String, Any>): JSONObject? {
        val json = JSONObject()
        try {
            map.forEach { (key, value) ->
                json.put(key, value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting map to JSON: ${e.message}")
        }
        return json
    }

    companion object {
        private const val TAG = "EventAds.TiktokAnalytics"
    }
}
