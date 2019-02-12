package top.itning.yunshuclassschedule.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.entity.ClassSchedule
import top.itning.yunshuclassschedule.entity.ClassScheduleDao
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
import top.itning.yunshuclassschedule.ui.view.RoundBackChange
import top.itning.yunshuclassschedule.util.ClassScheduleUtils
import top.itning.yunshuclassschedule.util.DateUtils
import java.util.*


/**
 * 适配器工厂
 *
 * @author itning
 */
class ToadyRemoteViewsFactory(val context: Context, val intent: Intent?) : RemoteViewsService.RemoteViewsFactory {
    private var orderListBySection = mutableListOf<ClassSchedule>()
    var width: Int = 0
    var height: Int = 0
    private val daoSession = (context.applicationContext as App).daoSession
    private val colorArray = IntArray(7)

    override fun onCreate() {
        val scale = context.resources.displayMetrics.density
        width = (325 * scale + 0.5f).toInt()
        height = (60 * scale + 0.5f).toInt()
        colorArray[0] = ContextCompat.getColor(context, R.color.class_color_1)
        colorArray[1] = ContextCompat.getColor(context, R.color.class_color_2)
        colorArray[2] = ContextCompat.getColor(context, R.color.class_color_3)
        colorArray[3] = ContextCompat.getColor(context, R.color.class_color_4)
        colorArray[4] = ContextCompat.getColor(context, R.color.class_color_5)
        colorArray[5] = ContextCompat.getColor(context, R.color.class_color_6)
        colorArray[6] = ContextCompat.getColor(context, R.color.class_color_7)
    }

    override fun onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged")
        val nowWeekNum = (PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!.toInt() - 1).toString()
        orderListBySection = ClassScheduleUtils
                .orderListBySection(daoSession
                        .classScheduleDao
                        .queryBuilder()
                        .where(ClassScheduleDao.Properties.Week.eq(DateUtils.week))
                        .list()
                        .filter { ClassScheduleUtils.isThisWeekOfClassSchedule(it, nowWeekNum) }
                        .toMutableList())

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        orderListBySection.clear()
    }

    override fun getCount(): Int {
        return orderListBySection.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val classSchedule = orderListBySection[position]
        val relativeLayout = LayoutInflater.from(context).inflate(R.layout.widget_layout_item_draw, RelativeLayout(context))
        val name = relativeLayout.findViewById<TextView>(R.id.tv_name)
        val location = relativeLayout.findViewById<TextView>(R.id.tv_location)
        val time = relativeLayout.findViewById<TextView>(R.id.tv_time)
        val round = relativeLayout.findViewById<RoundBackChange>(R.id.round)
        round.setBackColor(colorArray[Random().nextInt(colorArray.size)])
        name.text = classSchedule.name
        location.text = classSchedule.location
        time.text = DateUtils.timeList[classSchedule.section - 1]
        if (position == 0 && ClassScheduleUtils.haveClassAfterTime(orderListBySection)) {
            val viewBottom = relativeLayout.findViewById<View>(R.id.view_bottom)
            val viewTop = relativeLayout.findViewById<View>(R.id.view_top)
            val viewLeft = relativeLayout.findViewById<View>(R.id.view_left)
            val viewProgress = relativeLayout.findViewById<View>(R.id.view_progress)
            val flNo = relativeLayout.findViewById<FrameLayout>(R.id.fl_no)
            flNo.visibility = View.INVISIBLE
            viewBottom.visibility = View.VISIBLE
            viewTop.visibility = View.VISIBLE
            viewLeft.visibility = View.VISIBLE
            viewProgress.visibility = View.VISIBLE
            val layoutParams = viewProgress.layoutParams
            layoutParams.width = DateUtils.getNowProgress(width, orderListBySection)
            viewProgress.layoutParams = layoutParams
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        relativeLayout.measure(width, height)
        relativeLayout.layout(0, 0, 0, 0)
        relativeLayout.draw(Canvas(bitmap))

        val remoteViews = RemoteViews(context.packageName, top.itning.yunshuclassschedule.R.layout.widget_layout_item)
        remoteViews.setBitmap(R.id.iv, "setImageBitmap", bitmap)
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    companion object {
        private const val TAG = "ToadyRemoteViewsFactory"
    }
}
