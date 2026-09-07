package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ocr.CategoryClassifier
import com.example.ui.theme.TagAmber
import com.example.ui.theme.TagEmerald
import com.example.ui.theme.TagIndigo
import com.example.ui.theme.TagRose
import com.example.ui.theme.TagSky
import com.example.ui.theme.TagViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
        else -> "$bytes B"
    }
}

fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return "Unknown"
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateTime(timestamp: Long): String {
    if (timestamp <= 0) return "Unknown"
    val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        CategoryClassifier.CATEGORY_RECEIPTS -> TagEmerald
        CategoryClassifier.CATEGORY_FINANCE -> TagEmerald
        CategoryClassifier.CATEGORY_BILLS -> TagAmber
        CategoryClassifier.CATEGORY_FOOD -> TagAmber
        CategoryClassifier.CATEGORY_TICKETS -> TagSky
        CategoryClassifier.CATEGORY_TRAVEL -> TagSky
        CategoryClassifier.CATEGORY_SHOPPING -> TagRose
        CategoryClassifier.CATEGORY_STUDY -> TagIndigo
        CategoryClassifier.CATEGORY_DOCUMENTS -> TagIndigo
        CategoryClassifier.CATEGORY_NOTES -> TagViolet
        CategoryClassifier.CATEGORY_SOCIAL -> TagViolet
        else -> Color(0xFF64748B)
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val color = getCategoryColor(category)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = color
        )
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
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
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntityChip(
    label: String,
    value: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, value)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied $value", Toast.LENGTH_SHORT).show()
            },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
