package com.azrxtech.hitunguntung.customeads.view

import android.content.Intent
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenInNew
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
import com.azrxtech.hitunguntung.customeads.manager.AdManager
import com.azrxtech.hitunguntung.customeads.model.AdCampaign
import kotlinx.coroutines.delay
import androidx.core.net.toUri
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme

private const val TAG = "CustomAds.Interstitial"

/**
 * Layar iklan interstitial full-screen.
 *
 * Behavior berdasarkan ad_type:
 * - "image": Tampilkan gambar. Klik area gambar → buka target_url. Countdown → skip.
 * - "video": Tampilkan video. Klik area video → buka target_url. Countdown → skip.
 * - "webview": INTERAKTIF tanpa batas waktu. Countdown tetap berjalan tapi TIDAK auto-close.
 *   Setelah countdown habis: tombol Skip + tombol CTA (button_text) muncul berdampingan.
 */
@Composable
fun InterstitialAdScreen(
    campaign: AdCampaign,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isWebView = campaign.adType == "webview"

    // Ambil delay dari konfigurasi (default 3 detik jika gagal fetch)
    val skipDelaySeconds = AdManager.config?.skipDurationSeconds ?: 3
    var countdown by remember { mutableIntStateOf(skipDelaySeconds) }
    var canSkip by remember { mutableStateOf(false) }

    Log.i(TAG, "📺 InterstitialAdScreen tampil: type=${campaign.adType}, title=${campaign.title}")
    Log.d(TAG, "   ├─ skipDelay=${skipDelaySeconds}s, isWebView=$isWebView")

    // Efek hitung mundur
    LaunchedEffect(key1 = Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        canSkip = true
        Log.d(TAG, "⏰ Countdown selesai → canSkip=true")
    }

    // Fungsi buka target URL
    val openTargetUrl: () -> Unit = {
        try {
            val uri = campaign.targetUrl.toUri()
            Log.i(TAG, "🔗 Membuka target: ${campaign.targetUrl} (mode=${campaign.openTargetIn})")
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

    // Dialog full-screen overlay
    Dialog(
        onDismissRequest = { if (canSkip) onClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = canSkip
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // ===== KONTEN IKLAN =====
            when (campaign.adType) {
                "image" -> {
                    // Klik area gambar → buka target_url
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { openTargetUrl() }
                    ) {
                        AdImageComponent(mediaUrl = campaign.mediaUrl)
                    }
                }
                "video" -> {
                    // Klik area video → buka target_url
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { openTargetUrl() }
                    ) {
                        AdVideoComponent(mediaUrl = campaign.mediaUrl)
                    }
                }
                "webview" -> {
                    // WebView interaktif - user bisa scroll, klik link di dalam webview
                    // Padding bottom untuk memberi ruang tombol-tombol
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp) // Ruang untuk tombol bawah
                    ) {
                        AdWebViewComponent(mediaUrl = campaign.mediaUrl)
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { openTargetUrl() }
                    ) {
                        AdImageComponent(mediaUrl = campaign.mediaUrl)
                    }
                }
            }

            // ===== TOMBOL SKIP & COUNTDOWN (Pojok Kanan Atas) =====
            if (!isWebView) {
                // Image/Video: Tombol skip standar di pojok kanan atas
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (canSkip) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    Log.i(TAG, "⏭️ User menekan Skip (image/video)")
                                    onClose()
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(text = "Lewati", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Text(
                            text = "Iklan ($countdown)",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                // Image/Video: Tombol CTA di bawah
                Button(
                    onClick = openTargetUrl,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    Text(
                        text = campaign.buttonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // ===== WEBVIEW SPECIAL: Countdown di atas, lalu tombol skip + CTA berdampingan di bawah =====

                // Countdown badge di pojok kanan atas (saat countdown belum habis)
                if (!canSkip) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Iklan ($countdown)",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                // Setelah countdown habis: Tombol Skip + CTA berdampingan di bawah
                if (canSkip) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Skip (outline style)
                        OutlinedButton(
                            onClick = {
                                Log.i(TAG, "⏭️ User menekan Skip (webview)")
                                onClose()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Skip", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lewati",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Tombol CTA (filled, primary)
                        Button(
                            onClick = openTargetUrl,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = "Open", modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = campaign.buttonText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdPrev() {
    HitungUntungTheme{
        InterstitialAdScreen(
            campaign = AdCampaign(
                adType = "webview",
                mediaUrl = "https://www.example.com"
            ),
            onClose = {}
        )
    }
}