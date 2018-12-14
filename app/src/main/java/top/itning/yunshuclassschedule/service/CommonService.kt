package top.itning.yunshuclassschedule.service

import android.annotation.TargetApi
import android.app.*
import android.content.*
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.receiver.TimeTickReceiver
import top.itning.yunshuclassschedule.ui.activity.MainActivity
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.FOREGROUND_SERVICE_STATUS

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
        startForegroundServer()
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
            else -> {

            }
        }
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
                startForegroundServer()
            } else {
                stopForeground(true)
            }
        }
    }

    companion object {
        private const val TAG = "CommonService"
    }
}
