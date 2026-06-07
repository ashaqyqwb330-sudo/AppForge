package com.appforge.ui.screens.activeapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appforge.domain.model.Template
import com.appforge.ui.templates.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveAppHost(
    viewModel: ActiveAppViewModel,
    onBackToManager: () -> Unit,
    onTableClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.activeInstance?.name ?: "تطبيق") },
                navigationIcon = {
                    IconButton(onClick = onBackToManager) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (uiState.activeInstance != null) {
                        val template = Template.fromId(uiState.activeInstance!!.templateId)
                        Text(
                            text = template.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    LinearProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.error!!)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
                uiState.activeInstance != null && uiState.tables.isNotEmpty() -> {
                    val template = Template.fromId(uiState.activeInstance!!.templateId)
                    when (template) {
                        Template.ELEGANT_GALLERY -> TemplateElegantGallery(
                            tables = uiState.tables,
                            tableData = uiState.tableData,
                            onTableClick = onTableClick
                        )
                        Template.TABLE_ORGANIZER -> TemplateTableOrganizer(
                            tables = uiState.tables,
                            tableData = uiState.tableData,
                            onTableClick = onTableClick
                        )
                        Template.STORY_TELLER -> TemplateStoryTeller(
                            tables = uiState.tables,
                            tableData = uiState.tableData,
                            onTableClick = onTableClick
                        )
                        Template.INTERACTIVE_GRID -> TemplateInteractiveGrid(
                            tables = uiState.tables,
                            tableData = uiState.tableData,
                            onTableClick = onTableClick
                        )
                    }
                }
            }
        }
    }
}
