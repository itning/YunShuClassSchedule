package top.itning.yunshuclassschedule.util

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.annotation.NonNull
import com.tencent.bugly.crashreport.CrashReport
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * 文件工具类
 *
 * @author itning
 */
object FileUtils {
    private const val TAG = "FileUtils"
    private const val MAX_IMAGE_FILE_SIZE = 20

    fun transferFile(@NonNull context: Context, @NonNull fromUri: Uri, @NonNull fileName: String) {
        if (!writeFile2Cache(context, fromUri)) {
            return
        }
        val file = File(context.cacheDir.toString() + File.separator + "cache")
        if (!file.canRead()) {
            Toast.makeText(context, "读取图片失败:", Toast.LENGTH_LONG).show()
            CrashReport.postCatchedException(Throwable("read image failure , file.canRead method return false"))
            return
        }
        // Get length of file in bytes
        val fileSizeInBytes = file.length()
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        val fileSizeInKB = fileSizeInBytes / 1024
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        val fileSizeInMB = fileSizeInKB / 1024
        Log.d(TAG, "file size :" + fileSizeInKB + "KB")
        if (fileSizeInMB > MAX_IMAGE_FILE_SIZE) {
            Log.d(TAG, "this image too large :" + fileSizeInMB + "MB")
            Toast.makeText(context, "图片太大了", Toast.LENGTH_LONG).show()
            return
        }
        try {
            FileInputStream(file).channel.use { inChannel -> context.openFileOutput(fileName, Context.MODE_PRIVATE).channel.use { fileOutputStreamChannel -> fileOutputStreamChannel.transferFrom(inChannel, 0, inChannel.size()) } }
        } catch (e: IOException) {
            Log.e(TAG, " ", e)
            CrashReport.postCatchedException(e)
            Toast.makeText(context, "图片写入失败", Toast.LENGTH_LONG).show()
        }

    }

    @CheckResult
    private fun writeFile2Cache(@NonNull context: Context, @NonNull fromUri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(fromUri)!!.toFile(context.cacheDir.toString() + File.separator + "cache")
            true
        } catch (e: Exception) {
            Log.e(TAG, " ", e)
            CrashReport.postCatchedException(e)
            Toast.makeText(context, "写入缓存失败:" + e.message, Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun InputStream.toFile(path: String) {
        File(path).outputStream().use { this.copyTo(it) }
    }
}


