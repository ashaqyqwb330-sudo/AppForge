package com.appforge.ui.screens.tabledetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appforge.ui.components.SmartCell
import com.appforge.ui.components.SmartImageCell

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDetailScreen(
    viewModel: TableDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.tableName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.error!!)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadTableData() }) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
                else -> {
                    if (uiState.rows.isEmpty()) {
                        Text(
                            "لا توجد بيانات في هذا الجدول",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // Horizontally scrollable data table
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            // Header row
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .height(48.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                uiState.columns.forEach { col ->
                                    Text(
                                        text = col.name,
                                        modifier = Modifier
                                            .width(140.dp)
                                            .padding(horizontal = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Divider()

                            // Data rows (using LazyColumn inside horizontal scroll is tricky, so we use a simple Column for demonstration; for large data, consider using a custom layout or a two-dimensional scroll)
                            Column {
                                uiState.rows.take(200).forEach { row ->
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        uiState.columns.forEach { col ->
                                            Box(
                                                modifier = Modifier
                                                    .width(140.dp)
                                                    .padding(horizontal = 8.dp)
                                            ) {
                                                val value = row.values[col.name]
                                                val blobData = row.rawData[col.name]
                                                if (col.type.uppercase().contains("BLOB") && blobData != null) {
                                                    SmartImageCell(blobData = blobData)
                                                } else {
                                                    SmartCell(
                                                        value = value,
                                                        columnType = col.type,
                                                        onCopy = { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(it)) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Divider()
                                }
                                if (uiState.rows.size > 200) {
                                    Text(
                                        "... و ${uiState.rows.size - 200} صف آخر",
                                        modifier = Modifier.padding(12.dp),
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
