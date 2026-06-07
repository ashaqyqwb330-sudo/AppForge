package com.appforge.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.appforge.domain.model.ColumnInfo
import com.appforge.domain.model.TableInfo
import com.appforge.domain.model.Template
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun writeDebugLog(message: String) {
        try {
            val log = "[${System.currentTimeMillis()}] $message\n"
            val filename = "appforge_debug.log"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it, "wa")?.use { os ->
                        os.write(log.toByteArray())
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, filename)
                FileOutputStream(file, true).use { fos ->
                    fos.write(log.toByteArray())
                }
            }
        } catch (_: Exception) {}
    }

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
            writeDebugLog("بدء تحليل قاعدة البيانات...")
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
            writeDebugLog("تم نسخ الملف المؤقت إلى ${tempFile.absolutePath}")

            val db = SQLiteDatabase.openDatabase(
                tempFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            writeDebugLog("تم فتح قاعدة البيانات")

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
            writeDebugLog("عدد الجداول: ${tableNames.size}")

            val tablesToProcess = if (tableNames.size > 12) tableNames.take(12) else tableNames
            var appNameSuggestion: String? = null

            for (tableName in tablesToProcess) {
                writeDebugLog("معالجة جدول: $tableName")
                val pragmaCursor = db.rawQuery("PRAGMA table_info(`$tableName`)", null)
                val columns = mutableListOf<ColumnInfo>()
                var hasAppNameColumn = false

                while (pragmaCursor.moveToNext()) {
                    val colName = pragmaCursor.getString(pragmaCursor.getColumnIndexOrThrow("name"))
                    val colType = pragmaCursor.getString(pragmaCursor.getColumnIndexOrThrow("type"))
                    val isPk = pragmaCursor.getInt(pragmaCursor.getColumnIndexOrThrow("pk")) > 0
                    if (colName.equals("app_name", ignoreCase = true)) hasAppNameColumn = true
                    columns.add(ColumnInfo(colName, colType, isPk))
                }
                pragmaCursor.close()

                val countCursor = db.rawQuery("SELECT COUNT(*) FROM `$tableName`", null)
                var rowCount = 0
                if (countCursor.moveToFirst()) rowCount = countCursor.getInt(0)
                countCursor.close()

                if (rowCount > 0 && hasAppNameColumn) {
                    val nameCursor = db.rawQuery("SELECT app_name FROM `$tableName` LIMIT 1", null)
                    if (nameCursor.moveToFirst()) appNameSuggestion = nameCursor.getString(0)
                    nameCursor.close()
                }

                tables.add(TableInfo(tableName, columns, rowCount))
            }

            db.close()
            tempFile.delete()
            writeDebugLog("اكتمل التحليل بنجاح، عدد الجداول المُعالجة: ${tables.size}")

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
            writeDebugLog("فشل التحليل: ${e.message}")
            Result.failure(e)
        }
    }
}
