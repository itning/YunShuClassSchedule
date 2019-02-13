package top.itning.yunshuclassschedule.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.activity.MainActivity
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
import top.itning.yunshuclassschedule.ui.widget.TodayWidgetProvider
import java.util.*

/**
 * 小部件更新服务
 *
 * @author itning
 */
class TodayWidgetService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        Log.d(TAG, "on Create")
        EventBus.getDefault().register(this)
        startForegroundServer()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "on Start Command")
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        Log.d(TAG, "on Destroy")
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.TIME_TICK_CHANGE -> {
                val thisWidget = ComponentName(this, TodayWidgetProvider::class.java)
                val appWidgetManager = AppWidgetManager.getInstance(this)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                Log.d(TAG, "appWidgetIds: ${Arrays.toString(appWidgetIds)}")
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv)
            }
            else -> {

            }
        }
    }

    /**
     * 开启前台服务
     */
    private fun startForegroundServer() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.FOREGROUND_SERVICE_STATUS, true)) {
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

    companion object {
        private const val TAG = "TodayWidgetService"
    }
}