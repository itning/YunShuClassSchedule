package top.itning.yunshuclassschedule.service

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.widget.TodayWidgetProvider

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
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv)
            }
            else -> {

            }
        }
    }

    companion object {
        private const val TAG = "TodayWidgetService"
    }
}