package com.azrxtech.hitunguntung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azrxtech.hitunguntung.ui.SplashScreen
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme
import com.azrxtech.hitunguntung.ui.home.HomeScreen
import com.azrxtech.hitunguntung.ui.calculator.KulakanScreen
import com.azrxtech.hitunguntung.ui.calculator.MarginScreen
import com.azrxtech.hitunguntung.ui.calculator.DiskonScreen
import com.azrxtech.hitunguntung.ui.calculator.KembalianScreen
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    100
                )
            }
        }
        
        enableEdgeToEdge()

        setContent {
            HitungUntungTheme {
                // Surface berfungsi sebagai background dasar layar
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Inisialisasi NavController
                    val navController = rememberNavController()

                    // NavHost mengatur semua rute layar aplikasi
                    NavHost(navController = navController, startDestination = "splash") {

                        // 1. Rute Splash Screen
                        composable("splash") {
                            SplashScreen(
                                onSplashFinished = {
                                    // Pindah ke "home" dan hapus "splash" dari riwayat backstack
                                    // agar kalau user tekan tombol back, tidak kembali ke splash
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Rute Home Screen
                        composable("home") {
                            // HomeScreen perlu menerima fungsi navigasi ini (lihat instruksi di bawah)
                            HomeScreen(
                                onNavigateToKulakan = { navController.navigate("kulakan") },
                                onNavigateToMargin = { navController.navigate("margin") },
                                onNavigateToDiskon = { navController.navigate("diskon") },
                                onNavigateToKembalian = { navController.navigate("kembalian") }
                            )
                        }

                        // 3. Rute Kalkulator
                        composable("kulakan") {
                            KulakanScreen()
                        }
                        composable("margin") {
                            MarginScreen()
                        }
                        composable("diskon") {
                            DiskonScreen()
                        }
                        composable("kembalian") {
                            KembalianScreen()
                        }
                    }
                }
            }
        }
    }
}