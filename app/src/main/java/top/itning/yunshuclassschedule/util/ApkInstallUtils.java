package top.itning.yunshuclassschedule.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.AnyThread;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.util.List;

import top.itning.yunshuclassschedule.BuildConfig;


/**
 * 软件安装工具
 *
 * @author itning
 */
@SuppressWarnings("unused")
public class ApkInstallUtils {
    private static final String TAG = "ApkInstallUtils";

    private ApkInstallUtils() {

    }

    /**
     * 安装APK 文件
     *
     * @param file              文件对象
     * @param appCompatActivity {@link AppCompatActivity}
     * @param newTask           是否开启新的activity栈
     * @param killMyself        调用安装时是否将自己杀死
     */
    @AnyThread
    public static void installApk(@NonNull File file, @NonNull AppCompatActivity appCompatActivity, boolean newTask, boolean killMyself) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //添加临时权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(appCompatActivity, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            List<ResolveInfo> resInfoList = appCompatActivity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                appCompatActivity.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            //设置intent的数据类型是应用程序application
            intent.setDataAndType(Uri.parse("file://" + file.toString()), "application/vnd.android.package-archive");
        }
        if (newTask) {
            //为这个新apk开启一个新的activity栈
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        //开始安装
        appCompatActivity.startActivity(intent);
        if (killMyself) {
            //关闭旧版本的应用程序的进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 获取当前应用版本
     *
     * @param appCompatActivity {@link AppCompatActivity}
     * @return 版本信息
     */
    @CheckResult
    public static String getPackageVersionName(@NonNull AppCompatActivity appCompatActivity) {
        try {
            return appCompatActivity.getPackageManager().getPackageInfo(appCompatActivity.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found:", e);
            return "";
        }
    }

    /**
     * 获取当前应用本地版本
     *
     * @param appCompatActivity {@link AppCompatActivity}
     * @return 版本信息
     */
    @CheckResult
    public static int getPackageVersionCode(@NonNull AppCompatActivity appCompatActivity) {
        try {
            return appCompatActivity.getPackageManager().getPackageInfo(appCompatActivity.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "versionCode not found:", e);
            return -1;
        }
    }
}
