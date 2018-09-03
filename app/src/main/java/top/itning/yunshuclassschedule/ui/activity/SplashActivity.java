package top.itning.yunshuclassschedule.ui.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.AppUpdate;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.http.CheckAppUpdate;
import top.itning.yunshuclassschedule.service.ApkDownloadService;
import top.itning.yunshuclassschedule.service.DataDownloadService;
import top.itning.yunshuclassschedule.service.JobSchedulerService;
import top.itning.yunshuclassschedule.util.ApkInstallUtils;
import top.itning.yunshuclassschedule.util.GlideApp;
import top.itning.yunshuclassschedule.util.HttpUtils;
import top.itning.yunshuclassschedule.util.NetWorkUtils;

/**
 * 闪屏页
 *
 * @author itning
 */
public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    private static long startTime;
    private AppUpdate appUpdate;

    @BindView(R.id.iv_splash)
    AppCompatImageView ivSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initBackGroundImage();
        startService(new Intent(this, DataDownloadService.class));
        initJobScheduler();
        if (NetWorkUtils.isNetworkConnected(this)) {
            startTime = System.currentTimeMillis();
            checkAppUpdate();
            if (!App.sharedPreferences.getBoolean(ConstantPool.Str.FIRST_IN_APP.get(), true)) {
                //非第一次进入,才进行课程表数据更新检查
                //event to DataDownloadService
                EventBus.getDefault().postSticky(new EventEntity(ConstantPool.Int.START_CHECK_CLASS_SCHEDULE_UPDATE));
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                assert notificationManager != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted()) {
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("phone_mute_status", false).apply();
                }
            }
        } else {
            Toast.makeText(this, "没有网络", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(this::enterMainActivity, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get());
        }
    }

    /**
     * 初始化背景图片
     */
    private void initBackGroundImage() {
        Display display = this.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        GlideApp
                .with(this)
                .load(R.drawable.splash_background)
                .override(size.x, size.y)
                .centerCrop()
                .into(ivSplash);
    }

    /**
     * init Job Scheduler
     */
    private void initJobScheduler() {
        Log.d(TAG, "init Job Scheduler");
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert jobScheduler != null;
        jobScheduler.cancelAll();
        JobInfo jobInfo = new JobInfo.Builder(1024, new ComponentName(getPackageName(), JobSchedulerService.class.getName()))
                //10 minutes
                .setPeriodic(10 * 60 * 1000)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .build();
        int schedule = jobScheduler.schedule(jobInfo);
        if (schedule <= 0) {
            Log.e(TAG, "schedule error！");
        }
    }

    /**
     * 检查APP更新
     */
    private void checkAppUpdate() {
        HttpUtils.getRetrofit().create(CheckAppUpdate.class).checkUpdate().enqueue(new Callback<AppUpdate>() {
            @Override
            public void onResponse(@NonNull Call<AppUpdate> call, @NonNull Response<AppUpdate> response) {
                appUpdate = response.body();
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.START_CHECK_APP_UPDATE));
            }

            @Override
            public void onFailure(@NonNull Call<AppUpdate> call, @NonNull Throwable t) {
                Log.e(TAG, "更新失败", t);
                //event to commonService
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "更新错误:" + t.toString()));
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.ENTER_HOME_ACTIVITY));
            }
        });
    }

    /**
     * 进入主Activity
     */
    private void enterMainActivity() {
        Intent intent;
        if (App.sharedPreferences.getBoolean(ConstantPool.Str.FIRST_IN_APP.get(), true)) {
            //第一次进入APP
            intent = new Intent(this, LoginActivity.class);
        } else {
            //非第一次,肯定已经登陆
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case ENTER_HOME_ACTIVITY: {
                new Handler().postDelayed(this::enterMainActivity, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get() - (System.currentTimeMillis() - startTime));
                break;
            }
            case START_CHECK_APP_UPDATE: {
                if (ApkInstallUtils.getPackageVersionCode(this) < appUpdate.getVersionCode()) {
                    //需要升级
                    startService(new Intent(this, ApkDownloadService.class));
                    applicationUpdate();
                } else {
                    new Handler().postDelayed(this::enterMainActivity, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get() - (System.currentTimeMillis() - startTime));
                }
                break;
            }
            default:
        }
    }

    /**
     * 应用升级
     */
    private void applicationUpdate() {
        new AlertDialog.Builder(this).setTitle("版本更新 " + appUpdate.getVersionName())
                .setMessage(appUpdate.getVersionDesc())
                .setCancelable(false)
                .setPositiveButton("升级",
                        (dialog, which) -> {
                            if (checkPermission()) {
                                upgradeApplication();
                            }
                        })
                .setNegativeButton("取消",
                        (dialog, which) -> enterMainActivity())
                .show();
    }

    /**
     * 检查应用权限
     *
     * @return 真为已授权
     */
    @CheckResult
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, ConstantPool.Int.INSTALL_PACKAGES_REQUEST_CODE.get());
                return false;
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this).setTitle("需要外置存储权限")
                        .setMessage("请授予外置存储权限,否则无法升级应用")
                        .setCancelable(false)
                        .setPositiveButton("确定", (dialog1, which1) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ConstantPool.Int.WRITE_EXTERNAL_STORAGE_REQUEST_CODE.get()))
                        .show();
                return false;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ConstantPool.Int.WRITE_EXTERNAL_STORAGE_REQUEST_CODE.get());
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConstantPool.Int.WRITE_EXTERNAL_STORAGE_REQUEST_CODE.get()) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPermission()) {
                    upgradeApplication();
                }
            } else {
                new AlertDialog.Builder(this).setTitle("需要权限下载升级文件")
                        .setMessage("我们需要将升级包放到外置存储中")
                        .setCancelable(false)
                        .setPositiveButton("确定", (dialog, which) -> startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)), ConstantPool.Int.APP_SETTING_REQUEST_CODE.get()))
                        .setNegativeButton("取消", (dialog, which) -> enterMainActivity())
                        .show();
            }
        }
        if (requestCode == ConstantPool.Int.INSTALL_PACKAGES_REQUEST_CODE.get()) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPermission()) {
                    upgradeApplication();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new AlertDialog.Builder(this).setTitle("需要权限安装升级文件")
                            .setMessage("我们需要安装权限")
                            .setCancelable(false)
                            .setPositiveButton("确定", (dialog, which) -> startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES), ConstantPool.Int.APP_INSTALL_UNKNOWN_APK_SETTING.get()))
                            .setNegativeButton("取消", (dialog, which) -> enterMainActivity())
                            .show();
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstantPool.Int.APP_SETTING_REQUEST_CODE.get()) {
            if (checkPermission()) {
                upgradeApplication();
            }
        }
        if (requestCode == ConstantPool.Int.APP_INSTALL_UNKNOWN_APK_SETTING.get()) {
            if (checkPermission()) {
                upgradeApplication();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 开始下载应用程序
     */
    private void upgradeApplication() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            new AlertDialog.Builder(this).setTitle("升级失败")
                    .setMessage("没找到外置存储")
                    .setCancelable(false).setPositiveButton("确定", (dialog, which) -> enterMainActivity()).show();
            return;
        }
        //event to ApkDownloadService
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.START_DOWNLOAD_UPDATE_APK, appUpdate.getDownloadUrl(), appUpdate.getVersionCode()));
        enterMainActivity();
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
