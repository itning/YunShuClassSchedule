package top.itning.yunshuclassschedule.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.ClassSchedule
import top.itning.yunshuclassschedule.entity.ClassScheduleDao
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.service.CourseInfoService
import top.itning.yunshuclassschedule.ui.adapter.TodayRecyclerViewAdapter
import top.itning.yunshuclassschedule.util.ClassScheduleUtils
import top.itning.yunshuclassschedule.util.DateUtils
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


/**
 * 今天
 *
 * @author itning
 */
class TodayFragment : Fragment() {

    private var mView: View? = null
    /**
     * 课程集合
     */
    private var classScheduleList: MutableList<ClassSchedule>? = null
    /**
     * 是否滑动到顶部
     */
    private var mTop: AtomicBoolean? = null
    /**
     * 上次正在上的课程
     */
    private var lastClass = DateUtils.whichClassNow
    /**
     * [TodayRecyclerViewAdapter]
     */
    private var todayRecyclerViewAdapter: TodayRecyclerViewAdapter? = null
    /**
     * 滑动时移动的坐标
     */
    private var finalIndex: Int = 0
    /**
     * 标记是否已经Stop
     */
    private var stop: Boolean = false
    /**
     * 当Start时是否需要移动元素
     */
    private var needMoved: Boolean = false
    /**
     * 当前正在上的课
     */
    private var whichClassNow: Int = 0
    /**
     * 高度
     * 由于软键盘弹出影响高度测量,所以暂存
     */
    private var height: Int = 0

    private var courseInfoBinder: CourseInfoService.CourseInfoBinder? = null

    private val courseInfoConnection = CourseInfoConnection()

    internal class ViewHolder(view: View) {
        @BindView(R.id.rv)
        lateinit var rv: RecyclerView
        @BindView(R.id.ll)
        lateinit var ll: LinearLayout
        @BindView(R.id.rl)
        lateinit var rl: RelativeLayout
        @BindView(R.id.nsv)
        lateinit var nsv: NestedScrollView
        @BindView(R.id.tv_remind_time)
        lateinit var tvRemindTime: TextView
        @BindView(R.id.tv_remind_remind)
        lateinit var tvRemindRemind: TextView
        @BindView(R.id.tv_remind_name)
        lateinit var tvRemindName: TextView
        @BindView(R.id.tv_remind_location)
        lateinit var tvRemindLocation: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "on Create")
        requireActivity().bindService(Intent(requireActivity(), CourseInfoService::class.java), courseInfoConnection, Context.BIND_AUTO_CREATE)
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.TIME_TICK_CHANGE -> {
                Log.d(TAG, "Time changed event already received")
                //时间改变时,更新进度
                setViewProgress()
                //检查课程改变
                checkClassScheduleChange()
            }
            ConstantPool.Int.APP_COLOR_CHANGE -> {
                Log.d(TAG, "app color change , now afresh mView")
                val viewHolder = mView!!.tag as ViewHolder
                viewHolder.nsv.scrollTo(0, 0)
                ThemeChangeUtil.setBackgroundResources(requireContext(), viewHolder.ll)
                viewHolder.rv.adapter!!.notifyDataSetChanged()
            }
            ConstantPool.Int.COURSE_INFO_ARRAY_UPDATE -> {
                setPanelText(mView!!.tag as ViewHolder)
            }
            else -> {
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "on Destroy")
        requireActivity().unbindService(courseInfoConnection)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onStart() {
        stop = false
        if (needMoved) {
            val holder = mView!!.tag as ViewHolder
            Handler().postDelayed({ holder.rv.adapter!!.notifyItemMoved(0, finalIndex) }, 1000)
            needMoved = false
        }
        super.onStart()
    }

    override fun onStop() {
        stop = true
        super.onStop()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val holder: ViewHolder
        if (mView != null) {
            holder = mView!!.tag as ViewHolder
        } else {
            mView = inflater.inflate(R.layout.fragment_today, container, false)
            holder = ViewHolder(mView!!)
            mView!!.tag = holder
        }
        //初始化课程数据
        initClassScheduleListData()
        mTop = AtomicBoolean(true)
        whichClassNow = DateUtils.whichClassNow

        //LinearLayout背景颜色
        ThemeChangeUtil.setBackgroundResources(requireContext(), holder.ll)

        //RecyclerView初始化
        holder.rv.layoutManager = LinearLayoutManager(context)
        todayRecyclerViewAdapter = TodayRecyclerViewAdapter(classScheduleList, requireContext())
        holder.rv.adapter = todayRecyclerViewAdapter

        //设置LinearLayout的高度为总大小-RecyclerView的子项大小
        holder.rv.post {
            mView!!.post {
                val i = if (classScheduleList!!.size == 0) holder.rv.height else holder.rv.height / classScheduleList!!.size
                val lp: ViewGroup.LayoutParams = holder.ll.layoutParams
                if (height == 0) {
                    height = mView!!.height - i
                }
                lp.height = height
                holder.ll.layoutParams = lp
            }
        }

        //设置滑动索引
        setFinalIndex()
        //NestedScrollView滑动监听
        nestedScrollViewOnScrollChangeListener(holder)
        return this.mView
    }

