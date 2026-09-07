package com.example.ui.screens.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.ads.AdMobBanner
import com.example.data.local.ScreenshotEntity
import com.example.domain.ai.AiInsight
import com.example.domain.ocr.EntityExtractor
import com.example.ui.components.CategoryBadge
import com.example.ui.components.formatBytes
import com.example.ui.components.formatDateTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScreenshotDetailScreen(
    screenshotId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allScreenshots by viewModel.allScreenshots.collectAsState()
    val screenshot = allScreenshots.firstOrNull { it.id == screenshotId }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var aiInsight by remember { mutableStateOf<AiInsight?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }
    var userQuestion by remember { mutableStateOf("") }
    var aiAnswer by remember { mutableStateOf("") }
    var isAnsweringQuestion by remember { mutableStateOf(false) }

    LaunchedEffect(screenshot) {
        if (screenshot != null && aiInsight == null) {
            isLoadingAi = true
            aiInsight = viewModel.getAiInsight(screenshot)
            isLoadingAi = false
        }
    }

    if (screenshot == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Screenshot") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Screenshot not found or deleted.")
            }
        }
        return
    }

    val prices = remember(screenshot) { EntityExtractor.deserializeList(screenshot.extractedPrices) }
    val dates = remember(screenshot) { EntityExtractor.deserializeList(screenshot.extractedDates) }
    val emails = remember(screenshot) { EntityExtractor.deserializeList(screenshot.extractedEmails) }
    val phones = remember(screenshot) { EntityExtractor.deserializeList(screenshot.extractedPhones) }
    val urls = remember(screenshot) { EntityExtractor.deserializeList(screenshot.extractedUrls) }
    val orders = remember(screenshot) { EntityExtractor.deserializeList(screenshot.extractedOrderNumbers) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenshot.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(screenshot.id, screenshot.isFavorite) }) {
                        Icon(
                            imageVector = if (screenshot.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (screenshot.isFavorite) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(screenshot.uri))
                            if (screenshot.ocrText.isNotBlank()) {
                                putExtra(Intent.EXTRA_TEXT, screenshot.ocrText)
                            }
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Screenshot"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Image Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = screenshot.uri,
                    contentDescription = screenshot.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Quick Info Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CategoryBadge(category = screenshot.category)
                    Text(
                        text = "${formatBytes(screenshot.size)} • ${screenshot.width}x${screenshot.height}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formatDateTime(screenshot.dateTaken),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // AI Assistant Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AI Assistant Insights",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isLoadingAi) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing screenshot content...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else if (aiInsight != null) {
                            Text(
                                text = aiInsight?.summary ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (aiInsight?.keyInformation?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(8.dp))
                                aiInsight?.keyInformation?.forEach { info ->
                                    Text(
                                        text = "• $info",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Ask AI input field
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = userQuestion,
                                onValueChange = { userQuestion = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Ask a question about this image...", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (userQuestion.isNotBlank()) {
                                        scope.launch {
                                            isAnsweringQuestion = true
                                            aiAnswer = viewModel.askAiQuestion(screenshot, userQuestion)
                                            isAnsweringQuestion = false
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Ask",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (isAnsweringQuestion) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Thinking...", style = MaterialTheme.typography.labelSmall)
                        } else if (aiAnswer.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = aiAnswer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actionable Entities Section
                Text(
                    text = "Extracted Information",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Prices
                    prices.forEach { price ->
                        ActionableChip(
                            label = "Price",
                            value = price,
                            icon = Icons.Default.LocalOffer,
                            onAction = { copyToClipboard(context, "Price", price) }
                        )
                    }

                    // Dates
                    dates.forEach { date ->
                        ActionableChip(
                            label = "Date",
                            value = date,
                            icon = Icons.Default.DateRange,
                            onAction = { copyToClipboard(context, "Date", date) }
                        )
                    }

                    // Phones
                    phones.forEach { phone ->
                        ActionableChip(
                            label = "Call",
                            value = phone,
                            icon = Icons.Default.Call,
                            onAction = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    copyToClipboard(context, "Phone", phone)
                                }
                            }
                        )
                    }

                    // Emails
                    emails.forEach { email ->
                        ActionableChip(
                            label = "Email",
                            value = email,
                            icon = Icons.Default.Email,
                            onAction = {
                                try {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    copyToClipboard(context, "Email", email)
                                }
                            }
                        )
                    }

                    // URLs
                    urls.forEach { url ->
                        ActionableChip(
                            label = "Open",
                            value = url,
                            icon = Icons.Default.Language,
                            onAction = {
                                try {
                                    val finalUrl = if (!url.startsWith("http")) "https://$url" else url
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                    copyToClipboard(context, "Link", url)
                                }
                            }
                        )
                    }

                    // Orders
                    orders.forEach { order ->
                        ActionableChip(
                            label = "Ref/Order",
                            value = order,
                            icon = Icons.Default.Receipt,
                            onAction = { copyToClipboard(context, "Order", order) }
                        )
                    }
                }

                if (prices.isEmpty() && dates.isEmpty() && emails.isEmpty() && phones.isEmpty() && urls.isEmpty() && orders.isEmpty()) {
                    Text(
                        text = "No specific structured entities detected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Full OCR Text Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Extracted Text (OCR)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (screenshot.ocrText.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { copyToClipboard(context, "OCR Text", screenshot.ocrText) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy All Text", fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (screenshot.ocrText.isNotBlank()) {
                            Text(
                                text = screenshot.ocrText,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "No text was recognized in this image.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Screenshot?") },
            text = { Text("Are you sure you want to remove this screenshot from the app?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScreenshot(screenshot.id)
                        showDeleteConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActionableChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onAction),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
}
