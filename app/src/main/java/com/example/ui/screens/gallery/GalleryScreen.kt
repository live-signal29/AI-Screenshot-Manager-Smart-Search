package com.example.ui.screens.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.ScreenshotEntity
import com.example.domain.ocr.CategoryClassifier
import com.example.ui.components.CategoryBadge
import com.example.ui.components.formatBytes
import com.example.ui.components.formatDate
import java.util.Calendar

enum class SortOption(val title: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    LARGEST("Largest Size"),
    SMALLEST("Smallest Size"),
    NAME("Name A-Z")
}

enum class TimeFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    FAVORITES("Favorites")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    initialCategory: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (id: Long) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val context = LocalContext.current
    val allScreenshots by viewModel.allScreenshots.collectAsState()

    var isGridView by remember { mutableStateOf(true) }
    var selectedSort by remember { mutableStateOf(SortOption.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedTimeFilter by remember {
        mutableStateOf(if (initialCategory == "Favorites") TimeFilter.FAVORITES else TimeFilter.ALL)
    }
    var activeCategoryFilter by remember {
        mutableStateOf(if (initialCategory != "Favorites") initialCategory else null)
    }

    val selectedIds = remember { mutableStateListOf<Long>() }
    val isSelectionMode = selectedIds.isNotEmpty()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCategorizeDialog by remember { mutableStateOf(false) }

    // Filter screenshots
    val filteredScreenshots = remember(allScreenshots, selectedSort, selectedTimeFilter, activeCategoryFilter) {
        var list = allScreenshots

        if (activeCategoryFilter != null) {
            list = list.filter { it.category.equals(activeCategoryFilter, ignoreCase = true) }
        }

        val cal = Calendar.getInstance()
        when (selectedTimeFilter) {
            TimeFilter.ALL -> {}
            TimeFilter.FAVORITES -> {
                list = list.filter { it.isFavorite }
            }
            TimeFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                list = list.filter { it.dateTaken >= start }
            }
            TimeFilter.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                val end = start + 86_400_000L
                list = list.filter { it.dateTaken in start..end }
            }
            TimeFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                list = list.filter { it.dateTaken >= start }
            }
            TimeFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                list = list.filter { it.dateTaken >= start }
            }
        }

        when (selectedSort) {
            SortOption.NEWEST -> list.sortedByDescending { it.dateTaken }
            SortOption.OLDEST -> list.sortedBy { it.dateTaken }
            SortOption.LARGEST -> list.sortedByDescending { it.size }
            SortOption.SMALLEST -> list.sortedBy { it.size }
            SortOption.NAME -> list.sortedBy { it.displayName.lowercase() }
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedIds.size} Selected",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds.clear() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (selectedIds.size == filteredScreenshots.size) {
                                selectedIds.clear()
                            } else {
                                selectedIds.clear()
                                selectedIds.addAll(filteredScreenshots.map { it.id })
                            }
                        }) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Select All")
                        }
                        IconButton(onClick = {
                            val uris = filteredScreenshots
                                .filter { it.id in selectedIds }
                                .mapNotNull { Uri.parse(it.uri) }
                            if (uris.isNotEmpty()) {
                                val shareIntent = Intent().apply {
                                    action = if (uris.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE
                                    if (uris.size == 1) {
                                        putExtra(Intent.EXTRA_STREAM, uris.first())
                                    } else {
                                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                                    }
                                    type = "image/*"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Screenshots"))
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { showCategorizeDialog = true }) {
                            Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = "Categorize")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = activeCategoryFilter ?: "All Screenshots",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle View"
                            )
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = selectedSort == option,
                                                    onClick = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(option.title)
                                            }
                                        },
                                        onClick = {
                                            selectedSort = option
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
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
        ) {
            // Time & Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TimeFilter.values()) { filter ->
                    FilterChip(
                        selected = selectedTimeFilter == filter,
                        onClick = { selectedTimeFilter = filter },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Results count subheader
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredScreenshots.size} screenshots found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isSelectionMode && filteredScreenshots.isNotEmpty()) {
                    Text(
                        text = "Select",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            selectedIds.add(filteredScreenshots.first().id)
                        }
                    )
                }
            }

            if (filteredScreenshots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No screenshots match this filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredScreenshots, key = { it.id }) { screenshot ->
                        val isSelected = selectedIds.contains(screenshot.id)
                        GridScreenshotCard(
                            screenshot = screenshot,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onToggleSelect = {
                                if (isSelected) selectedIds.remove(screenshot.id)
                                else selectedIds.add(screenshot.id)
                            },
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedIds.remove(screenshot.id)
                                    else selectedIds.add(screenshot.id)
                                } else {
                                    onNavigateToDetail(screenshot.id)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedIds.add(screenshot.id)
                                }
                            },
                            onFavoriteClick = {
                                viewModel.toggleFavorite(screenshot.id, screenshot.isFavorite)
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredScreenshots, key = { it.id }) { screenshot ->
                        val isSelected = selectedIds.contains(screenshot.id)
                        ListScreenshotRow(
                            screenshot = screenshot,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedIds.remove(screenshot.id)
                                    else selectedIds.add(screenshot.id)
                                } else {
                                    onNavigateToDetail(screenshot.id)
                                }
                            },
                            onFavoriteClick = {
                                viewModel.toggleFavorite(screenshot.id, screenshot.isFavorite)
                            }
                        )
                    }
                }
            }
        }
    }

    // Batch Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${selectedIds.size} Screenshot(s)?") },
            text = { Text("Are you sure you want to remove the selected screenshots from the app database?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScreenshots(selectedIds.toList())
                        selectedIds.clear()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Batch Categorize Dialog
    if (showCategorizeDialog) {
        var chosenCategory by remember { mutableStateOf(CategoryClassifier.CATEGORY_RECEIPTS) }
        AlertDialog(
            onDismissRequest = { showCategorizeDialog = false },
            title = { Text("Categorize ${selectedIds.size} Items") },
            text = {
                Column {
                    CategoryClassifier.ALL_CATEGORIES.take(8).forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chosenCategory = cat }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = chosenCategory == cat,
                                onClick = { chosenCategory = cat }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cat)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedIds.forEach { id ->
                        viewModel.updateCategory(id, chosenCategory)
                    }
                    selectedIds.clear()
                    showCategorizeDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategorizeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GridScreenshotCard(
    screenshot: ScreenshotEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = screenshot.uri,
                    contentDescription = screenshot.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clickable(onClick = onToggleSelect)
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = if (screenshot.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (screenshot.isFavorite) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.8f)
                    )
                }

                // File size pill overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = formatBytes(screenshot.size),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                CategoryBadge(category = screenshot.category)
                Spacer(modifier = Modifier.height(3.dp))
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

@Composable
fun ListScreenshotRow(
    screenshot: ScreenshotEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
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
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(category = screenshot.category)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatBytes(screenshot.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (screenshot.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (screenshot.isFavorite) Color(0xFFFBBF24) else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
