package top.itning.yunshuclassschedule.util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 文件工具类
 *
 * @author itning
 */
@SuppressWarnings("unused")
public class FileUtils {
    private static final String TAG = "FileUtils";

    private FileUtils() {
    }

    public static void transferFile(@NonNull Context context, @NonNull Uri fromUri, @NonNull String fileName) {
        try {
            FileChannel inChannel = new FileInputStream(new File(getRealPathFromUri(context, fromUri))).getChannel();
            FileChannel fileOutputStreamChannel = context.openFileOutput(fileName, Context.MODE_PRIVATE).getChannel();
            fileOutputStreamChannel.transferFrom(inChannel, 0, inChannel.size());
            fileOutputStreamChannel.close();
            inChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        @SuppressLint("Recycle")
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
