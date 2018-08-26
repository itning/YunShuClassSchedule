package top.itning.yunshuclassschedule.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.receiver.TimeTickReceiver;
import top.itning.yunshuclassschedule.ui.activity.MainActivity;

import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.FOREGROUND_SERVICE_STATUS;

/**
 * 公共服务
 *
 * @author itning
 */
public class CommonService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "CommonService";
    private TimeTickReceiver timeTickReceiver;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        Log.d(TAG, "on Create");
        EventBus.getDefault().register(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        initNotificationChannel();
        startForegroundServer();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        //设置了系统时区
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        //设置了系统时间
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        timeTickReceiver = new TimeTickReceiver();
        registerReceiver(timeTickReceiver, filter);
    }

    /**
     * 初始化通知渠道
     *
     * @since android 8.0
     */
    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Build.VERSION.SDK_INT :" + Build.VERSION.SDK_INT + " now create Notification Channel");
            String channelId = "download";
            String channelName = "下载通知";
            int importance = NotificationManager.IMPORTANCE_LOW;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "class_reminder";
            channelName = "课程提醒";
            importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "foreground_service";
            channelName = "前台服务";
            importance = NotificationManager.IMPORTANCE_MIN;
            createNotificationChannel(channelId, channelName, importance);
        }
    }

    /**
     * 开启前台服务
     */
    private void startForegroundServer() {
        Log.d(TAG, "start Foreground Server");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //用ComponentName得到class对象
        intent.setComponent(new ComponentName(this, MainActivity.class));
        // 关键的一步，设置启动模式，两种情况
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 88, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "foreground_service")
                .setContentTitle("云舒课表")
                .setContentText("提醒服务正在运行")
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setSmallIcon(this.getApplicationInfo().icon)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        Notification notification = builder.build();
        startForeground(111, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on Start Command");
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(timeTickReceiver);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case HTTP_ERROR: {
                Toast.makeText(this, eventEntity.getMsg(), Toast.LENGTH_LONG).show();
                break;
            }
            default:
        }
    }

    /**
     * 创建通知渠道
     *
     * @param channelId   渠道ID
     * @param channelName 渠道名
     * @param importance  重要程度 {@link NotificationManager}
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(@NonNull String channelId, @NonNull String channelName, int importance) {
        Log.d(TAG, "created Notification Channel id:" + channelId + " name:" + channelName + " importance:" + importance);
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(FOREGROUND_SERVICE_STATUS)) {
            if (sharedPreferences.getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
                startForegroundServer();
            } else {
                stopForeground(true);
            }
        }
    }
}
