package com.appforge.ui.templates

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appforge.domain.model.RowData
import com.appforge.domain.model.TableInfo

@Composable
fun TemplateTableOrganizer(
    tables: List<TableInfo>,
    tableData: Map<String, List<RowData>>,
    onTableClick: (String) -> Unit
) {
    var selectedTableIndex by remember { mutableStateOf(0) }
    val tabs = tables.map { it.name }

    Column {
        // Table tabs
        ScrollableTabRow(selectedTabIndex = selectedTableIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTableIndex == index,
                    onClick = { selectedTableIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // Selected table content
        if (tables.isNotEmpty()) {
            val selectedTable = tables[selectedTableIndex]
            val rows = tableData[selectedTable.name] ?: emptyList()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            selectedTable.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = { onTableClick(selectedTable.name) }) {
                            Text("عرض كامل")
                        }
                    }

                    // Data table (horizontally scrollable)
                    if (rows.isNotEmpty()) {
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Column {
                                // Table header
                                Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    selectedTable.columns.forEach { col ->
                                        Text(
                                            col.name,
                                            modifier = Modifier
                                                .width(120.dp)
                                                .padding(8.dp),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                                Divider()
                                // Rows (show first 5)
                                rows.take(5).forEach { row ->
                                    Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                                        selectedTable.columns.forEach { col ->
                                            Text(
                                                text = row.values[col.name] ?: "NULL",
                                                modifier = Modifier
                                                    .width(120.dp)
                                                    .padding(8.dp),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    Divider()
                                }
                                if (rows.size > 5) {
                                    Text(
                                        "... و ${rows.size - 5} صف آخر",
                                        modifier = Modifier.padding(12.dp),
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        Text("لا توجد بيانات", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            // Summary cards for other tables
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tables.filter { it != selectedTable }) { table ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedTableIndex = tables.indexOf(table)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(table.name, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text("${table.columns.size}") })
                                AssistChip(onClick = {}, label = { Text("${table.rowCount}") })
                            }
                        }
                    }
                }
            }
        }
    }
}
