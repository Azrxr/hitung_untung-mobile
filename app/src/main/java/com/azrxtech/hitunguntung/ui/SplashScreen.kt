package com.azrxtech.hitunguntung.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.azrxtech.hitunguntung.R

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit // Callback saat animasi selesai untuk pindah ke Home
) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "1.0.0"
    val appName = stringResource(id = R.string.app_name)

    // State untuk Animasi
    val scaleAnimation = remember { Animatable(0.5f) } // Mulai dari ukuran setengah
    val alphaAnimation = remember { Animatable(0f) }   // Mulai dari transparan (tidak terlihat)

    // Warna Background Utama (Dark Green)
    val splashBackground = Color(0xFF004D40)

    // Warna Lingkaran Ikon (Lebih terang sedikit dari background)
    val iconBackground = Color.White.copy(alpha = 0.15f)

    LaunchedEffect(key1 = true) {
        // Menjalankan animasi Scale dan Alpha secara bersamaan (Parallel)
        launch {
            scaleAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    // Memberikan efek membal sedikit di akhir animasi
                    easing = { overshootEasing(it) }
                )
            )
        }
        launch {
            alphaAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }

        // Tahan layar splash selama 1.5 detik setelah animasi selesai
        delay(1500)

        // Beri tahu activity/navigation untuk pindah ke HomeScreen
        onSplashFinished()
    }

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(splashBackground)
    ) {
        // Konten Utama di Tengah (Logo & Judul)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                // Terapkan animasi pada kolom utama ini
                .scale(scaleAnimation.value)
                .alpha(alphaAnimation.value)
        ) {
            // Box Lingkaran Ikon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(iconBackground)
            ) {
                Image(
                    painter = painterResource(id = com.azrxtech.hitunguntung.R.drawable.ic_brand),
                    contentDescription = "Logo Aplikasi",
                    Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Teks Judul
            Text(
                text = appName,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }

        // Konten Footer di Bawah (Tagline & Versi)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alphaAnimation.value) // Teks bawah ikut efek fade-in
        ) {
            Text(
                text = "PRESISI DALAM SETIAP TRANSAKSI.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp // Memberikan spasi antar huruf agar elegan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Teks Versi Aplikasi
            Text(
                text = "Versi $versionName",
                color = Color.White.copy(alpha = 0.4f), // Lebih redup dari tagline
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

private fun overshootEasing(fraction: Float): Float {
    val tension = 1.2f // Semakin tinggi semakin membal
    val t = fraction - 1f
    return t * t * ((tension + 1f) * t + tension) + 1f
}

// Preview untuk melihat hasil di Android Studio tanpa perlu run ke HP
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen(onSplashFinished = {})
    }
}