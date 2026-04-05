package com.azrxtech.hitunguntung.customeads.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Data class representasi konfigurasi iklan dari Firestore.
 * Collection: "ad_settings" -> Document: "hitunguntung_config"
 *
 * Parsing manual dari DocumentSnapshot untuk menghindari masalah
 * @PropertyName + val yang tidak kompatibel dengan Firestore toObject().
 */
data class AdConfig(
    val isAdsEnabled: Boolean = false,
    val triggerStrategy: String = "after_clicks",
    val triggerClicksCount: Int = 5,
    val triggerSecondsDelay: Long = 120,
    val showOnFirstOpen: Boolean = false,
    val skipDurationSeconds: Int = 3
) {
    companion object {
        /**
         * Factory method untuk parsing manual dari DocumentSnapshot.
         * Lebih robust daripada toObject() karena tidak bergantung pada @PropertyName.
         */
        fun fromSnapshot(snapshot: DocumentSnapshot): AdConfig? {
            if (!snapshot.exists()) return null
            return try {
                AdConfig(
                    isAdsEnabled = snapshot.getBoolean("is_ads_enabled") ?: false,
                    triggerStrategy = snapshot.getString("trigger_strategy") ?: "after_clicks",
                    triggerClicksCount = (snapshot.getLong("trigger_clicks_count") ?: 5).toInt(),
                    triggerSecondsDelay = snapshot.getLong("trigger_seconds_delay") ?: 120,
                    showOnFirstOpen = snapshot.getBoolean("show_on_first_open") ?: false,
                    skipDurationSeconds = (snapshot.getLong("skip_duration_seconds") ?: 3).toInt()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}