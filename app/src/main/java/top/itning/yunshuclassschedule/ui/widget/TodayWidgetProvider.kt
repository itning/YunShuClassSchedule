package top.itning.yunshuclassschedule.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import top.itning.yunshuclassschedule.service.TodayWidgetService

/**
 * 今天课程小部件
 *
 * @author itning
 */
class TodayWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        Log.d(TAG, "onUpdate")
        context?.startService(Intent(context, TodayWidgetService::class.java))
        appWidgetIds?.forEach {
            val remoteViews = RemoteViews(context?.packageName, top.itning.yunshuclassschedule.R.layout.widget_layout_today)
            val intent = Intent(context, TodayRemoteViewsService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, it)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            remoteViews.setRemoteAdapter(top.itning.yunshuclassschedule.R.id.lv, intent)
            remoteViews.setEmptyView(top.itning.yunshuclassschedule.R.id.lv, top.itning.yunshuclassschedule.R.id.empty_view)
            appWidgetManager?.updateAppWidget(it, remoteViews)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDisabled(context: Context?) {
        Log.d(TAG, "onDisabled")
        context?.stopService(Intent(context, TodayWidgetService::class.java))
        super.onDisabled(context)
    }

    companion object {
        private const val TAG = "TodayWidgetProvider"
    }
}