package com.azrxtech.hitunguntung.customeads.repository

import android.util.Log
import com.azrxtech.hitunguntung.customeads.model.AdCampaign
import com.azrxtech.hitunguntung.customeads.model.AdConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Repository untuk mengambil data iklan dari Firestore.
 * Semua akses data Firebase terpusat di sini.
 */
class AdRepository {

    companion object {
        private const val TAG = "CustomAds.Repository"
    }

    // SAFE GETTER: Mencegah Force Close jika Firebase belum diinisialisasi
    private val db: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "❌ FirebaseApp belum siap! Pastikan google-services.json ada dan initializeApp dipanggil.", e)
            null
        }

    /**
     * Mengambil konfigurasi iklan dari Firestore.
     * Collection: "ad_settings" -> Document: "hitunguntung_config"
     */
    suspend fun getConfig(): AdConfig? {
        val firestore = db ?: run {
            Log.e(TAG, "❌ getConfig() - Firestore instance NULL")
            return null
        }

        return try {
            Log.d(TAG, "📡 Fetching ad config dari ad_settings/hitunguntung_config...")
            val snapshot = firestore.collection("ad_settings")
                .document("hitunguntung_config")
                .get()
                .await()

            if (!snapshot.exists()) {
                Log.w(TAG, "⚠️ Document hitunguntung_config TIDAK DITEMUKAN di Firestore")
                return null
            }

            val config = AdConfig.fromSnapshot(snapshot)
            if (config != null) {
                Log.i(TAG, "✅ Config berhasil dimuat:")
                Log.i(TAG, "   ├─ is_ads_enabled: ${config.isAdsEnabled}")
                Log.i(TAG, "   ├─ trigger_strategy: ${config.triggerStrategy}")
                Log.i(TAG, "   ├─ trigger_clicks_count: ${config.triggerClicksCount}")
                Log.i(TAG, "   ├─ trigger_seconds_delay: ${config.triggerSecondsDelay}s")
                Log.i(TAG, "   ├─ show_on_first_open: ${config.showOnFirstOpen}")
                Log.i(TAG, "   └─ skip_duration_seconds: ${config.skipDurationSeconds}s")
            } else {
                Log.e(TAG, "❌ Config parsing GAGAL dari snapshot")
            }

            config
        } catch (e: Exception) {
            Log.e(TAG, "❌ getConfig() ERROR: ${e.message}", e)
            null
        }
    }

    /**
     * Mengambil daftar campaign aktif dari Firestore, sudah difilter berdasarkan:
     * 1. is_active == true (query Firestore)
     * 2. Jam saat ini berada dalam rentang schedule_start - schedule_end (filter lokal)
     *
     * Collection: "campaigns"
     */
    suspend fun getActiveCampaigns(): List<AdCampaign> {
        val firestore = db ?: run {
            Log.e(TAG, "❌ getActiveCampaigns() - Firestore instance NULL")
            return emptyList()
        }

        return try {
            Log.d(TAG, "📡 Fetching campaigns aktif dari collection 'campaigns'...")
            val snapshot = firestore.collection("campaigns")
                .whereEqualTo("is_active", true)
                .get()
                .await()

            Log.d(TAG, "📦 Jumlah document yang dikembalikan Firestore: ${snapshot.documents.size}")

            // Parse manual setiap document
            val allCampaigns = snapshot.documents.mapNotNull { doc ->
                val campaign = AdCampaign.fromSnapshot(doc)
                if (campaign != null) {
                    Log.d(TAG, "   ├─ Campaign '${campaign.adId}': type=${campaign.adType}, title=${campaign.title}, weight=${campaign.weight}")
                } else {
                    Log.w(TAG, "   ├─ ⚠️ Gagal parse document: ${doc.id}")
                }
                campaign
            }

            // Filter berdasarkan jam (abaikan tanggal)
            val now = Calendar.getInstance()
            val currentTime = "${now.get(Calendar.HOUR_OF_DAY)}:${String.format("%02d", now.get(Calendar.MINUTE))}"
            Log.d(TAG, "🕐 Waktu sekarang: $currentTime — Memfilter berdasarkan jadwal...")

            val eligibleCampaigns = allCampaigns.filter { campaign ->
                val eligible = campaign.isTimeEligible()
                if (!eligible) {
                    Log.d(TAG, "   ├─ ⏰ Campaign '${campaign.adId}' DILUAR jadwal → dilewati")
                } else {
                    Log.d(TAG, "   ├─ ✅ Campaign '${campaign.adId}' DALAM jadwal → lolos")
                }
                eligible
            }

            Log.i(TAG, "📊 Hasil: ${allCampaigns.size} total → ${eligibleCampaigns.size} lolos filter jadwal")

            eligibleCampaigns
        } catch (e: Exception) {
            Log.e(TAG, "❌ getActiveCampaigns() ERROR: ${e.message}", e)
            emptyList()
        }
    }
}