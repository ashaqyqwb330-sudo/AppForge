package com.appforge

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.appforge.ui.navigation.NavGraph
import com.appforge.ui.theme.AppForgeTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // معالج الاستثناءات غير المعالجة
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            val log = buildCrashLog(e)
            saveCrashLogToPublicDownloads(log)
            runOnUiThread {
                Toast.makeText(
                    this,
                    "حدث خطأ، تم حفظ التفاصيل في مجلد التنزيلات (Download/appforge_crash.log)",
                    Toast.LENGTH_LONG
                ).show()
            }
            // إنهاء التطبيق بعد تأخير قصير لضمان ظهور التوست
            Thread.sleep(3000)
            finish()
        }

        enableEdgeToEdge()
        setContent {
            AppForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }

    private fun buildCrashLog(e: Throwable): String {
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sb.appendLine("========== AppForge Crash Report ==========")
        sb.appendLine("Date: ${sdf.format(Date())}")
        sb.appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        sb.appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        sb.appendLine("App Version: 1.0")
        sb.appendLine("Exception: ${e.javaClass.name}")
        sb.appendLine("Message: ${e.message ?: "none"}")
        sb.appendLine("Stack Trace:")
        e.stackTrace.forEach { element ->
            sb.appendLine("  at $element")
        }
        // إذا كان هناك سبب (cause) أضفه أيضاً
        e.cause?.let { cause ->
            sb.appendLine("Caused by: ${cause.javaClass.name}")
            sb.appendLine("Message: ${cause.message ?: "none"}")
            cause.stackTrace.forEach { element ->
                sb.appendLine("  at $element")
            }
        }
        sb.appendLine("===========================================")
        return sb.toString()
    }

    private fun saveCrashLogToPublicDownloads(log: String) {
        try {
            val filename = "appforge_crash.log"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // استخدام MediaStore للوصول إلى مجلد التحميلات العام
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(log.toByteArray())
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    contentResolver.update(it, contentValues, null, null)
                }
            } else {
                // أجهزة أقدم من Android 10
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, filename)
                FileOutputStream(file).use { fos ->
                    fos.write(log.toByteArray())
                }
            }
        } catch (e: Exception) {
            // فشل الحفظ، لا يمكننا فعل شيء أكثر من ذلك
            e.printStackTrace()
        }
    }
}
