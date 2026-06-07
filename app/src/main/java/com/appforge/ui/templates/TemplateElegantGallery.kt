package com.appforge.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appforge.domain.model.RowData
import com.appforge.domain.model.TableInfo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TemplateElegantGallery(
    tables: List<TableInfo>,
    tableData: Map<String, List<RowData>>,
    onTableClick: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tables.size })

    Column {
        // Horizontal image carousel for tables that have image columns
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) { page ->
            val table = tables[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = { onTableClick(table.name) },
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Try to find an image column to display
                    val imageColumn = table.columns.find { it.type.uppercase().contains("BLOB") }
                    if (imageColumn != null && tableData[table.name]?.isNotEmpty() == true) {
                        val firstRow = tableData[table.name]!!.first()
                        val blobData = firstRow.rawData[imageColumn.name]
                        if (blobData != null) {
                            AsyncImage(
                                model = blobData,
                                contentDescription = "صورة من ${table.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    // Overlay text
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            table.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${table.rowCount} صف",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Table list
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tables) { table ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onTableClick(table.name) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(table.name, fontWeight = FontWeight.Bold)
                            Text("${table.columns.size} أعمدة")
                        }
                        Text("${table.rowCount}", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}
