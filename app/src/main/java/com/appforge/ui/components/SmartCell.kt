package com.appforge.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SmartCell(
    value: String?,
    columnType: String,
    modifier: Modifier = Modifier,
    onCopy: (String) -> Unit = {}
) {
    if (value == null) {
        Text(
            text = "NULL",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = modifier
        )
        return
    }

    when {
        value.matches(Regex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$")) -> {
            val uriHandler = LocalUriHandler.current
            TextButton(
                onClick = { uriHandler.openUri(value) },
                modifier = modifier
            ) {
                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("فتح الرابط", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        value.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) -> {
            val uriHandler = LocalUriHandler.current
            TextButton(
                onClick = { uriHandler.openUri("mailto:$value") },
                modifier = modifier
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        value.matches(Regex("\\+?[0-9\\-\\s]{7,}")) -> {
            val uriHandler = LocalUriHandler.current
            TextButton(
                onClick = { uriHandler.openUri("tel:$value") },
                modifier = modifier
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        value.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
            var formattedDate = value
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = sdf.parse(value)
                if (date != null) {
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("ar"))
                    formattedDate = outputFormat.format(date)
                }
            } catch (_: Exception) {}
            Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(formattedDate)
            }
        }
        value.length > 100 -> {
            var expanded by remember { mutableStateOf(false) }
            Column(modifier = modifier.clickable { expanded = !expanded }) {
                Text(
                    text = if (expanded) value else value.take(100) + "...",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!expanded) {
                    Text("اقرأ المزيد", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        else -> {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { onCopy(value) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun SmartImageCell(blobData: ByteArray?, modifier: Modifier = Modifier) {
    if (blobData == null) {
        Text("لا توجد صورة", modifier = modifier)
        return
    }
    AsyncImage(
        model = blobData,
        contentDescription = "صورة من قاعدة البيانات",
        modifier = modifier
            .size(80.dp)
            .padding(4.dp)
    )
}
