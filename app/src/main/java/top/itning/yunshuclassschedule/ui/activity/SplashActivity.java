package top.itning.yunshuclassschedule.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import top.itning.yunshuclassschedule.BaseApplication;
import top.itning.yunshuclassschedule.ConstantPool;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.http.CheckClassScheduleVersion;
import top.itning.yunshuclassschedule.http.DownloadClassSchedule;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
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
                    EventBus.getDefault().post(new EventEntity(ConstantPool.Int.DOWNLOAD_CLASS_SCHEDULE_ERROR, "服务器连接失败:" + response.code()));
                }
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.ENTER_HOME_ACTIVITY));
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e(TAG, "检查课表错误", t);
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.DOWNLOAD_CLASS_SCHEDULE_ERROR, "服务器连接失败:" + t.toString()));
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.ENTER_HOME_ACTIVITY));
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
                    EventBus.getDefault().post(new EventEntity(ConstantPool.Int.DOWNLOAD_CLASS_SCHEDULE_ERROR, "下载课表错误:" + response.code()));
                }
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.ENTER_HOME_ACTIVITY));
            }

            @Override
            public void onFailure(@NonNull Call<List<ClassSchedule>> call, @NonNull Throwable t) {
                Log.e(TAG, "下载课表错误", t);
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.DOWNLOAD_CLASS_SCHEDULE_ERROR, "下载课表错误:" + t.toString()));
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.ENTER_HOME_ACTIVITY));
            }
        });
    }

    /**
     * 检查APP更新
     */
    private void checkAppUpdate() {

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
            case DOWNLOAD_CLASS_SCHEDULE_ERROR: {
                Toast.makeText(this, eventEntity.getMsg(), Toast.LENGTH_LONG).show();
                break;
            }
            case ENTER_HOME_ACTIVITY: {
                new Handler().postDelayed(this::enterMainActivity, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get() - (System.currentTimeMillis() - startTime));
                break;
            }
            default:
        }
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
