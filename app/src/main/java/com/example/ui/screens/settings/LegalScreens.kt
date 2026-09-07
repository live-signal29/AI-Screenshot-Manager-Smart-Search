package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Privacy Policy for AI Screenshot Manager",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Effective Date: September 2026 • Developer: FX Signal Lab",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            LegalSection(
                title = "1. On-Device Processing Principle",
                content = "AI Screenshot Manager – Smart Search processes your screenshots, optical character recognition (OCR), entity extraction, and duplicate detection directly on your local device. We do not transmit your screenshots to any external server or cloud service without your explicit permission."
            )

            LegalSection(
                title = "2. Device Storage & Media Access",
                content = "The application requests READ_MEDIA_IMAGES permission solely to index your screenshot gallery, generate perceptual hashes for duplicate cleanup, and allow you to search text within your screenshots."
            )

            LegalSection(
                title = "3. Optional Cloud AI Integration",
                content = "If you choose to enable the optional Cloud AI Assistant or connect a custom Gemini API key, only the text snippets or queries you explicitly submit are sent to the AI endpoint. No photos or media are transferred in default offline mode."
            )

            LegalSection(
                title = "4. Advertising (AdMob)",
                content = "This app displays non-intrusive banner and occasional interstitial ads served by Google AdMob. AdMob may collect pseudonymous advertising identifiers in compliance with Google Play Developer Program Policies."
            )

            LegalSection(
                title = "5. Data Deletion & Control",
                content = "You maintain complete ownership of your data. You can delete screenshots from your device or reset the local application database at any time from the Settings screen."
            )

            LegalSection(
                title = "6. Contact Us",
                content = "For questions or concerns regarding our privacy practices, contact FX Signal Lab at support@fxsignallab.com."
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfUseScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms of Use", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Terms of Use",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "FX Signal Lab • Version 1.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            LegalSection(
                title = "1. Acceptance of Terms",
                content = "By downloading or using AI Screenshot Manager – Smart Search, you agree to comply with and be bound by these Terms of Use."
            )

            LegalSection(
                title = "2. License & Intellectual Property",
                content = "FX Signal Lab grants you a personal, non-exclusive, non-transferable license to use the app for personal productivity and screenshot management."
            )

            LegalSection(
                title = "3. Storage Deletion Disclaimer",
                content = "The storage cleaner allows you to delete duplicate or unwanted screenshots. Deletion actions require your explicit confirmation in the dialog. The user remains solely responsible for verifying items prior to confirming permanent deletion."
            )

            LegalSection(
                title = "4. Governing Law",
                content = "These Terms are governed by applicable laws and Google Play Developer Program Policies."
            )
        }
    }
}

@Composable
fun LegalSection(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
