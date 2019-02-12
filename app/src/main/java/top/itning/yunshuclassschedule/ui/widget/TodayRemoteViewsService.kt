package top.itning.yunshuclassschedule.ui.widget

import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService

class TodayRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        Log.d("TodayRemoteViewsService", "onGetViewFactory")
        return ToadyRemoteViewsFactory(this, intent)
    }
}