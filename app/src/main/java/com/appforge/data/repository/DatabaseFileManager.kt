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
    suspend fun copyDbToInternal(uri: Uri, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
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
}
