package top.itning.yunshuclassschedule.service;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import top.itning.yunshuclassschedule.entity.EventEntity;

/**
 * 公共服务
 *
 * @author itning
 */
public class CommonService extends Service {
    private static final String TAG = "CommonService";

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
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
