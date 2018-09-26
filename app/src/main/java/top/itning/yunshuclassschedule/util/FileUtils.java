package top.itning.yunshuclassschedule.util;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * 文件工具类
 *
 * @author itning
 */
public class FileUtils {
    private static final String TAG = "FileUtils";
    public static final int MAX_IMAGE_FILE_SIZE = 20;
    private static final int CACHE_BYTES_SIZE = 4096;

    private FileUtils() {
    }

    public static void transferFile(@NonNull Context context, @NonNull Uri fromUri, @NonNull String fileName) {
        if (!writeFile2Cache(context, fromUri)) {
            return;
        }
        File file = new File(context.getCacheDir() + File.separator + "cache");
        if (!file.canRead()) {
            Toast.makeText(context, "读取图片失败:", Toast.LENGTH_LONG).show();
            CrashReport.postCatchedException(new Throwable("read image failure , file.canRead method return false"));
            return;
        }
        // Get length of file in bytes
        long fileSizeInBytes = file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;
        Log.d(TAG, "file size :" + fileSizeInKB + "KB");
        if (fileSizeInMB > MAX_IMAGE_FILE_SIZE) {
            Log.d(TAG, "this image too large :" + fileSizeInMB + "MB");
            Toast.makeText(context, "图片太大了", Toast.LENGTH_LONG).show();
            return;
        }
        try (FileChannel inChannel = new FileInputStream(file).getChannel();
             FileChannel fileOutputStreamChannel = context.openFileOutput(fileName, Context.MODE_PRIVATE).getChannel()) {
            fileOutputStreamChannel.transferFrom(inChannel, 0, inChannel.size());
        } catch (IOException e) {
            Log.e(TAG, " ", e);
            CrashReport.postCatchedException(e);
            Toast.makeText(context, "图片写入失败", Toast.LENGTH_LONG).show();
        }
    }

    @CheckResult
    private static boolean writeFile2Cache(@NonNull Context context, @NonNull Uri fromUri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(fromUri);
             OutputStream os = new FileOutputStream(context.getCacheDir() + File.separator + "cache")) {
            if (inputStream == null) {
                throw new NullPointerException("inputStream is null");
            }
            int bytesRead;
            byte[] buffer = new byte[CACHE_BYTES_SIZE];
            while ((bytesRead = inputStream.read(buffer, 0, CACHE_BYTES_SIZE)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            Log.e(TAG, " ", e);
            CrashReport.postCatchedException(e);
            Toast.makeText(context, "写入缓存失败:" + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