    /**
     * 滑动监听
     *
     * @param holder [ViewHolder]
     */
    private fun nestedScrollViewOnScrollChangeListener(holder: ViewHolder) {
        val adapter = holder.rv.adapter
        val pp = holder.rl.layoutParams as LinearLayout.LayoutParams
        holder.nsv.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            //设置随滑动改变位置
            pp.topMargin = scrollY
            holder.rl.layoutParams = pp
            if (whichClassNow == -1 || !ClassScheduleUtils.haveClassAfterTime(classScheduleList!!)) {
                return@OnScrollChangeListener
            }
            if (scrollY <= SLIDE_UP_THRESHOLD && !mTop!!.get()) {
                mTop!!.set(true)
                adapter!!.notifyItemMoved(finalIndex, 0)
            } else if (mTop!!.get() && scrollY == holder.rv.height - holder.rv.height / classScheduleList!!.size) {
                mTop!!.set(false)
                adapter!!.notifyItemMoved(0, finalIndex)
            }
        })
    }

    /**
     * 初始化课程数据
     */
    private fun initClassScheduleListData() {
        val daoSession = (requireActivity().application as App).daoSession
        classScheduleList = ClassScheduleUtils
                .orderListBySection(daoSession!!
                        .classScheduleDao
                        .queryBuilder()
                        .where(ClassScheduleDao.Properties.Week.eq(DateUtils.week))
                        .list())
    }

    /**
     * 设置面板文字
     *
     * @param holder [ViewHolder]
     */
    private fun setPanelText(holder: ViewHolder) {
        if (courseInfoBinder != null) {
            val sparseArray = courseInfoBinder!!.nowCourseInfo
            holder.tvRemindRemind.text = sparseArray.get(1)
            holder.tvRemindName.text = sparseArray.get(2)
            holder.tvRemindLocation.text = sparseArray.get(3)
            holder.tvRemindTime.text = sparseArray.get(4)
        }
    }

    internal inner class CourseInfoConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected: $service")
            courseInfoBinder = service as CourseInfoService.CourseInfoBinder
            setPanelText(mView!!.tag as ViewHolder)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "onServiceDisconnected")
            courseInfoBinder = null
        }
    }

    /**
     * 设置滑动索引
     */
    private fun setFinalIndex() {
        val index: Int
        whichClassNow = DateUtils.whichClassNow
        if (whichClassNow != -1 && ClassScheduleUtils.haveClassAfterTime(classScheduleList!!)) {
            a@ while (true) {
                if (whichClassNow == 0) {
                    index = 0
                    break
                }
                for (c in classScheduleList!!) {
                    if (c.section == whichClassNow) {
                        index = classScheduleList!!.indexOf(c)
                        break@a
                    }
                }
                whichClassNow--
            }

        } else {
            index = 0
        }
        finalIndex = index
    }

    /**
     * 更新进度
     */
    private fun setViewProgress() {
        val viewProgress = todayRecyclerViewAdapter!!.viewProgress
        if (viewProgress != null) {
            val display = (Objects.requireNonNull(Objects.requireNonNull<Context>(context).getSystemService(Context.WINDOW_SERVICE)) as WindowManager).defaultDisplay
            val size = Point()
            display.getSize(size)
            val layoutParams = viewProgress.layoutParams
            layoutParams.width = DateUtils.getNowProgress(size.x, classScheduleList!!)
            viewProgress.layoutParams = layoutParams
        }
    }

    /**
     * 检查课程改变
     */
    private fun checkClassScheduleChange() {
        if (lastClass != DateUtils.whichClassNow) {
            Log.d(TAG, "time changed ,need update class schedule")
            lastClass = DateUtils.whichClassNow
            classScheduleList = ClassScheduleUtils.orderListBySection(classScheduleList!!)
            val holder = mView!!.tag as ViewHolder
            val adapter = holder.rv.adapter
            adapter!!.notifyDataSetChanged()
            setFinalIndex()
            if (!mTop!!.get() && whichClassNow != -1) {
                if (stop) {
                    needMoved = true
                } else {
                    Handler().postDelayed({ adapter.notifyItemMoved(0, finalIndex) }, 1000)
                }
                mTop!!.set(false)
            }
        }
    }

    companion object {
        private const val TAG = "TodayFragment"
        /**
         * 向上滑动的临界值
         */
        private const val SLIDE_UP_THRESHOLD = 20
    }
}
