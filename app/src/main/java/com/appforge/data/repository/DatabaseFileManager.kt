package com.appforge.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseFileManager @Inject constructor(
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
                // نحاول فتح الملف الموجود للإلحاق، وإن لم يوجد ننشئه
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

    suspend fun copyDbToInternal(uri: Uri, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            writeDebugLog("بدء نسخ الملف: $fileName")
            val dbDir = File(context.filesDir, "databases")
            if (!dbDir.exists()) dbDir.mkdirs()

            val targetFile = File(dbDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            } ?: throw Exception("تعذر فتح الملف المصدر")
            writeDebugLog("تم النسخ بنجاح إلى ${targetFile.absolutePath}")
            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            writeDebugLog("فشل النسخ: ${e.message}")
            Result.failure(e)
        }
    }

    fun deleteDatabase(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }
}
