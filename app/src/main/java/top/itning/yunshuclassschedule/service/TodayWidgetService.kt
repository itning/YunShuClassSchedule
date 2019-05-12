package top.itning.yunshuclassschedule.service

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.preference.PreferenceManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
import top.itning.yunshuclassschedule.ui.widget.TodayWidgetProvider
import top.itning.yunshuclassschedule.util.ClassScheduleUtils
import java.util.*

/**
 * 小部件更新服务
 *
 * @author itning
 */
class TodayWidgetService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        Log.d(TAG, "on Create")
        EventBus.getDefault().register(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        ClassScheduleUtils.startForegroundServer(this, TAG)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "on Start Command")
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        Log.d(TAG, "on Destroy")
        EventBus.getDefault().unregister(this)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SettingsFragment.FOREGROUND_SERVICE_STATUS) {
            if (sharedPreferences.getBoolean(SettingsFragment.FOREGROUND_SERVICE_STATUS, true)) {
                ClassScheduleUtils.startForegroundServer(this, TAG)
            } else {
                stopForeground(true)
            }
        }
    }

    companion object {
        private const val TAG = "TodayWidgetService"
    }
}