package com.azrxtech.hitunguntung.customeads.view

import android.content.Intent
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.azrxtech.hitunguntung.customeads.manager.AdManager
import com.azrxtech.hitunguntung.customeads.model.AdCampaign
import com.azrxtech.hitunguntung.eventads.EventAdsManager
import kotlinx.coroutines.delay

private const val TAG = "CustomAds.Interstitial"

@Composable
fun InterstitialAdScreen(
    campaign: AdCampaign,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var activeInAppUrl by remember { mutableStateOf<String?>(null) }
    val isWebView = campaign.adType == "webview"

    // Tangani tombol back perangkat keras di level root Compose
    BackHandler(enabled = true) {
        if (activeInAppUrl != null) {
            activeInAppUrl = null
            onClose()
        } else if (!isWebView) {
            Log.i(TAG, "Tombol back ditekan, tetapi diblokir oleh iklan.")
        }
    }

    val skipDelaySeconds = AdManager.config?.skipDurationSeconds ?: 3
    var countdown by remember { mutableIntStateOf(skipDelaySeconds) }
    var canSkip by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        canSkip = true
    }

    // Fungsi pusat untuk membuka target URL
    val openTargetUrl: () -> Unit = {
        try {
            val uri = campaign.targetUrl.toUri()
            Log.i(TAG, "🔗 Membuka target: ${campaign.targetUrl} (mode=${campaign.openTargetIn})")

            // Send event to EventAdsManager
            val params = mapOf(
                "campaign_id" to campaign.adId,
                "title" to campaign.title,
                "ad_type" to campaign.adType,
                "target_url" to campaign.targetUrl,
                "open_target_in" to campaign.openTargetIn
            )
            EventAdsManager.logEvent("ad_clicked", params)
            EventAdsManager.logAdClick(
                campaignId = campaign.adId,
                adType = campaign.adType,
                targetUrl = campaign.targetUrl
            )

            if (campaign.openTargetIn == "internal") {
                // Tampilkan overlay webview native internal kita sendiri
                activeInAppUrl = campaign.targetUrl
            } else {
                // Buka lewat browser luar
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Gagal buka URL: ${e.message}", e)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        if (activeInAppUrl != null) {
            // ===== IN-APP WEBVIEW OVERLAY =====
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            webViewClient = WebViewClient()
                            loadUrl(activeInAppUrl!!)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Floating close button (X) di pojok kanan atas
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = {
                            activeInAppUrl = null
                            onClose()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close WebView",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        } else {
            // ===== STANDARD AD SCREEN (Edge-to-Edge Fullscreen) =====
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // ----- TOP BAR -----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ads",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!canSkip) {
                            Text(
                                text = "Close in $countdown",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        } else {
                            Button(
                                onClick = { openTargetUrl() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = campaign.buttonText.ifEmpty { "Buka" },
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Ad",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { onClose() }
                                    .padding(2.dp)
                            )
                        }
                    }
                }

                // ----- KONTEN IKLAN -----
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (campaign.adType) {
                        "image" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { openTargetUrl() }
                            ) {
                                AdImageComponent(mediaUrl = campaign.mediaUrl)
                            }
                        }
                        "video" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { openTargetUrl() }
                            ) {
                                AdVideoComponent(mediaUrl = campaign.mediaUrl)
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent)
                                )
                            }
                        }
                        "webview" -> {
                            Box(modifier = Modifier.fillMaxSize()) {
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
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "WebView Ad Preview")
@Composable
private fun InterstitialWebViewPreview() {
    MaterialTheme {
        InterstitialAdScreen(
            campaign = AdCampaign(
                adType = "webview",
                mediaUrl = "https://example.com",
                targetUrl = "https://example.com"
            ),
            onClose = {}
        )
    }
}