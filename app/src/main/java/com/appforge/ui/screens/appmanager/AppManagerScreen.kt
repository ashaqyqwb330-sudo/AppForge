package com.appforge.ui.screens.appmanager

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appforge.domain.model.AppInstance
import com.appforge.domain.model.Template

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(
    viewModel: AppManagerViewModel,
    onAppActivated: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Get file name from URI
            val cursor = context.contentResolver.query(it, null, null, null, null)
            var fileName = "unknown.db"
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = c.getString(nameIndex)
                    }
                }
            }
            viewModel.onFileSelected(it, fileName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AppForge - تطبيقاتي") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "الإعدادات")
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Outlined.Info, contentDescription = "حول")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة قاعدة بيانات")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isAnalyzing) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("جاري تحليل قاعدة البيانات...")
                }
            } else if (uiState.instances.isEmpty()) {
                // Empty state
                EmptyStateView(
                    message = "لا توجد تطبيقات مضافة بعد.\nاضغط على + لفتح قاعدة بيانات جديدة.",
                    onAction = { filePickerLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.instances, key = { it.id }) { instance ->
                        AppInstanceCard(
                            instance = instance,
                            onActivate = {
                                viewModel.activateApp(instance.id)
                                onAppActivated()
                            },
                            onDelete = { viewModel.deleteApp(instance) },
                            onRename = { newName -> viewModel.renameApp(instance.id, newName) }
                        )
                    }
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("موافق")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // Add dialog
            if (uiState.showAddDialog) {
                AddAppDialog(
                    suggestedName = uiState.suggestedName,
                    template = uiState.analysisResult?.suggestedTemplate ?: Template.default(),
                    onConfirm = { name ->
                        viewModel.addNewApp(name)
                    },
                    onDismiss = { viewModel.dismissAddDialog() }
                )
            }
        }
    }
}

@Composable
fun AppInstanceCard(
    instance: AppInstance,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(instance.name) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActivate() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = instance.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Template.fromId(instance.templateId).displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { showRenameDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل الاسم")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف")
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("إعادة تسمية التطبيق") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("الاسم الجديد") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRename(newName)
                    showRenameDialog = false
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AddAppDialog(
    suggestedName: String,
    template: Template,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(suggestedName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تطبيق جديد") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم التطبيق") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "القالب المقترح: ${template.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.ifBlank { suggestedName }) }) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun EmptyStateView(message: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAction) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("فتح قاعدة بيانات")
        }
    }
}
