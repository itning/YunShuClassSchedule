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

/**
 * 上下课提醒通知
 *
 * @author itning
 */
public class ClassReminderService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "ClassReminderService";
    public static final String CLASS_REMINDER_UP_STATUS = "class_reminder_up_status";
    public static final String CLASS_REMINDER_DOWN_STATUS = "class_reminder_down_status";
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
        Log.d(TAG, "Class Reminder Service On Destroy");
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
            case CLASS_UP_TIME_CHANGE: {
                timeUpChange(Integer.parseInt(eventEntity.getMsg()));
                break;
            }
            case CLASS_DOWN_TIME_CHANGE: {
                timeDownChange(Integer.parseInt(eventEntity.getMsg()));
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
        classReminderUpStatus = defaultSharedPreferences.getBoolean(CLASS_REMINDER_UP_STATUS, true);
        classReminderDownStatus = defaultSharedPreferences.getBoolean(CLASS_REMINDER_DOWN_STATUS, true);
    }

    /**
     * 离上课分钟改变
     *
     * @param time 离上课分钟
     */
    private void timeUpChange(int time) {
        if (classReminderUpStatus) {
            int classReminderUpTime = Integer.parseInt(defaultSharedPreferences.getString("class_reminder_up_time", "1"));
            if (classReminderUpTime == time) {
                sendNotification("上课提醒", "离上课还有" + classReminderUpTime + "分钟");
            }
        }
    }

    /**
     * 离下课分钟改变
     *
     * @param time 离下课分钟
     */
    private void timeDownChange(int time) {
        if (classReminderDownStatus) {
            int classReminderDownTime = Integer.parseInt(defaultSharedPreferences.getString("class_reminder_down_time", "1"));
            if (classReminderDownTime == time) {
                sendNotification("下课提醒", "离下课还有" + classReminderDownTime + "分钟");
            }
        }
    }

    /**
     * 发送通知
     *
     * @param contentTitle 标题
     * @param contentText  内容
     */
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
        if (CLASS_REMINDER_UP_STATUS.equals(key)) {
            classReminderUpStatus = sharedPreferences.getBoolean(key, true);
        }
        if (CLASS_REMINDER_DOWN_STATUS.equals(key)) {
            classReminderDownStatus = sharedPreferences.getBoolean(key, true);
        }
    }
}
