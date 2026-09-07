package com.example.ui.screens.cleaner

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.ads.AdMobBanner
import com.example.ads.InterstitialAdManager
import com.example.data.local.ScreenshotEntity
import com.example.ui.components.CategoryBadge
import com.example.ui.components.MetricStatCard
import com.example.ui.components.formatBytes
import com.example.ui.components.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageCleanerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (id: Long) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val totalCount by viewModel.totalCount.collectAsState()
    val totalStorage by viewModel.totalStorageBytes.collectAsState()
    val duplicateCount by viewModel.duplicateCount.collectAsState()
    val duplicateStorage by viewModel.duplicateStorageBytes.collectAsState()
    val duplicateClusters by viewModel.duplicateClusters.collectAsState()
    val largeScreenshots by viewModel.largeScreenshots.collectAsState()
    val removableScreenshots by viewModel.removableScreenshots.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Duplicates", "Removable", "Large (>3MB)")

    val selectedIds = remember { mutableStateListOf<Long>() }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastFreedBytes by remember { mutableStateOf(0L) }

    val allDuplicateScreenshots = remember(duplicateClusters) {
        duplicateClusters.flatMap { it.duplicateScreenshots }
    }

    val currentItems = when (selectedTab) {
        0 -> allDuplicateScreenshots
        1 -> removableScreenshots
        else -> largeScreenshots
    }

    val selectedBytes = remember(selectedIds.size, currentItems) {
        currentItems.filter { it.id in selectedIds }.sumOf { it.size }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Storage Cleaner",
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
            Column {
                if (selectedIds.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${selectedIds.size} Selected",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Free up ${formatBytes(selectedBytes)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = { showConfirmDeleteDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clean Now")
                            }
                        }
                    }
                }
                AdMobBanner()
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Storage Stats Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Total Screenshot Storage",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatBytes(totalStorage),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Select Suggestions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recoverable: ${formatBytes(duplicateStorage)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFE11D48)
                            )
                            TextButton(
                                onClick = {
                                    selectedIds.clear()
                                    selectedIds.addAll(currentItems.map { it.id })
                                }
                            ) {
                                Text("Select All in Tab")
                            }
                        }
                    }
                }
            }

            // Cleaner Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                selectedIds.clear()
                            },
                            text = { Text(title, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // Tab Content
            if (currentItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Storage is Clean!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "No items found in this section.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "${currentItems.size} items • ${formatBytes(currentItems.sumOf { it.size })} total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                items(currentItems, key = { it.id }) { item ->
                    val isSelected = selectedIds.contains(item.id)
                    CleanerItemCard(
                        screenshot = item,
                        isSelected = isSelected,
                        onToggleSelect = {
                            if (isSelected) selectedIds.remove(item.id)
                            else selectedIds.add(item.id)
                        },
                        onClick = { onNavigateToDetail(item.id) }
                    )
                }
            }
        }
    }

    // Deletion Confirmation Dialog (Strict User Consent Protection)
    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Confirm Clean Storage") },
            text = {
                Text("Delete ${selectedIds.size} screenshots to free up ${formatBytes(selectedBytes)} of storage? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        lastFreedBytes = selectedBytes
                        viewModel.deleteScreenshots(selectedIds.toList())
                        selectedIds.clear()
                        showConfirmDeleteDialog = false
                        showSuccessDialog = true

                        // Trigger natural transition interstitial ad (policy compliant cooldown applied)
                        if (activity != null) {
                            InterstitialAdManager.showIfAvailable(activity)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Notice Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Storage Cleaned!") },
            text = {
                Text("Successfully freed ${formatBytes(lastFreedBytes)} of device storage.")
            },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun CleanerItemCard(
    screenshot: ScreenshotEntity,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clickable(onClick = onToggleSelect)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = screenshot.uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CategoryBadge(category = screenshot.category)
                    Text(
                        text = formatBytes(screenshot.size),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = screenshot.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatDate(screenshot.dateTaken),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
