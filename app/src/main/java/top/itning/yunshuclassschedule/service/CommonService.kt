package top.itning.yunshuclassschedule.service

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.preference.PreferenceManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.receiver.TimeTickReceiver
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.FOREGROUND_SERVICE_STATUS
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.NOW_WEEK_NUM
import top.itning.yunshuclassschedule.util.ClassScheduleUtils
import top.itning.yunshuclassschedule.util.DateUtils.getNextMondayOfTimeInMillis

/**
 * 公共服务
 *
 * @author itning
 */
class CommonService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var timeTickReceiver: TimeTickReceiver
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        Log.d(TAG, "on Create")
        EventBus.getDefault().register(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        initNotificationChannel()
        ClassScheduleUtils.startForegroundServer(this, TAG)
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        //设置了系统时区
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        //设置了系统时间
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        timeTickReceiver = TimeTickReceiver()
        registerReceiver(timeTickReceiver, filter)
    }

    /**
     * 初始化通知渠道
     *
     * @since android 8.0
     */
    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Build.VERSION.SDK_INT :" + Build.VERSION.SDK_INT + " now create Notification Channel")

            var channelId = "class_reminder"
            var channelName = "课程提醒"
            var importance = NotificationManager.IMPORTANCE_HIGH
            createNotificationChannel(channelId, channelName, importance, true)

            channelId = "foreground_service"
            channelName = "前台服务"
            importance = NotificationManager.IMPORTANCE_NONE
            createNotificationChannel(channelId, channelName, importance, false)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "on Start Command")
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        unregisterReceiver(timeTickReceiver)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.HTTP_ERROR -> {
                Toast.makeText(this, eventEntity.msg, Toast.LENGTH_LONG).show()
            }
            ConstantPool.Int.TIME_TICK_CHANGE -> {
                val currentTimeMillis = System.currentTimeMillis()
                if (isNewWeek(currentTimeMillis)) {
                    val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                    var i = defaultSharedPreferences.getString(NOW_WEEK_NUM, "1")!!.toInt()
                    var lastTimeMillis = App.sharedPreferences.getLong(ConstantPool.Str.NEXT_WEEK_OF_MONDAY.get(), currentTimeMillis)
                    while (true) {
                        if (currentTimeMillis > lastTimeMillis) {
                            Log.d(TAG, "currentTimeMillis > nextMondayOfTimeInMillis is true")
                            val nextMondayOfTimeInMillis = getNextMondayOfTimeInMillis(lastTimeMillis)
                            lastTimeMillis = nextMondayOfTimeInMillis
                            i++
                            continue
                        }
                        break
                    }
                    if (i > 50) {
                        i = 1
                    }
                    Log.d(TAG, "Set week num $i")
                    if (defaultSharedPreferences.edit().putString(NOW_WEEK_NUM, i.toString()).commit()) {
                        App.sharedPreferences.edit().putLong(ConstantPool.Str.NEXT_WEEK_OF_MONDAY.get(), lastTimeMillis).apply()
                    }
                }
            }
            else -> {

            }
        }
    }

    /**
     * 检查是不是新的一周
     */
    private fun isNewWeek(currentTimeMillis: Long): Boolean {
        val lastTimeMillis = App.sharedPreferences.getLong(ConstantPool.Str.NEXT_WEEK_OF_MONDAY.get(), currentTimeMillis)
        val isNewWeek = currentTimeMillis > lastTimeMillis
        Log.d(TAG, "Is new week ? $isNewWeek")
        return isNewWeek
    }

    /**
     * 创建通知渠道
     *
     * @param channelId   渠道ID
     * @param channelName 渠道名
     * @param importance  重要程度 [NotificationManager]
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(@NonNull channelId: String, @NonNull channelName: String, importance: Int, showBadge: Boolean) {
        Log.d(TAG, "created Notification Channel id:$channelId name:$channelName importance:$importance")
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.setShowBadge(showBadge)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == FOREGROUND_SERVICE_STATUS) {
            if (sharedPreferences.getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
                ClassScheduleUtils.startForegroundServer(this, TAG)
            } else {
                stopForeground(true)
            }
        }
    }

    companion object {
        private const val TAG = "CommonService"
    }
}
