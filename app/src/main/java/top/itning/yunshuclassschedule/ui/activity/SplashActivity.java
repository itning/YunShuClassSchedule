package top.itning.yunshuclassschedule.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.itning.yunshuclassschedule.BaseApplication;
import top.itning.yunshuclassschedule.ConstantPool;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.AppUpdate;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.http.CheckAppUpdate;
import top.itning.yunshuclassschedule.http.CheckClassScheduleVersion;
import top.itning.yunshuclassschedule.http.DownloadClassSchedule;
import top.itning.yunshuclassschedule.service.CommonService;
import top.itning.yunshuclassschedule.service.DownloadService;
import top.itning.yunshuclassschedule.util.ApkInstallUtils;
import top.itning.yunshuclassschedule.util.HttpUtils;
import top.itning.yunshuclassschedule.util.NetWorkUtils;

/**
 * 闪屏页
 *
 * @author itning
 */
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    private static long startTime;
    private AppUpdate appUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        startService(new Intent(this, CommonService.class));
        if (NetWorkUtils.isNetworkConnected(this)) {
            startTime = System.currentTimeMillis();
            checkAppUpdate();
            checkClassScheduleUpdate();
        } else {
            Toast.makeText(this, "没有网络", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(this::enterMainActivity, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get());
        }
    }

    /**
     * 检查课程表数据更新
     */
    private void checkClassScheduleUpdate() {
        HttpUtils.getRetrofit().create(CheckClassScheduleVersion.class).checkVersion("2016010103").enqueue(new retrofit2.Callback<String>() {

            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == ConstantPool.Int.RESPONSE_SUCCESS_CODE.get()) {
                    SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                    String version = response.body();
                    if (!sharedPreferences.getString(ConstantPool.Str.APP_CLASS_SCHEDULE_VERSION.get(), "").equals(version)) {
                        sharedPreferences.edit().putString(ConstantPool.Str.APP_CLASS_SCHEDULE_VERSION.get(), version).apply();
                        downloadClassSchedule();
                    }
                } else {
                    Log.e(TAG, "检查课表错误" + response.code());
                    EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "服务器连接失败:" + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e(TAG, "检查课表错误", t);
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "服务器连接失败:" + t.toString()));
            }
        });
    }

    /**
     * 下载课程表
     */
    private void downloadClassSchedule() {
        HttpUtils.getRetrofit().create(DownloadClassSchedule.class).download("2016010103").enqueue(new retrofit2.Callback<List<ClassSchedule>>() {

            @Override
            public void onResponse(@NonNull Call<List<ClassSchedule>> call, @NonNull Response<List<ClassSchedule>> response) {
                if (response.code() == ConstantPool.Int.RESPONSE_SUCCESS_CODE.get()) {
                    List<ClassSchedule> classScheduleList = response.body();
                    if (classScheduleList != null) {
                        DaoSession daoSession = ((BaseApplication) getApplication()).getDaoSession();
                        ClassScheduleDao classScheduleDao = daoSession.getClassScheduleDao();
                        for (ClassSchedule classSchedule : classScheduleList) {
                            classScheduleDao.save(classSchedule);
                        }
                    }
                } else {
                    Log.e(TAG, "下载课表错误:" + response.code());
                    EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "下载课表错误:" + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ClassSchedule>> call, @NonNull Throwable t) {
                Log.e(TAG, "下载课表错误", t);
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "下载课表错误:" + t.toString()));
            }
        });
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
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "更新错误:" + t.toString()));
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.ENTER_HOME_ACTIVITY));
            }
        });
    }

    /**
     * 进入主Activity
     */
    private void enterMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * 消息事件
     *
     * @param eventEntity event
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case ENTER_HOME_ACTIVITY: {
                new Handler().postDelayed(this::enterMainActivity, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get() - (System.currentTimeMillis() - startTime));
                break;
            }
            case START_CHECK_APP_UPDATE: {
                if (ApkInstallUtils.getPackageVersionCode(this) != appUpdate.getVersionCode()) {
                    //需要升级
                    startService(new Intent(this, DownloadService.class));
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstantPool.Int.APP_SETTING_REQUEST_CODE.get()) {
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
