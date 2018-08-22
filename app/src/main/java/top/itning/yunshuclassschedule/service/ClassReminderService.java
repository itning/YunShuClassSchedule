package top.itning.yunshuclassschedule.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.ClassScheduleUtils;
import top.itning.yunshuclassschedule.util.DateUtils;

/**
 * 上下课提醒通知
 *
 * @author itning
 */
public class ClassReminderService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "ClassReminderService";
    private SharedPreferences defaultSharedPreferences;
    private boolean classReminderDownStatus;
    private boolean classReminderUpStatus;

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.START_CLASS_REMINDER_WORK));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case START_CLASS_REMINDER_WORK: {
                start();
                break;
            }
            case TIME_TICK_CHANGE: {
                timeChange();
                break;
            }
            default:
        }
    }

    /**
     * 开始
     */
    private void start() {
        Log.d(TAG, "run thread name:" + Thread.currentThread().getName());
        classReminderUpStatus = defaultSharedPreferences.getBoolean("class_reminder_up_status", true);
        classReminderDownStatus = defaultSharedPreferences.getBoolean("class_reminder_down_status", true);
        sendNotification("测试标题", "测试内容");
    }

    public void timeChange() {
        if (classReminderUpStatus) {
            //开启了上课提醒通知
            int classReminderUpTime = Integer.parseInt(defaultSharedPreferences.getString("class_reminder_up_time", "1"));

        }
        if (classReminderDownStatus) {
            //开启了下课提醒通知
        }
    }

    private void sendNotification(String contentTitle, String contentText) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "class_reminder")
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setSmallIcon(this.getApplicationInfo().icon)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        Notification notification = builder.build();
        assert notificationManager != null;
        notificationManager.notify(2, notification);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
