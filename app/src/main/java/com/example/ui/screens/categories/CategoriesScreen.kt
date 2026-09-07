package com.example.ui.screens.categories

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ads.AdMobBanner
import com.example.domain.ocr.CategoryClassifier
import com.example.ui.components.formatBytes
import com.example.ui.components.getCategoryColor

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        CategoryClassifier.CATEGORY_RECEIPTS -> Icons.Default.ReceiptLong
        CategoryClassifier.CATEGORY_SHOPPING -> Icons.Default.ShoppingBag
        CategoryClassifier.CATEGORY_BILLS -> Icons.Default.Receipt
        CategoryClassifier.CATEGORY_TICKETS -> Icons.Default.ConfirmationNumber
        CategoryClassifier.CATEGORY_TRAVEL -> Icons.Default.Flight
        CategoryClassifier.CATEGORY_STUDY -> Icons.Default.School
        CategoryClassifier.CATEGORY_NOTES -> Icons.Default.Note
        CategoryClassifier.CATEGORY_DOCUMENTS -> Icons.Default.Description
        CategoryClassifier.CATEGORY_SOCIAL -> Icons.Default.Share
        CategoryClassifier.CATEGORY_ENTERTAINMENT -> Icons.Default.Movie
        CategoryClassifier.CATEGORY_MAPS -> Icons.Default.Map
        CategoryClassifier.CATEGORY_FINANCE -> Icons.Default.AccountBalance
        CategoryClassifier.CATEGORY_FOOD -> Icons.Default.Fastfood
        else -> Icons.Default.Folder
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onCategoryClick: (category: String) -> Unit
) {
    val categoryStats by viewModel.categoryStats.collectAsState()
    val statsMap = categoryStats.associateBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smart Categories",
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(CategoryClassifier.ALL_CATEGORIES) { category ->
                val stat = statsMap[category]
                val count = stat?.count ?: 0
                val totalBytes = stat?.totalBytes ?: 0L
                val color = getCategoryColor(category)
                val icon = getCategoryIcon(category)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onCategoryClick(category) },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }

                        Column {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$count items • ${formatBytes(totalBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
