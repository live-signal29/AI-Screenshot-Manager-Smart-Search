package com.example.ui.screens.home

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.ads.AdMobBanner
import com.example.data.local.ScreenshotEntity
import com.example.domain.ocr.CategoryClassifier
import com.example.ui.components.CategoryBadge
import com.example.ui.components.MetricStatCard
import com.example.ui.components.formatBytes
import com.example.ui.components.formatDate
import com.example.ui.components.getCategoryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToGallery: (category: String?) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToCleaner: () -> Unit,
    onNavigateToDetail: (id: Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val totalCount by viewModel.totalCount.collectAsState()
    val totalStorage by viewModel.totalStorageBytes.collectAsState()
    val duplicateCount by viewModel.duplicateCount.collectAsState()
    val duplicateStorage by viewModel.duplicateStorageBytes.collectAsState()
    val allScreenshots by viewModel.allScreenshots.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()

    val primaryCategories = listOf(
        CategoryClassifier.CATEGORY_RECEIPTS,
        CategoryClassifier.CATEGORY_SHOPPING,
        CategoryClassifier.CATEGORY_TICKETS,
        CategoryClassifier.CATEGORY_TRAVEL,
        CategoryClassifier.CATEGORY_STUDY,
        CategoryClassifier.CATEGORY_NOTES,
        CategoryClassifier.CATEGORY_DOCUMENTS,
        CategoryClassifier.CATEGORY_SOCIAL,
        CategoryClassifier.CATEGORY_OTHER
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Screenshot Manager",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "By FX Signal Lab • On-Device Privacy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startScan() }) {
                        if (scanProgress.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan MediaStore",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Scan progress indicator banner
            item {
                AnimatedVisibility(visible = scanProgress.isScanning) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = scanProgress.currentStepText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (scanProgress.totalItems > 0) {
                                    Text(
                                        text = "${scanProgress.currentItem}/${scanProgress.totalItems}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (scanProgress.totalItems > 0) {
                                        scanProgress.currentItem.toFloat() / scanProgress.totalItems.toFloat()
                                    } else 0.5f
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            // 4 Main Action Cards
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MainActionCard(
                            title = "Smart Search",
                            subtitle = "Natural Language AI",
                            icon = Icons.Default.Search,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSearch
                        )
                        MainActionCard(
                            title = "All Screenshots",
                            subtitle = "$totalCount Items",
                            icon = Icons.Default.PhotoLibrary,
                            iconTint = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToGallery(null) }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MainActionCard(
                            title = "Categories",
                            subtitle = "Smart Grouping",
                            icon = Icons.Default.FolderSpecial,
                            iconTint = Color(0xFF059669),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCategories
                        )
                        MainActionCard(
                            title = "Clean Storage",
                            subtitle = formatBytes(duplicateStorage),
                            icon = Icons.Default.CleaningServices,
                            iconTint = Color(0xFFE11D48),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCleaner
                        )
                    }
                }
            }

            // Overview Metric Stats Row
            item {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    Text(
                        text = "Library Overview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Total Storage",
                            value = formatBytes(totalStorage),
                            subtitle = "$totalCount items",
                            icon = Icons.Default.Storage,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToGallery(null) }
                        )
                        MetricStatCard(
                            title = "Duplicates",
                            value = duplicateCount.toString(),
                            subtitle = formatBytes(duplicateStorage),
                            icon = Icons.Default.DeleteSweep,
                            iconTint = Color(0xFFE11D48),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCleaner
                        )
                        MetricStatCard(
                            title = "Favorites",
                            value = favorites.size.toString(),
                            subtitle = "Starred",
                            icon = Icons.Default.Star,
                            iconTint = Color(0xFFD97706),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToGallery("Favorites") }
                        )
                    }
                }
            }

            // Category Shortcuts
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToCategories() }
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(primaryCategories) { cat ->
                            CategoryShortcutChip(
                                category = cat,
                                onClick = { onNavigateToGallery(cat) }
                            )
                        }
                    }
                }
            }

            // Recently Added Screenshots
            item {
                Column(modifier = Modifier.padding(top = 22.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Added",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "View All ($totalCount)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToGallery(null) }
                        )
                    }

                    if (allScreenshots.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No screenshots found yet. Tap Refresh to scan storage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(allScreenshots.take(8), key = { it.id }) { item ->
                                RecentScreenshotCard(
                                    screenshot = item,
                                    onClick = { onNavigateToDetail(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryShortcutChip(
    category: String,
    onClick: () -> Unit
) {
    val color = getCategoryColor(category)
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RecentScreenshotCard(
    screenshot: ScreenshotEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = screenshot.uri,
                    contentDescription = screenshot.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (screenshot.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                CategoryBadge(category = screenshot.category)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = screenshot.displayName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatDate(screenshot.dateTaken),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
