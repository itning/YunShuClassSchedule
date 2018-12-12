package top.itning.yunshuclassschedule.ui.adapter

import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.entity.ClassSchedule
import top.itning.yunshuclassschedule.ui.view.RoundBackChange
import top.itning.yunshuclassschedule.util.ClassScheduleUtils
import top.itning.yunshuclassschedule.util.DateUtils
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.util.*

/**
 * 今天课程列表适配器
 *
 * @author itning
 */
class TodayRecyclerViewAdapter(
        /**
         * 列表数据集合
         */
        @param:NonNull private val scheduleList: List<ClassSchedule>?,
        /**
         * [Context]
         */
        @param:NonNull private val context: Context) : RecyclerView.Adapter<TodayRecyclerViewAdapter.ViewHolder>() {
    /**
     * 颜色数组
     */
    private val colorArray = IntArray(7)
    /**
     * 随机好的颜色集合
     */
    private val showColorList: ArrayList<Int>
    var viewProgress: View? = null
        private set

    init {
        Log.d(TAG, "new Today Recycler View Adapter")
        //数组赋值
        colorArray[0] = ContextCompat.getColor(context, R.color.class_color_1)
        colorArray[1] = ContextCompat.getColor(context, R.color.class_color_2)
        colorArray[2] = ContextCompat.getColor(context, R.color.class_color_3)
        colorArray[3] = ContextCompat.getColor(context, R.color.class_color_4)
        colorArray[4] = ContextCompat.getColor(context, R.color.class_color_5)
        colorArray[5] = ContextCompat.getColor(context, R.color.class_color_6)
        colorArray[6] = ContextCompat.getColor(context, R.color.class_color_7)
        //随机颜色集合构建
        val random = Random()
        showColorList = ArrayList(colorArray.size)
        do {
            val number = random.nextInt(colorArray.size)
            if (!showColorList.contains(number)) {
                showColorList.add(number)
            }
        } while (showColorList.size != colorArray.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_class_rv, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder pos->$position")
        val classSchedule = scheduleList!![position]
        holder.tvName.text = classSchedule.name
        holder.tvLocation.text = classSchedule.location
        holder.tvTime.text = DateUtils.timeList[classSchedule.section - 1]
        holder.round.setBackColor(colorArray[showColorList[position]])
        //显示设置可见性
        holder.flNo.visibility = View.VISIBLE
        holder.viewBottom.visibility = View.INVISIBLE
        holder.viewTop.visibility = View.INVISIBLE
        holder.viewLeft.visibility = View.INVISIBLE
        holder.viewProgress.visibility = View.INVISIBLE
        ThemeChangeUtil.setProgressBackgroundResource(context, holder.viewProgress)
        ThemeChangeUtil.setBackgroundResources(context, holder.viewBottom, holder.viewTop, holder.viewLeft, holder.viewCenter)
        if (position == 0 && ClassScheduleUtils.haveClassAfterTime(scheduleList)) {
            //是当前正在或要上的课程
            holder.flNo.visibility = View.INVISIBLE
            holder.viewBottom.visibility = View.VISIBLE
            holder.viewTop.visibility = View.VISIBLE
            holder.viewLeft.visibility = View.VISIBLE
            holder.viewProgress.visibility = View.VISIBLE
            val display = (Objects.requireNonNull(context.getSystemService(Context.WINDOW_SERVICE)) as WindowManager).defaultDisplay
            val size = Point()
            display.getSize(size)
            val layoutParams = holder.viewProgress.layoutParams
            layoutParams.width = DateUtils.getNowProgress(size.x, scheduleList)
            holder.viewProgress.layoutParams = layoutParams
            viewProgress = holder.viewProgress
        }
    }

    override fun getItemCount(): Int {
        return scheduleList?.size ?: 0
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.view_left)
        lateinit var viewLeft: View
        @BindView(R.id.view_top)
        lateinit var viewTop: View
        @BindView(R.id.view_bottom)
        lateinit var viewBottom: View
        @BindView(R.id.view_center)
        lateinit var viewCenter: View
        @BindView(R.id.view_progress)
        lateinit var viewProgress: View
        @BindView(R.id.round)
        lateinit var round: RoundBackChange
        @BindView(R.id.fl_no)
        lateinit var flNo: FrameLayout
        @BindView(R.id.tv_name)
        lateinit var tvName: TextView
        @BindView(R.id.tv_location)
        lateinit var tvLocation: TextView
        @BindView(R.id.tv_time)
        lateinit var tvTime: TextView

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    companion object {
        private const val TAG = "TodayRecyclerAdapter"
    }
}
