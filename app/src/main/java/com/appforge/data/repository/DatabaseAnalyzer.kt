package com.appforge.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.appforge.domain.model.ColumnInfo
import com.appforge.domain.model.TableInfo
import com.appforge.domain.model.Template
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class AnalysisResult(
        val tables: List<TableInfo>,
        val suggestedTemplate: Template,
        val appNameSuggestion: String?,
        val containsImages: Boolean,
        val containsLongText: Boolean,
        val containsNumbers: Boolean
    )

    suspend fun analyzeDatabase(uri: Uri): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            // نسخ الملف أولاً (حتى لو تم تحليله لاحقاً، نضمن وجود نسخة)
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.db")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            } ?: throw Exception("تعذر فتح الملف")

            val db = SQLiteDatabase.openDatabase(
                tempFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            val tables = mutableListOf<TableInfo>()
            var containsImages = false
            var containsLongText = false
            var containsNumbers = false

            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
                null
            )

            val tableNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tableNames.add(cursor.getString(0))
            }
            cursor.close()

            val tablesToProcess = if (tableNames.size > 12) tableNames.take(12) else tableNames
            var appNameSuggestion: String? = null

            for (tableName in tablesToProcess) {
                val pragmaCursor = db.rawQuery("PRAGMA table_info(`$tableName`)", null)
                val columns = mutableListOf<ColumnInfo>()
                var hasAppNameColumn = false

                while (pragmaCursor.moveToNext()) {
                    val colName = pragmaCursor.getString(pragmaCursor.getColumnIndexOrThrow("name"))
                    val colType = pragmaCursor.getString(pragmaCursor.getColumnIndexOrThrow("type"))
                    val isPk = pragmaCursor.getInt(pragmaCursor.getColumnIndexOrThrow("pk")) > 0

                    if (colName.equals("app_name", ignoreCase = true)) {
                        hasAppNameColumn = true
                    }
                    columns.add(ColumnInfo(colName, colType, isPk))
                }
                pragmaCursor.close()

                val countCursor = db.rawQuery("SELECT COUNT(*) FROM `$tableName`", null)
                var rowCount = 0
                if (countCursor.moveToFirst()) {
                    rowCount = countCursor.getInt(0)
                }
                countCursor.close()

                if (rowCount > 0 && hasAppNameColumn) {
                    val nameCursor = db.rawQuery(
                        "SELECT app_name FROM `$tableName` LIMIT 1",
                        null
                    )
                    if (nameCursor.moveToFirst()) {
                        appNameSuggestion = nameCursor.getString(0)
                    }
                    nameCursor.close()
                }

                if (rowCount > 0) {
                    val sampleCursor = db.rawQuery(
                        "SELECT * FROM `$tableName` LIMIT 5",
                        null
                    )
                    val colIndices = columns.indices.associate { i -> columns[i].name to i }

                    while (sampleCursor.moveToNext()) {
                        for (col in columns) {
                            val value = sampleCursor.getString(colIndices[col.name]!!)
                            when {
                                col.type.uppercase().contains("BLOB") -> containsImages = true
                                value != null && value.length > 200 -> containsLongText = true
                                value != null && value.toDoubleOrNull() != null -> containsNumbers = true
                            }
                        }
                    }
                    sampleCursor.close()
                }

                tables.add(TableInfo(tableName, columns, rowCount))
            }

            db.close()
            tempFile.delete()

            val suggestedTemplate = when {
                containsImages -> Template.ELEGANT_GALLERY
                containsLongText -> Template.STORY_TELLER
                containsNumbers && tables.size > 2 -> Template.TABLE_ORGANIZER
                else -> Template.INTERACTIVE_GRID
            }

            Result.success(AnalysisResult(
                tables = tables,
                suggestedTemplate = suggestedTemplate,
                appNameSuggestion = appNameSuggestion,
                containsImages = containsImages,
                containsLongText = containsLongText,
                containsNumbers = containsNumbers
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
