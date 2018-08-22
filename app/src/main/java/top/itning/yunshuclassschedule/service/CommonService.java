package top.itning.yunshuclassschedule.service;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.receiver.TimeTickReceiver;

/**
 * 公共服务
 *
 * @author itning
 */
public class CommonService extends Service {
    private static final String TAG = "CommonService";
    private TimeTickReceiver timeTickReceiver;

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        //设置了系统时区
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        //设置了系统时间
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        timeTickReceiver = new TimeTickReceiver();
        registerReceiver(timeTickReceiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "download";
            String channelName = "下载通知";
            int importance = NotificationManager.IMPORTANCE_LOW;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "class_reminder";
            channelName = "课程提醒";
            importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(timeTickReceiver);
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
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }
}
