package top.itning.yunshuclassschedule.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.receiver.RemindReceiver;
import top.itning.yunshuclassschedule.ui.activity.MainActivity;
import top.itning.yunshuclassschedule.util.DateUtils;

import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.CLASS_REMINDER_DOWN_TIME;
import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.CLASS_REMINDER_UP_TIME;
import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.PHONE_MUTE_AFTER_TIME;
import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.PHONE_MUTE_BEFORE_TIME;
import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.PHONE_MUTE_STATUS;

/**
 * 提醒服务
 *
 * @author itning
 */
public class RemindService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "RemindService";
    private static final String REMIND_SERVICE_NEW_DAY = "remind_service_new_day";
    private static final String CLASS_REMINDER_UP_STATUS = "class_reminder_up_status";
    private static final String CLASS_REMINDER_DOWN_STATUS = "class_reminder_down_status";
    private List<ClassSchedule> classScheduleList;
    private SharedPreferences sharedPreferences;
    private final Calendar calendar = Calendar.getInstance();
    private AlarmManager alarmManager;
    private PowerManager powerManager;
    private volatile boolean classReminderDownStatus;
    private volatile boolean classReminderUpStatus;
    private volatile boolean phoneMuteStatus;
    private int phoneMuteAfterTime;
    private int phoneMuteBeforeTime;
    private int classReminderUpTime;
    private int classReminderDownTime;
    private List<Intent> upTimeList;
    private List<Intent> downTimeList;
    private List<PendingIntent> pendingIntentList;
    private List<Long> timeList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "on Create");
        EventBus.getDefault().register(this);
        startForegroundServer();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        initData();
        super.onCreate();
    }

    /**
     * 开启前台服务
     *
     * @since android 8.0
     */
    private void startForegroundServer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "on Destroy");
        EventBus.getDefault().unregister(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on Start Command");
        return START_REDELIVER_INTENT;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case TIME_TICK_CHANGE: {
                if (isNewDay()) {
                    initData();
                }
                break;
            }
            case PHONE_MUTE_OPEN: {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                break;
            }
            case PHONE_MUTE_CANCEL: {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
                break;
            }
            case CLASS_UP_REMIND: {
                ClassSchedule classSchedule = (ClassSchedule) eventEntity.getData();
                sendNotification("上课提醒", classSchedule.getName() + " " + classSchedule.getLocation());
                break;
            }
            case CLASS_DOWN_REMIND: {
                sendNotification("下课提醒", "快要下课了");
                break;
            }
            case END_CHECK_CLASS_SCHEDULE_UPDATE: {
                initData();
                break;
            }
            default:
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Log.d(TAG, "start init data");
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "initData");
        wakeLock.acquire(5 * 60 * 1000);
        classReminderUpStatus = sharedPreferences.getBoolean(CLASS_REMINDER_UP_STATUS, true);
        classReminderDownStatus = sharedPreferences.getBoolean(CLASS_REMINDER_DOWN_STATUS, true);
        phoneMuteStatus = sharedPreferences.getBoolean(PHONE_MUTE_STATUS, false);
        phoneMuteBeforeTime = Integer.parseInt(sharedPreferences.getString(PHONE_MUTE_BEFORE_TIME, "0"));
        phoneMuteAfterTime = Integer.parseInt(sharedPreferences.getString(PHONE_MUTE_AFTER_TIME, "0"));
        classReminderUpTime = Integer.parseInt(sharedPreferences.getString(CLASS_REMINDER_UP_TIME, "1"));
        classReminderDownTime = Integer.parseInt(sharedPreferences.getString(CLASS_REMINDER_DOWN_TIME, "1"));
        initClassScheduleList();
        obsoleteClear();
        if (!classScheduleList.isEmpty()) {
            clearAlarm();
            initTimeList();
            initPendingIntentList();
            addToAlarm();
        }
        wakeLock.release();
    }

    private void clearAlarm() {
        Log.d(TAG, "start clear alarm");
        int i = 0;
        if (pendingIntentList != null) {
            for (PendingIntent p : pendingIntentList) {
                alarmManager.cancel(p);
                i++;
            }
        }
        Log.d(TAG, "cancel " + i + " alarm !");
    }

    private void initPendingIntentList() {
        Log.d(TAG, "start init pending intent list");
        pendingIntentList = new ArrayList<>();
        timeList = new ArrayList<>();
        int requestCode = 0;
        for (Intent upIntent : upTimeList) {
            long time = upIntent.getLongExtra("time", -1);
            if (time == -1) {
                throw new RuntimeException("time is -1");
            }
            timeList.add(time);
            Log.d(TAG, "time list add " + time);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, upIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntentList.add(pendingIntent);
            requestCode++;
        }
        for (Intent downIntent : downTimeList) {
            long time = downIntent.getLongExtra("time", -1);
            if (time == -1) {
                throw new RuntimeException("time is -1");
            }
            timeList.add(time);
            Log.d(TAG, "time list add " + time);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, downIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntentList.add(pendingIntent);
            requestCode++;
        }
        Log.d(TAG, "finish init pending intent list. pendingIntentList size:" + pendingIntentList.size() + " timeList size:" + timeList.size());
    }

    /**
     * 初始化今天课程
     */
    private void initClassScheduleList() {
        Log.d(TAG, "init class schedule list data");
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        classScheduleList = daoSession
                .getClassScheduleDao()
                .queryBuilder()
                .where(ClassScheduleDao.Properties.Week.eq(DateUtils.getWeek()))
                .list();
        Log.d(TAG, "init class schedule list size:" + classScheduleList.size());
    }

    private void obsoleteClear() {
        if (classScheduleList != null) {
            Log.d(TAG, "start obsolete clear list");
            try {
                List<String> timeList = DateUtils.getTimeList();
                List<ClassSchedule> tempList = new ArrayList<>();
                for (ClassSchedule c : classScheduleList) {
                    String[] timeArray = timeList.get(c.getSection() - 1).split("-");
                    long endTime = DateUtils.DF.parse(timeArray[1]).getTime();
                    long nowTime = DateUtils.DF.parse(DateUtils.DF.format(new Date())).getTime();
                    if (endTime >= nowTime) {
                        tempList.add(c);
                        Log.d(TAG, "add section " + c.getSection() + " time:" + timeArray[1] + " endTime:" + endTime + " nowTime:" + nowTime);
                    }
                }
                Log.d(TAG, "end obsolete clear list now size:" + tempList.size());
                classScheduleList.clear();
                classScheduleList.addAll(tempList);
                tempList.clear();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化时间数据
     */
    private void initTimeList() {
        Log.d(TAG, "start init time list");
        List<String> defaultTimeList = DateUtils.getTimeList();
        upTimeList = new ArrayList<>();
        downTimeList = new ArrayList<>();
        for (ClassSchedule classSchedule : classScheduleList) {
            String[] timeArray = defaultTimeList.get(classSchedule.getSection() - 1).split("-");
            if (phoneMuteStatus) {
                initIntent(upTimeList, downTimeList, classSchedule, timeArray, phoneMuteBeforeTime, phoneMuteAfterTime, "phone_mute", true, true);
            }
            if (classReminderUpStatus) {
                initIntent(upTimeList, downTimeList, classSchedule, timeArray, classReminderUpTime, 0, "class_reminder_up", true, false);
            }
            if (classReminderDownStatus) {
                initIntent(upTimeList, downTimeList, classSchedule, timeArray, 0, classReminderDownTime, "class_reminder_down", false, true);
            }
        }
    }

    private void initIntent(List<Intent> upTimeList, List<Intent> downTimeList, ClassSchedule classSchedule, String[] timeArray, int beforeTime, int afterTime, String type, boolean haveBefore, boolean haveAfter) {
        try {
            String[] up = timeArray[0].split(":");
            String[] down = timeArray[1].split(":");
            String[] timeArr = DateUtils.getTimeList().get(classSchedule.getSection() - 1).split("-");
            if (haveBefore && DateUtils.DF.parse(timeArr[0]).getTime() > DateUtils.DF.parse(DateUtils.DF.format(new Date())).getTime()) {
                Intent upIntent = new Intent(this, RemindReceiver.class);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(up[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(up[1]));
                calendar.add(Calendar.MINUTE, 0 - beforeTime);
                upIntent.putExtra("time", calendar.getTimeInMillis());
                upIntent.putExtra("type", type);
                upIntent.putExtra("status", 0);
                upIntent.putExtra("name", classSchedule.getName());
                upIntent.putExtra("location", classSchedule.getLocation());
                upIntent.putExtra("section", classSchedule.getSection());
                upIntent.putExtra("week", classSchedule.getWeek());
                upTimeList.add(upIntent);
                Log.d(TAG, "add up time list " + type + " at " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
            }
            if (haveAfter) {
                Intent downIntent = new Intent(this, RemindReceiver.class);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(down[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(down[1]));
                calendar.add(Calendar.MINUTE, 0 - afterTime);
                downIntent.putExtra("time", calendar.getTimeInMillis());
                downIntent.putExtra("type", type);
                downIntent.putExtra("status", 1);
                downIntent.putExtra("name", classSchedule.getName());
                downIntent.putExtra("location", classSchedule.getLocation());
                downIntent.putExtra("section", classSchedule.getSection());
                downIntent.putExtra("week", classSchedule.getWeek());
                downTimeList.add(downIntent);
                Log.d(TAG, "add down time list " + type + " at " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加到提醒
     */
    private void addToAlarm() {
        Log.d(TAG, "start add to alarm");
        int index = 0;
        for (PendingIntent p : pendingIntentList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeList.get(index), p);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeList.get(index), p);
            }
            calendar.setTimeInMillis(timeList.get(index));
            Log.d(TAG, "add alarm " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
            index++;
        }
        Log.d(TAG, "add " + index + " task");
    }

    /**
     * 判断是否是新的一天
     *
     * @return true is new day
     */
    private boolean isNewDay() {
        int last = App.sharedPreferences.getInt(REMIND_SERVICE_NEW_DAY, 0);
        int i = Calendar.getInstance().get(Calendar.DATE);
        App.sharedPreferences.edit().putInt(REMIND_SERVICE_NEW_DAY, i).apply();
        Log.d(TAG, "need refresh : " + (last != i));
        return last != 0 && last != i;
    }

    /**
     * 发送通知
     *
     * @param contentTitle 标题
     * @param contentText  内容
     */
    private void sendNotification(String contentTitle, String contentText) {
        Log.d(TAG, "now send notification");
        // 设置启动的程序，如果存在则找出，否则新的启动
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //用ComponentName得到class对象
        intent.setComponent(new ComponentName(this, MainActivity.class));
        // 关键的一步，设置启动模式，两种情况
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "class_reminder")
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(this.getApplicationInfo().icon)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        Notification notification = builder.build();
        assert notificationManager != null;
        notificationManager.notify(2, notification);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(CLASS_REMINDER_DOWN_STATUS)
                || key.equals(CLASS_REMINDER_UP_STATUS)
                || key.equals(PHONE_MUTE_STATUS)
                || key.equals(PHONE_MUTE_BEFORE_TIME)
                || key.equals(PHONE_MUTE_AFTER_TIME)
                || key.equals(CLASS_REMINDER_UP_TIME)
                || key.equals(CLASS_REMINDER_DOWN_TIME)) {
            Log.d(TAG, "Preference Changed , now Init Data");
            initData();
        }
    }
}
