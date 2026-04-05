package com.azrxtech.hitunguntung.customeads.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Calendar

/**
 * Data class representasi campaign iklan dari Firestore.
 * Collection: "campaigns" -> Document: auto-generated ID
 *
 * ad_id menggunakan document ID Firestore (bukan field manual).
 * schedule_start dan schedule_end menggunakan Firebase Timestamp.
 * Hanya jam:menit yang dievaluasi (tanggal diabaikan).
 */
data class AdCampaign(
    val adId: String = "",
    val isActive: Boolean = false,
    val adType: String = "image",
    val title: String = "",
    val mediaUrl: String = "",
    val targetUrl: String = "",
    val weight: Int = 0,
    val openTargetIn: String = "internal",
    val buttonText: String = "Buka",
    val scheduleStart: Timestamp? = null,
    val scheduleEnd: Timestamp? = null
) {
    /**
     * Mengecek apakah waktu sekarang (jam:menit) berada di antara
     * jam mulai dan jam selesai. Tanggal/bulan/tahun DIABAIKAN.
     */
    fun isTimeEligible(): Boolean {
        if (scheduleStart == null || scheduleEnd == null) return true

        val now = Calendar.getInstance()
        val currentMinutesOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val startCal = Calendar.getInstance().apply { time = scheduleStart.toDate() }
        val startMinutesOfDay = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)

        val endCal = Calendar.getInstance().apply { time = scheduleEnd.toDate() }
        val endMinutesOfDay = endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)

        return if (startMinutesOfDay <= endMinutesOfDay) {
            // Jadwal normal (misal 08:00 - 17:00)
            currentMinutesOfDay in startMinutesOfDay..endMinutesOfDay
        } else {
            // Jadwal melewati tengah malam (misal 22:00 - 04:00)
            currentMinutesOfDay >= startMinutesOfDay || currentMinutesOfDay <= endMinutesOfDay
        }
    }

    companion object {
        /**
         * Factory method untuk parsing manual dari DocumentSnapshot.
         * ad_id diambil dari document.id (bukan field dalam document).
         */
        fun fromSnapshot(snapshot: DocumentSnapshot): AdCampaign? {
            if (!snapshot.exists()) return null
            return try {
                AdCampaign(
                    adId = snapshot.id, // Menggunakan document ID Firestore
                    isActive = snapshot.getBoolean("is_active") ?: false,
                    adType = snapshot.getString("ad_type") ?: "image",
                    title = snapshot.getString("title") ?: "",
                    mediaUrl = snapshot.getString("media_url") ?: "",
                    targetUrl = snapshot.getString("target_url") ?: "",
                    weight = (snapshot.getLong("weight") ?: 0).toInt(),
                    openTargetIn = snapshot.getString("open_target_in") ?: "internal",
                    buttonText = snapshot.getString("button_text") ?: "Buka",
                    scheduleStart = snapshot.getTimestamp("schedule_start"),
                    scheduleEnd = snapshot.getTimestamp("schedule_end")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}