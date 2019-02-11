package top.itning.yunshuclassschedule.service

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.tencent.bugly.crashreport.CrashReport
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.ClassSchedule
import top.itning.yunshuclassschedule.entity.ClassScheduleDao
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.receiver.RemindReceiver
import top.itning.yunshuclassschedule.ui.activity.MainActivity
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.CLASS_REMINDER_DOWN_TIME
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.CLASS_REMINDER_UP_TIME
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.FOREGROUND_SERVICE_STATUS
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.PHONE_MUTE_AFTER_TIME
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.PHONE_MUTE_BEFORE_TIME
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.PHONE_MUTE_STATUS
import top.itning.yunshuclassschedule.util.ClassScheduleUtils
import top.itning.yunshuclassschedule.util.DateUtils
import java.text.ParseException
import java.util.*

/**
 * 提醒服务
 *
 * @author itning
 */
class RemindService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var classScheduleList: MutableList<ClassSchedule>
    private lateinit var sharedPreferences: SharedPreferences
    private val calendar = Calendar.getInstance()
    private lateinit var alarmManager: AlarmManager
    private lateinit var powerManager: PowerManager
    @Volatile
    private var classReminderDownStatus: Boolean = false
    @Volatile
    private var classReminderUpStatus: Boolean = false
    @Volatile
    private var phoneMuteStatus: Boolean = false
    private var phoneMuteAfterTime: Int = 0
    private var phoneMuteBeforeTime: Int = 0
    private var classReminderUpTime: Int = 0
    private var classReminderDownTime: Int = 0
    private val upTimeList: MutableList<Intent> = mutableListOf()
    private val downTimeList: MutableList<Intent> = mutableListOf()
    private val pendingIntentList: MutableList<PendingIntent> = mutableListOf()
    private val timeList: MutableList<Long> = mutableListOf()

    /**
     * 判断是否是新的一天
     *
     * @return true is new day
     */
    private val isNewDay: Boolean
        get() {
            val last = App.sharedPreferences.getInt(REMIND_SERVICE_NEW_DAY, 0)
            val i = Calendar.getInstance().get(Calendar.DATE)
            App.sharedPreferences.edit().putInt(REMIND_SERVICE_NEW_DAY, i).apply()
            Log.d(TAG, "need refresh : " + (last != i))
            return last != 0 && last != i
        }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d(TAG, "on Create")
        EventBus.getDefault().register(this)
        startForegroundServer()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        initData()
        super.onCreate()
    }

    /**
     * 开启前台服务
     */
    private fun startForegroundServer() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
            return
        }
        Log.d(TAG, "start Foreground Server")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        //用ComponentName得到class对象
        intent.component = ComponentName(this, MainActivity::class.java)
        // 关键的一步，设置启动模式，两种情况
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val pendingIntent = PendingIntent.getActivity(this, 88, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, "foreground_service")
                .setContentTitle("云舒课表")
                .setContentText("提醒服务正在运行")
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.logo))
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
        val notification = builder.build()
        startForeground(111, notification)
    }

    override fun onDestroy() {
        Log.d(TAG, "on Destroy")
        EventBus.getDefault().unregister(this)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "on Start Command")
        return Service.START_REDELIVER_INTENT
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.TIME_TICK_CHANGE -> {
                if (isNewDay) {
                    initData()
                }
            }
            ConstantPool.Int.PHONE_MUTE_OPEN -> {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
            ConstantPool.Int.PHONE_MUTE_CANCEL -> {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            ConstantPool.Int.CLASS_UP_REMIND -> {
                val classSchedule = eventEntity.data as ClassSchedule
                sendNotification("上课提醒", classSchedule.name + " " + classSchedule.location)
            }
            ConstantPool.Int.CLASS_DOWN_REMIND -> {
                sendNotification("下课提醒", "快要下课了")
            }
            ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT -> run { initData() }
            else -> {
            }
        }
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        Log.d(TAG, "start init data")
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ":initData")
        wakeLock.setReferenceCounted(false)
        wakeLock.acquire((5 * 60 * 1000).toLong())
        classReminderUpStatus = sharedPreferences.getBoolean(CLASS_REMINDER_UP_STATUS, true)
        classReminderDownStatus = sharedPreferences.getBoolean(CLASS_REMINDER_DOWN_STATUS, true)
        phoneMuteStatus = sharedPreferences.getBoolean(PHONE_MUTE_STATUS, false)
        phoneMuteBeforeTime = sharedPreferences.getString(PHONE_MUTE_BEFORE_TIME, "0")!!.toInt()
        phoneMuteAfterTime = sharedPreferences.getString(PHONE_MUTE_AFTER_TIME, "0")!!.toInt()
        classReminderUpTime = sharedPreferences.getString(CLASS_REMINDER_UP_TIME, "1")!!.toInt()
        classReminderDownTime = sharedPreferences.getString(CLASS_REMINDER_DOWN_TIME, "1")!!.toInt()
        initClassScheduleList()
        obsoleteClear()
        if (!classScheduleList.isEmpty()) {
            clearAlarm()
            initTimeList()
            initPendingIntentList()
            addToAlarm()
        }
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun clearAlarm() {
        Log.d(TAG, "start clear alarm")
        var i = 0
        for (p in pendingIntentList) {
            alarmManager.cancel(p)
            i++
        }
        Log.d(TAG, "cancel $i alarm !")
    }

    private fun initPendingIntentList() {
        Log.d(TAG, "start init pending intent list")
        pendingIntentList.clear()
        timeList.clear()
        var requestCode = 0
        for (upIntent in upTimeList) {
            val time = upIntent.getLongExtra("time", -1)
            if (time == -1L) {
                throw RuntimeException("time is -1")
            }
            timeList.add(time)
            Log.d(TAG, "time list add $time")
            val pendingIntent = PendingIntent.getBroadcast(this, requestCode, upIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            pendingIntentList.add(pendingIntent)
            requestCode++
        }
        for (downIntent in downTimeList) {
            val time = downIntent.getLongExtra("time", -1)
            if (time == -1L) {
                throw RuntimeException("time is -1")
            }
            timeList.add(time)
            Log.d(TAG, "time list add $time")
            val pendingIntent = PendingIntent.getBroadcast(this, requestCode, downIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            pendingIntentList.add(pendingIntent)
            requestCode++
        }
        Log.d(TAG, "finish init pending intent list. pendingIntentList size:" + pendingIntentList.size + " timeList size:" + timeList.size)
    }

    /**
     * 初始化今天课程
     */
    private fun initClassScheduleList() {
        Log.d(TAG, "init class schedule list data")
        val nowWeekNum = (PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!.toInt() - 1).toString()
        val daoSession = (application as App).daoSession
        classScheduleList = daoSession
                .classScheduleDao
                .queryBuilder()
                .where(ClassScheduleDao.Properties.Week.eq(DateUtils.week))
                .list()
                .filter { ClassScheduleUtils.isThisWeekOfClassSchedule(it, nowWeekNum) }
                .toMutableList()
        Log.d(TAG, "init class schedule list size:" + classScheduleList.size)
    }

    private fun obsoleteClear() {
        Log.d(TAG, "start obsolete clear list")
        try {
            val timeList = DateUtils.timeList
            val tempList = ArrayList<ClassSchedule>()
            for (c in classScheduleList) {
                val timeArray = timeList[c.section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val endTime = DateUtils.DF.parse(timeArray[1]).time
                val nowTime = DateUtils.DF.parse(DateUtils.DF.format(Date())).time
                if (endTime >= nowTime) {
                    tempList.add(c)
                    Log.d(TAG, "add section " + c.section + " time:" + timeArray[1] + " endTime:" + endTime + " nowTime:" + nowTime)
                }
            }
            Log.d(TAG, "end obsolete clear list now size:" + tempList.size)
            classScheduleList.clear()
            classScheduleList.addAll(tempList)
            tempList.clear()
        } catch (e: ParseException) {
            Log.e(TAG, " ", e)
            CrashReport.postCatchedException(e)
        }
    }

    /**
     * 初始化时间数据
     */
    private fun initTimeList() {
        Log.d(TAG, "start init time list")
        val defaultTimeList = DateUtils.timeList
        upTimeList.clear()
        downTimeList.clear()
        for (classSchedule in classScheduleList) {
            val timeArray = defaultTimeList[classSchedule.section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (phoneMuteStatus) {
                initIntent(upTimeList, downTimeList, classSchedule, timeArray, phoneMuteBeforeTime, phoneMuteAfterTime, "phone_mute", haveBefore = true, haveAfter = true)
            }
            if (classReminderUpStatus) {
                initIntent(upTimeList, downTimeList, classSchedule, timeArray, classReminderUpTime, 0, "class_reminder_up", haveBefore = true, haveAfter = false)
            }
            if (classReminderDownStatus) {
                initIntent(upTimeList, downTimeList, classSchedule, timeArray, 0, classReminderDownTime, "class_reminder_down", haveBefore = false, haveAfter = true)
            }
        }
    }

    private fun initIntent(upTimeList: MutableList<Intent>, downTimeList: MutableList<Intent>, classSchedule: ClassSchedule, timeArray: Array<String>, beforeTime: Int, afterTime: Int, type: String, haveBefore: Boolean, haveAfter: Boolean) {
        try {
            val up = timeArray[0].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val down = timeArray[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val timeArr = DateUtils.timeList[classSchedule.section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (haveBefore && DateUtils.DF.parse(timeArr[0]).time > DateUtils.DF.parse(DateUtils.DF.format(Date())).time) {
                val upIntent = Intent(this, RemindReceiver::class.java)
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(up[0]))
                calendar.set(Calendar.MINUTE, Integer.parseInt(up[1]))
                calendar.add(Calendar.MINUTE, 0 - beforeTime)
                upIntent.putExtra("time", calendar.timeInMillis)
                upIntent.putExtra("type", type)
                upIntent.putExtra("status", 0)
                upIntent.putExtra("name", classSchedule.name)
                upIntent.putExtra("location", classSchedule.location)
                upIntent.putExtra("section", classSchedule.section)
                upIntent.putExtra("week", classSchedule.week)
                upTimeList.add(upIntent)
                Log.d(TAG, "add up time list " + type + " at " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE))
            }
            if (haveAfter) {
                val downIntent = Intent(this, RemindReceiver::class.java)
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(down[0]))
                calendar.set(Calendar.MINUTE, Integer.parseInt(down[1]))
                calendar.add(Calendar.MINUTE, 0 - afterTime)
                downIntent.putExtra("time", calendar.timeInMillis)
                downIntent.putExtra("type", type)
                downIntent.putExtra("status", 1)
                downIntent.putExtra("name", classSchedule.name)
                downIntent.putExtra("location", classSchedule.location)
                downIntent.putExtra("section", classSchedule.section)
                downIntent.putExtra("week", classSchedule.week)
                downTimeList.add(downIntent)
                Log.d(TAG, "add down time list " + type + " at " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE))
            }
        } catch (e: ParseException) {
            Log.e(TAG, " ", e)
            CrashReport.postCatchedException(e)
        }

    }

    /**
     * 添加到提醒
     */
    private fun addToAlarm() {
        Log.d(TAG, "start add to alarm")
        var index = 0
        pendingIntentList.forEach {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeList[index], it)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeList[index], it)
            }
            calendar.timeInMillis = timeList[index]
            Log.d(TAG, "add alarm " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE))
            index++
        }
        Log.d(TAG, "add $index task")
    }

    /**
     * 发送通知
     *
     * @param contentTitle 标题
     * @param contentText  内容
     */
    private fun sendNotification(contentTitle: String, contentText: String) {
        Log.d(TAG, "now send notification")
        // 设置启动的程序，如果存在则找出，否则新的启动
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        //用ComponentName得到class对象
        intent.component = ComponentName(this, MainActivity::class.java)
        // 关键的一步，设置启动模式，两种情况
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val pendingIntent = PendingIntent.getActivity(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, "class_reminder")
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.logo)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.logo))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
        val notification = builder.build()
        notificationManager.notify(2, notification)
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == FOREGROUND_SERVICE_STATUS) {
            if (sharedPreferences.getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
                startForegroundServer()
            } else {
                stopForeground(true)
            }
        }
        if (key == CLASS_REMINDER_DOWN_STATUS
                || key == CLASS_REMINDER_UP_STATUS
                || key == PHONE_MUTE_STATUS
                || key == PHONE_MUTE_BEFORE_TIME
                || key == PHONE_MUTE_AFTER_TIME
                || key == CLASS_REMINDER_UP_TIME
                || key == CLASS_REMINDER_DOWN_TIME) {
            Log.d(TAG, "Preference Changed , now Init Data")
            initData()
        }
    }

    companion object {
        private const val TAG = "RemindService"
        private const val REMIND_SERVICE_NEW_DAY = "remind_service_new_day"
        private const val CLASS_REMINDER_UP_STATUS = "class_reminder_up_status"
        private const val CLASS_REMINDER_DOWN_STATUS = "class_reminder_down_status"
    }
}
