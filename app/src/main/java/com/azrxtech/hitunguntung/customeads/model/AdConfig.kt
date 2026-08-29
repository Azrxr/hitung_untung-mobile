package com.azrxtech.hitunguntung.customeads.model

import org.json.JSONObject
import android.util.Log
import com.azrxtech.hitunguntung.eventads.models.EventSdkConfig
import com.azrxtech.hitunguntung.eventads.models.GoogleSdkConfig
import com.azrxtech.hitunguntung.eventads.models.MetaSdkConfig
import com.azrxtech.hitunguntung.eventads.models.TiktokSdkConfig

/**
 * Data class representasi konfigurasi iklan dari GitHub CDN (Flat JSON).
 */
data class AdConfig(
    val isAdsEnabled: Boolean = false,
    val triggerStrategy: String = "after_clicks",
    val triggerClicksCount: Int = 5,
    val triggerSecondsDelay: Long = 120,
    val showOnFirstOpen: Boolean = false,
    val skipDurationSeconds: Int = 3,
    val activeCampaigns: List<ActiveCampaignConfig> = emptyList(),
    val eventSdk: EventSdkConfig? = null
) {
    companion object {
        private const val TAG = "CustomAds.AdConfig"

        /**
         * Factory method untuk parsing manual dari JSON String.
         */
        fun fromJson(jsonStr: String): AdConfig? {
            return try {
                val root = JSONObject(jsonStr)

                val isAdsEnabled = root.optBoolean("is_ads_enabled", false)
                val triggerStrategy = root.optString("trigger_strategy", "after_clicks")
                val triggerClicksCount = root.optInt("trigger_clicks_count", 5)
                val triggerSecondsDelay = root.optLong("trigger_seconds_delay", 120)
                val showOnFirstOpen = root.optBoolean("show_on_first_open", false)
                val skipDurationSeconds = root.optInt("skip_duration_seconds", 3)

                val activeList = mutableListOf<ActiveCampaignConfig>()
                val activeCampaignsArr = root.optJSONArray("active_campaigns")
                if (activeCampaignsArr != null) {
                    for (i in 0 until activeCampaignsArr.length()) {
                        val obj = activeCampaignsArr.optJSONObject(i) ?: continue
                        activeList.add(
                            ActiveCampaignConfig(
                                campaignId = obj.optString("campaign_id", ""),
                                weight = obj.optInt("weight", 0),
                                isActive = obj.optBoolean("is_active", false),
                                scheduleStart = obj.optString("schedule_start", "00:00"),
                                scheduleEnd = obj.optString("schedule_end", "23:59"),
                                openTargetIn = obj.optString("open_target_in", "internal"),
                                title = obj.optString("title", ""),
                                adType = obj.optString("ad_type", "image"),
                                mediaUrl = obj.optString("media_url", ""),
                                targetUrl = obj.optString("target_url", ""),
                                buttonText = obj.optString("button_text", "Buka")
                            )
                        )
                    }
                }

                var eventSdkConfig: EventSdkConfig? = null
                val eventSdkObj = root.optJSONObject("event_sdk")
                if (eventSdkObj != null) {
                    val googleObj = eventSdkObj.optJSONObject("google")
                    val googleConfig = if (googleObj != null) {
                        GoogleSdkConfig(
                            isActive = googleObj.optBoolean("is_active", false),
                            isDebugViewEnabled = googleObj.optBoolean("is_debug_view_enabled", false),
                            gtmIsActive = googleObj.optBoolean("gtm_is_active", false)
                        )
                    } else {
                        GoogleSdkConfig()
                    }

                    val metaObj = eventSdkObj.optJSONObject("meta")
                    val metaConfig = if (metaObj != null) {
                        MetaSdkConfig(
                            isActive = metaObj.optBoolean("is_active", false),
                            metaAppId = metaObj.optString("meta_app_id", ""),
                            metaClientToken = metaObj.optString("meta_client_token", "")
                        )
                    } else {
                        MetaSdkConfig()
                    }

                    val tiktokObj = eventSdkObj.optJSONObject("tiktok")
                    val tiktokConfig = if (tiktokObj != null) {
                        TiktokSdkConfig(
                            isActive = tiktokObj.optBoolean("is_active", false),
                            androidId = tiktokObj.optString("android_id", ""),
                            androidTiktokId = tiktokObj.optString("android_tiktok_id", ""),
                            appleId = tiktokObj.optString("apple_id", ""),
                            appleTiktokId = tiktokObj.optString("apple_tiktok_id", ""),
                            stage = tiktokObj.optString("stage", "sandbox")
                        )
                    } else {
                        TiktokSdkConfig()
                    }

                    eventSdkConfig = EventSdkConfig(
                        google = googleConfig,
                        meta = metaConfig,
                        tiktok = tiktokConfig
                    )
                    Log.d(TAG, "Parsed EventSdkConfig: $eventSdkConfig")
                } else {
                    Log.w(TAG, "event_sdk JSON object not found in configuration!")
                }

                val parsedConfig = AdConfig(
                    isAdsEnabled = isAdsEnabled,
                    triggerStrategy = triggerStrategy,
                    triggerClicksCount = triggerClicksCount,
                    triggerSecondsDelay = triggerSecondsDelay,
                    showOnFirstOpen = showOnFirstOpen,
                    skipDurationSeconds = skipDurationSeconds,
                    activeCampaigns = activeList,
                    eventSdk = eventSdkConfig
                )
                Log.d(TAG, "Successfully parsed AdConfig: $parsedConfig")
                parsedConfig
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing flat AdConfig JSON: ${e.message}", e)
                null
            }
        }
    }
}

data class ActiveCampaignConfig(
    val campaignId: String = "",
    val weight: Int = 0,
    val isActive: Boolean = false,
    val scheduleStart: String = "00:00",
    val scheduleEnd: String = "23:59",
    val openTargetIn: String = "internal",
    val title: String = "",
    val adType: String = "image",
    val mediaUrl: String = "",
    val targetUrl: String = "",
    val buttonText: String = "Buka"
)