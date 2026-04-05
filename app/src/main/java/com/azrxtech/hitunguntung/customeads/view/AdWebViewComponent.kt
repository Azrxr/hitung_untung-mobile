package com.azrxtech.hitunguntung.customads.view

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
// Ganti import theme ini dengan lokasi theme project Anda jika berbeda
// import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme

/**
 * Komponen WebView untuk iklan interaktif.
 * User bisa scroll dan klik link di dalam webview.
 * Dilengkapi loading indicator dan penanganan tombol Back.
 */
@Composable
fun AdWebViewComponent(mediaUrl: String) {
    val isLoading = remember { mutableStateOf(true) }

    // 1. Simpan instance WebView agar bisa kita kontrol dari luar AndroidView
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // 2. Pasang BackHandler untuk memanipulasi tombol Back bawaan HP
    BackHandler(enabled = true) {
        if (webViewInstance?.canGoBack() == true) {
            // Jika web punya histori, mundur 1 halaman web
            webViewInstance?.goBack()
        } else {
            // Jika sudah mentok di halaman pertama, abaikan pencetan tombol back.
            // Iklan TETAP TIDAK AKAN TERTUTUP. Pengguna wajib tekan X.
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading.value = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading.value = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            isLoading.value = false
                        }
                    }

                    loadUrl(mediaUrl)
                }.also {
                    // Simpan referensinya ke state di atas
                    webViewInstance = it
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                // 3. KOSONGKAN BLOK INI
                // Jangan memanggil webView.loadUrl() di sini untuk menghindari infinite refresh loop
            }
        )

        // Loading indicator overlay
        if (isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}