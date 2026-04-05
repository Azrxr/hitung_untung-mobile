package com.azrxtech.hitunguntung

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azrxtech.hitunguntung.customeads.manager.AdManager
import com.azrxtech.hitunguntung.customeads.view.InterstitialAdScreen
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
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CustomAds.Main"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        AdManager.initialize()

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

                val currentAd by AdManager.currentAd.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {

                        // 1. Rute Splash Screen
                        composable("splash") {
                            SplashScreen(
                                onSplashFinished = {
                                    Log.i(TAG, "🎬 Splash selesai → navigasi ke home + trigger onSplashFinished()")
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                    // Trigger logika iklan setelah splash selesai
                                    AdManager.onSplashFinished()
                                }
                            )
                        }

                        // 2. Rute Home Screen
                        composable("home") {
                            HomeScreen(
                                onNavigateToKulakan = {
                                    AdManager.registerClick()
                                    navController.navigate("kulakan")
                                },
                                onNavigateToMargin = {
                                    AdManager.registerClick()
                                    navController.navigate("margin")
                                },
                                onNavigateToDiskon = {
                                    AdManager.registerClick()
                                    navController.navigate("diskon")
                                },
                                onNavigateToKembalian = {
                                    AdManager.registerClick()
                                    navController.navigate("kembalian")
                                }
                            )
                        }

                        // 3. Rute Kalkulator
                        composable("kulakan") {
                            KulakanScreen(
                                onBackClick = {
                                    AdManager.registerClick()
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("margin") {
                            MarginScreen(
                                onBackClick = {
                                    AdManager.registerClick()
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("diskon") {
                            DiskonScreen(
                                onBackClick = {
                                    AdManager.registerClick()
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("kembalian") {
                            KembalianScreen(
                                onBackClick = {
                                    AdManager.registerClick()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    // Iklan overlay di atas semua layar
                    currentAd?.let { campaign ->
                        InterstitialAdScreen(
                            campaign = campaign,
                            onClose = { AdManager.closeAd() }
                        )
                    }
                }
            }
        }
    }
}