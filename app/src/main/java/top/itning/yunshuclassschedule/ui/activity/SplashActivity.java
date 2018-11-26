package top.itning.yunshuclassschedule.ui.activity;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.service.DataDownloadService;
import top.itning.yunshuclassschedule.service.JobSchedulerService;
import top.itning.yunshuclassschedule.util.GlideApp;
import top.itning.yunshuclassschedule.util.NetWorkUtils;

/**
 * 闪屏页
 *
 * @author itning
 */
public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    private static long startTime;

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
            new Handler().postDelayed(this::enterMainActivity, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get() - (System.currentTimeMillis() - startTime));
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
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .build();
        int schedule = jobScheduler.schedule(jobInfo);
        if (schedule <= 0) {
            Log.e(TAG, "schedule error！");
        }
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
