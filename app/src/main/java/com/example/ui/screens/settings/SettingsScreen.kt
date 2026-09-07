package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ads.AdMobBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val isAutoScan by viewModel.isAutoScanEnabled.collectAsState()
    val isOcrEnabled by viewModel.isOcrEnabled.collectAsState()
    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val aiApiKey by viewModel.aiApiKey.collectAsState()
    val aiEndpoint by viewModel.aiApiEndpoint.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var tempApiKey by remember { mutableStateOf("") }
    var tempEndpoint by remember { mutableStateOf("") }

    var showThemeMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            AdMobBanner()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            SettingsSectionHeader(title = "Appearance")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column {
                    SettingsRow(
                        title = "Theme Mode",
                        subtitle = themeMode.replaceFirstChar { it.uppercase() },
                        icon = Icons.Default.Brightness4,
                        onClick = { showThemeMenu = true }
                    )
                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        listOf("system", "light", "dark").forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Scanning & OCR Section
            SettingsSectionHeader(title = "Scanning & Processing")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column {
                    SettingsToggleRow(
                        title = "Auto-Scan Screenshots",
                        subtitle = "Automatically index new screenshots upon app launch",
                        checked = isAutoScan,
                        onCheckedChange = { viewModel.setAutoScan(it) }
                    )
                    SettingsToggleRow(
                        title = "On-Device OCR",
                        subtitle = "Extract text, prices, and dates locally using ML Kit",
                        checked = isOcrEnabled,
                        onCheckedChange = { viewModel.setOcr(it) }
                    )
                }
            }

            // AI Assistant Section
            SettingsSectionHeader(title = "AI Assistant (Optional)")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column {
                    SettingsToggleRow(
                        title = "Enable Cloud AI Assistant",
                        subtitle = "Use Gemini or custom AI proxy for advanced queries",
                        checked = isAiEnabled,
                        onCheckedChange = { viewModel.setAiEnabled(it) }
                    )
                    if (isAiEnabled) {
                        SettingsRow(
                            title = "AI API Configuration",
                            subtitle = if (aiApiKey.isNotBlank()) "Configured" else "Tap to configure key/endpoint",
                            icon = Icons.Default.AutoAwesome,
                            onClick = {
                                tempApiKey = aiApiKey
                                tempEndpoint = aiEndpoint
                                showApiKeyDialog = true
                            }
                        )
                    }
                }
            }

            // Privacy & Data Controls
            SettingsSectionHeader(title = "Privacy & Data")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column {
                    SettingsRow(
                        title = "Privacy Policy",
                        subtitle = "On-device processing & data protection",
                        icon = Icons.Default.Security,
                        onClick = onNavigateToPrivacy
                    )
                    SettingsRow(
                        title = "Terms of Use",
                        subtitle = "Usage terms and licensing",
                        icon = Icons.Default.Policy,
                        onClick = onNavigateToTerms
                    )
                    SettingsRow(
                        title = "Clear Local App Database",
                        subtitle = "Remove local index and cache",
                        icon = Icons.Default.DeleteForever,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { showResetDialog = true }
                    )
                }
            }

            // About Section
            SettingsSectionHeader(title = "About")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column {
                    SettingsRow(
                        title = "AI Screenshot Manager – Smart Search",
                        subtitle = "Version 1.0.0 • Developed by FX Signal Lab",
                        icon = Icons.Default.Info,
                        onClick = {}
                    )
                    SettingsRow(
                        title = "Contact Support",
                        subtitle = "support@fxsignallab.com",
                        icon = Icons.Default.Email,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@fxsignallab.com"))
                            context.startActivity(intent)
                        }
                    )
                    SettingsRow(
                        title = "Share Application",
                        subtitle = "Recommend to colleagues & friends",
                        icon = Icons.Default.Share,
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out AI Screenshot Manager – Smart Search by FX Signal Lab! https://fxsignallab.com"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share App"))
                        }
                    )
                }
            }
        }
    }

    // AI API Config Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Configure AI Assistant") },
            text = {
                Column {
                    Text(
                        text = "For security and privacy, API keys are stored locally on your device and are never transmitted to third parties.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text("Gemini / Provider API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempEndpoint,
                        onValueChange = { tempEndpoint = it },
                        label = { Text("Custom Backend Endpoint (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setAiApiKey(tempApiKey)
                    viewModel.setAiApiEndpoint(tempEndpoint)
                    showApiKeyDialog = false
                    Toast.makeText(context, "AI settings saved", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Database Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Local Database?") },
            text = { Text("This will clear the local screenshot index and OCR data. Your original photos in your gallery will NOT be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                        Toast.makeText(context, "Database cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
