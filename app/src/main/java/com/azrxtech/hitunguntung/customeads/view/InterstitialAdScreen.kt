package com.azrxtech.hitunguntung.customeads.view

import android.content.Intent
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler // Tambahkan import ini
import com.azrxtech.hitunguntung.customads.view.AdWebViewComponent
import com.azrxtech.hitunguntung.customeads.manager.AdManager
import com.azrxtech.hitunguntung.customeads.model.AdCampaign
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme
import kotlinx.coroutines.delay

private const val TAG = "CustomAds.Interstitial"

@Composable
fun InterstitialAdScreen(
    campaign: AdCampaign,
    onClose: () -> Unit
) {
    // Dialog overlay dengan proteksi ketat
    Dialog(
        onDismissRequest = { /* Kosongkan agar back press dari luar tidak bekerja otomatis menutup */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            // UBAH JADI TRUE: Agar BackHandler di dalam konten bisa menangkap event-nya
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        // Panggil konten UI yang terpisah agar bisa di-Preview dengan mudah
        InterstitialAdContent(campaign = campaign, onClose = onClose)
    }
}

/**
 * Konten UI dipisah dari Dialog agar anotasi @Preview dapat merender tampilan dengan baik
 * di dalam Android Studio.
 */
@Composable
private fun InterstitialAdContent(
    campaign: AdCampaign,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isWebView = campaign.adType == "webview"

    // Tahan tombol back bawaan HP agar tidak bisa menutup iklan (kecuali lewat X)
    // Untuk WebView, kita tidak pasang di sini, karena akan ditangani oleh AdWebViewComponent langsung
    if (!isWebView) {
        BackHandler(enabled = true) {
            Log.i(TAG, "Tombol back ditekan, tapi diblokir oleh iklan.")
            // Do nothing: Iklan tidak akan tertutup
        }
    }

    // Ambil delay dari konfigurasi (default 5 detik jika gagal fetch)
    val skipDelaySeconds = AdManager.config?.skipDurationSeconds ?: 5
    var countdown by remember { mutableIntStateOf(skipDelaySeconds) }
    var canSkip by remember { mutableStateOf(false) }

    // Efek hitung mundur
    LaunchedEffect(key1 = Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        canSkip = true
    }

    // Fungsi buka target URL (Link Tujuan)
    val openTargetUrl: () -> Unit = {
        try {
            val uri = campaign.targetUrl.toUri()
            Log.i(TAG, "🔗 Membuka target: ${campaign.targetUrl}")
            if (campaign.openTargetIn == "internal") {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(context, uri)
            } else {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Gagal buka URL: ${e.message}", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ===== TOP BAR (Mirip UI AdMob) =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.Black), // Background solid black seperti screenshot Anda
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Kiri: Teks "Ads"
            Text(
                text = "Ads",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 6.dp)
            )

            // Kanan: Countdown ATAU Tombol Open & X
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                if (!canSkip) {
                    Text(
                        text = "Close in $countdown",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                } else {
                    // Jika Webview, munculkan teks "Open" di samping X secara rapat
                    if (isWebView) {
                        Text(
                            text = campaign.buttonText,
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .clickable { openTargetUrl() }
                                .padding(horizontal = 8.dp, vertical = 4.dp) // Hitbox lumayan besar agar sering tak sengaja terpencet
                        )
                    }

                    // Tombol X (Close)
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Ad",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp) // Ukuran proporsional
                            .clickable { onClose() }
                            .padding(8.dp)
                    )
                }
            }
        }

        // ===== KONTEN IKLAN =====
        Box(modifier = Modifier
            .fillMaxSize()
            .weight(1f)) {
            when (campaign.adType) {
                "image" -> {
                    // Image: Seluruh area bisa diklik menuju link
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { openTargetUrl() }
                    ) {
                        AdImageComponent(mediaUrl = campaign.mediaUrl)
                    }
                }
                "video" -> {
                    // Video: Seluruh area bisa diklik menuju link
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { openTargetUrl() }
                    ) {
                        AdVideoComponent(mediaUrl = campaign.mediaUrl)
                    }
                }
                "webview" -> {
                    // WebView: Area scroll interaktif murni (klik ditangani dari dalam WebView sendiri)
                    Box(modifier = Modifier.fillMaxSize()) {
                        AdWebViewComponent(mediaUrl = campaign.mediaUrl)
                    }
                }
                else -> {
                    // Fallback
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { openTargetUrl() }
                    ) {
                        AdImageComponent(mediaUrl = campaign.mediaUrl)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "1. WebView (Can Skip)")
@Composable
private fun InterstitialWebViewPreview() {
    HitungUntungTheme {
        InterstitialAdContent(
            campaign = AdCampaign(
                adType = "webview",
                mediaUrl = "https://azrxr.my.id",
                targetUrl = "https://azrxr.my.id"
            ),
            onClose = {}
        )
    }
}

@Preview(showBackground = true, name = "2. Image Ad (Full Clickable)")
@Composable
private fun InterstitialImagePreview() {
    HitungUntungTheme {
        InterstitialAdContent(
            campaign = AdCampaign(
                adType = "image",
                mediaUrl = "https://via.placeholder.com/400x800",
                targetUrl = "https://azrxr.my.id"
            ),
            onClose = {}
        )
    }
}