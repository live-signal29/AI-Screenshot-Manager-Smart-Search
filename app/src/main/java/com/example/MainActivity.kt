package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.ads.InterstitialAdManager
import com.example.ui.navigation.AppNavHost
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // When permission is granted, trigger initial screenshot indexing
        viewModel.startScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK on background thread
        MobileAds.initialize(this) {}
        InterstitialAdManager.loadAd(this)

        // Request appropriate media permission for screenshot detection
        checkAndRequestMediaPermissions()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                AppNavHost(viewModel = viewModel)
            }
        }
    }

    private fun checkAndRequestMediaPermissions() {
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permissionToRequest) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startScan()
        } else {
            requestPermissionLauncher.launch(permissionToRequest)
        }
    }
}
