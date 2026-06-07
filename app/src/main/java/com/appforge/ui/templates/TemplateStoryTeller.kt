package com.appforge.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appforge.domain.model.RowData
import com.appforge.domain.model.TableInfo

@Composable
fun TemplateStoryTeller(
    tables: List<TableInfo>,
    tableData: Map<String, List<RowData>>,
    onTableClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(tables) { index, table ->
            val rows = tableData[table.name] ?: emptyList()
            // Timeline style
            Row(modifier = Modifier.fillMaxWidth()) {
                // Timeline indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(40.dp)
                ) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    if (index < tables.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(60.dp)
                                .defaultMinSize(minHeight = 60.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                // Content card
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { onTableClick(table.name) },
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            table.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${table.rowCount} قصة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Show snippet of first row if contains long text
                        if (rows.isNotEmpty()) {
                            val textColumns = table.columns.filter { it.type.uppercase() == "TEXT" }
                            if (textColumns.isNotEmpty()) {
                                val firstText = rows.first().values[textColumns.first().name]
                                if (firstText != null && firstText.length > 50) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        firstText.take(100) + "...",
                                        style = MaterialTheme.typography.bodySmall,
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
