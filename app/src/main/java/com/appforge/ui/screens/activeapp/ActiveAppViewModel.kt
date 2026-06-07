package com.appforge.ui.screens.activeapp

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appforge.data.repository.AppRepository
import com.appforge.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ActiveAppUiState(
    val activeInstance: AppInstance? = null,
    val tables: List<TableInfo> = emptyList(),
    val tableData: Map<String, List<RowData>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ActiveAppViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveAppUiState())
    val uiState: StateFlow<ActiveAppUiState> = _uiState.asStateFlow()

    init {
        loadActiveApp()
    }

    fun loadActiveApp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val active = repository.getActiveInstance()
                if (active == null) {
                    _uiState.update { it.copy(isLoading = false, error = "لا يوجد تطبيق نشط") }
                    return@launch
                }

                val dbPath = active.dbFilePath
                val file = File(dbPath)
                if (!file.exists()) {
                    _uiState.update { it.copy(isLoading = false, error = "ملف قاعدة البيانات غير موجود") }
                    return@launch
                }

                val db = withContext(Dispatchers.IO) {
                    SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
                }

                val tables = mutableListOf<TableInfo>()
                val dataMap = mutableMapOf<String, List<RowData>>()

                val cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
                    null
                )
                val tableNames = mutableListOf<String>()
                while (cursor.moveToNext()) tableNames.add(cursor.getString(0))
                cursor.close()

                // Limit to 12 tables
                val limitedNames = tableNames.take(12)

                for (name in limitedNames) {
                    val pragmaCursor = db.rawQuery("PRAGMA table_info(`$name`)", null)
                    val columns = mutableListOf<ColumnInfo>()
                    while (pragmaCursor.moveToNext()) {
                        columns.add(
                            ColumnInfo(
                                name = pragmaCursor.getString(pragmaCursor.getColumnIndexOrThrow("name")),
                                type = pragmaCursor.getString(pragmaCursor.getColumnIndexOrThrow("type")),
                                isPrimaryKey = pragmaCursor.getInt(pragmaCursor.getColumnIndexOrThrow("pk")) > 0
                            )
                        )
                    }
                    pragmaCursor.close()

                    val countCursor = db.rawQuery("SELECT COUNT(*) FROM `$name`", null)
                    var rowCount = 0
                    if (countCursor.moveToFirst()) rowCount = countCursor.getInt(0)
                    countCursor.close()

                    tables.add(TableInfo(name, columns, rowCount))

                    // Load sample rows (first 20)
                    val dataCursor = db.rawQuery("SELECT * FROM `$name` LIMIT 20", null)
                    val rows = mutableListOf<RowData>()
                    while (dataCursor.moveToNext()) {
                        val values = mutableMapOf<String, String?>()
                        val raw = mutableMapOf<String, ByteArray?>()
                        for (col in columns) {
                            val index = dataCursor.getColumnIndex(col.name)
                            if (index >= 0) {
                                if (col.type.uppercase().contains("BLOB")) {
                                    raw[col.name] = dataCursor.getBlob(index)
                                    values[col.name] = "[صورة]"
                                } else {
                                    values[col.name] = dataCursor.getString(index)
                                }
                            }
                        }
                        rows.add(RowData(values, raw))
                    }
                    dataCursor.close()
                    dataMap[name] = rows
                }

                db.close()

                _uiState.update {
                    it.copy(
                        activeInstance = active,
                        tables = tables,
                        tableData = dataMap,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "خطأ: ${e.message}") }
            }
        }
    }

    fun refresh() = loadActiveApp()
}
