package com.appforge.ui.screens.tabledetail

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appforge.data.repository.AppRepository
import com.appforge.domain.model.ColumnInfo
import com.appforge.domain.model.RowData
import com.appforge.domain.model.TableInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class TableDetailUiState(
    val tableName: String = "",
    val columns: List<ColumnInfo> = emptyList(),
    val rows: List<RowData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TableDetailViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tableName: String = savedStateHandle.get<String>("tableName") ?: ""
    private val _uiState = MutableStateFlow(TableDetailUiState(tableName = tableName))
    val uiState: StateFlow<TableDetailUiState> = _uiState.asStateFlow()

    init {
        loadTableData()
    }

    fun loadTableData() {
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

                // Get columns
                val pragmaCursor = db.rawQuery("PRAGMA table_info(`$tableName`)", null)
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

                // Load all rows (with reasonable limit, e.g., 500)
                val dataCursor = db.rawQuery("SELECT * FROM `$tableName` LIMIT 500", null)
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
                db.close()

                _uiState.update {
                    it.copy(
                        columns = columns,
                        rows = rows,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "خطأ: ${e.message}") }
            }
        }
    }
}
