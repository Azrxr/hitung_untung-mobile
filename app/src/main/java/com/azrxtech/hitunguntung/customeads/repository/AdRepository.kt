package com.azrxtech.hitunguntung.customeads.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.azrxtech.hitunguntung.customeads.model.AdCampaign
import com.azrxtech.hitunguntung.customeads.model.AdConfig
import com.azrxtech.hitunguntung.customeads.model.ActiveCampaignConfig
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar
import kotlin.random.Random

/**
 * Repository untuk mengambil data iklan dari GitHub CDN.
 */
class AdRepository {

    companion object {
        private const val TAG = "CustomAds.Repository"
        private const val CONFIG_URL = "https://raw.githubusercontent.com/Azrxr/app-config-hub/main/com.podlax.kalkuwarung/config.json"

        // Cache parameters (static/singleton across instances)
        private var cachedConfig: AdConfig? = null
        private var lastFetchTime: Long = 0
    }

    private fun isDebugMode(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun httpGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.setRequestProperty("Accept", "application/json")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                val responseStr = response.toString()
                Log.d(TAG, "📡 HTTP GET Success! URL: $urlString, Response Length: ${responseStr.length}")
                Log.v(TAG, "📡 Response Content: $responseStr")
                responseStr
            } else {
                Log.e(TAG, "❌ HTTP GET Error: $responseCode untuk URL $urlString")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ HTTP GET Exception: ${e.message} untuk URL $urlString", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Mengambil konfigurasi iklan dari GitHub CDN untuk aplikasi ini.
     */
    suspend fun getConfig(context: Context): AdConfig? {
        val debug = isDebugMode(context)
        val cacheDuration = 10 * 60 * 1000L // 10 menit

        if (!debug && cachedConfig != null && (System.currentTimeMillis() - lastFetchTime) < cacheDuration) {
            Log.d(TAG, "📦 Using cached ad config (Release Mode, cache age: ${(System.currentTimeMillis() - lastFetchTime) / 1000}s)")
            return cachedConfig
        }

        if (debug) {
            Log.d(TAG, "📡 Debug Mode: Bypassing cache to fetch fresh config...")
        } else {
            Log.d(TAG, "📡 Cache expired or not set: Fetching fresh config...")
        }

        return try {
            val response = httpGet(CONFIG_URL)
            if (response == null) {
                Log.w(TAG, "⚠️ getConfig() - Response NULL, falling back to cache if available")
                return cachedConfig
            }
            val config = AdConfig.fromJson(response)
            if (config != null) {
                cachedConfig = config
                lastFetchTime = System.currentTimeMillis()
                Log.i(TAG, "✅ Config berhasil dimuat dari GitHub: is_ads_enabled=${config.isAdsEnabled}, active_campaigns=${config.activeCampaigns.size}")
            }
            config ?: cachedConfig
        } catch (e: Exception) {
            Log.e(TAG, "❌ getConfig() ERROR: ${e.message}", e)
            cachedConfig
        }
    }

    /**
     * Memilih campaign berdasarkan filter waktu dan sistem pembobotan (gacha)
     * dari konfigurasi yang sudah dimuat.
     */
    suspend fun fetchSelectedCampaign(config: AdConfig): AdCampaign? {
        try {
            // 1. Dapatkan jam saat ini ("HH:mm")
            val now = Calendar.getInstance()
            val currentHourStr = "${String.format("%02d", now.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", now.get(Calendar.MINUTE))}"
            Log.d(TAG, "🕐 Waktu sekarang: $currentHourStr")

            // 2. Filter campaigns yang aktif dan berada di dalam jadwal
            val validCampaigns = config.activeCampaigns.filter { camp ->
                if (!camp.isActive) return@filter false

                val start = if (camp.scheduleStart.isNotEmpty()) camp.scheduleStart else "00:00"
                val end = if (camp.scheduleEnd.isNotEmpty()) camp.scheduleEnd else "23:59"

                if (start <= end) {
                    currentHourStr >= start && currentHourStr <= end
                } else {
                    currentHourStr >= start || currentHourStr <= end
                }
            }

            if (validCampaigns.isEmpty()) {
                Log.i(TAG, "⏭️ Tidak ada campaign yang memenuhi syarat jadwal waktu")
                return null
            }

            Log.d(TAG, "📊 Ditemukan ${validCampaigns.size} campaign valid untuk jadwal saat ini")

            // 3. Sistem Pembobotan (Gacha berdasarkan Weight)
            val totalWeight = validCampaigns.sumOf { it.weight }
            val selected = if (totalWeight <= 0) {
                Log.d(TAG, "🎲 Total weight=0, memilih random")
                validCampaigns.random()
            } else {
                val randomNum = Random.nextDouble() * totalWeight
                Log.d(TAG, "🎲 Gacha: randomNum=$randomNum dari totalWeight=$totalWeight")

                var sum = 0.0
                var chosen: ActiveCampaignConfig? = null
                for (camp in validCampaigns) {
                    sum += camp.weight
                    if (randomNum < sum) {
                        chosen = camp
                        break
                    }
                }
                chosen ?: validCampaigns.last()
            }

            Log.i(TAG, "🎯 Terpilih campaign: ${selected.campaignId} (weight=${selected.weight})")

            // 4. Map direct campaign details to AdCampaign
            return AdCampaign(
                adId = selected.campaignId,
                adType = selected.adType,
                title = selected.title,
                mediaUrl = selected.mediaUrl,
                targetUrl = selected.targetUrl,
                buttonText = selected.buttonText,
                weight = selected.weight,
                openTargetIn = selected.openTargetIn
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ fetchSelectedCampaign() ERROR: ${e.message}", e)
            return null
        }
    }
}