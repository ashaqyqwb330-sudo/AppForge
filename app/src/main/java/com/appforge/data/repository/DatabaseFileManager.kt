package com.appforge.data.repository

import android.content.Context
import android.net.Uri
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
    /**
     * Copies the selected database file to app-private storage
     * and returns the absolute path of the copied file.
     * This ensures the database remains accessible even if the original is deleted.
     */
    suspend fun copyDbToInternal(uri: Uri, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dbDir = File(context.filesDir, "databases")
            if (!dbDir.exists()) dbDir.mkdirs()

            val targetFile = File(dbDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw Exception("تعذر فتح الملف المصدر")

            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
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

    fun getDatabaseFile(filePath: String): File = File(filePath)
}
