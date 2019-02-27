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
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_today.*
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
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
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

    private lateinit var mView: View
    /**
     * 课程集合
     */
    private lateinit var classScheduleList: MutableList<ClassSchedule>
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
    private lateinit var todayRecyclerViewAdapter: TodayRecyclerViewAdapter
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
                nsv.scrollTo(0, 0)
                ThemeChangeUtil.setBackgroundResources(requireContext(), ll)
                rv.adapter!!.notifyDataSetChanged()
            }
            ConstantPool.Int.COURSE_INFO_ARRAY_UPDATE -> {
                setPanelText()
            }
            ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT -> {
                height = 0
                Log.d(TAG, "Reset height: $height")
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
            Handler().postDelayed({ rv.adapter!!.notifyItemMoved(0, finalIndex) }, 1000)
            needMoved = false
        }
        super.onStart()
    }

    override fun onStop() {
        stop = true
        super.onStop()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_today, container, false)
        //初始化课程数据
        initClassScheduleListData()
        mTop = AtomicBoolean(true)
        whichClassNow = DateUtils.whichClassNow
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //LinearLayout背景颜色
        ThemeChangeUtil.setBackgroundResources(requireContext(), ll)
        //RecyclerView初始化
        rv.layoutManager = LinearLayoutManager(context)
        todayRecyclerViewAdapter = TodayRecyclerViewAdapter(classScheduleList, requireContext())
        rv.adapter = todayRecyclerViewAdapter
        //设置LinearLayout的高度为总大小-RecyclerView的子项大小
        rv.post {
            mView.post {
                val mRv = view.findViewById<RecyclerView>(R.id.rv)
                val mLl = view.findViewById<LinearLayout>(R.id.ll)
                val i = if (classScheduleList.size == 0) mRv.height else mRv.height / classScheduleList.size
                val lp: ViewGroup.LayoutParams = mLl.layoutParams
                if (height == 0) {
                    //如果今天没有课那么不进行赋值,防止修改后造成BUG
                    if (classScheduleList.isEmpty()) {
                        lp.height = mView.height - i
                    } else {
                        height = mView.height - i
                        lp.height = height
                    }
                } else {
                    lp.height = height
                }
                mLl.layoutParams = lp
            }
        }

        //设置滑动索引
        setFinalIndex()
        //NestedScrollView滑动监听
        nestedScrollViewOnScrollChangeListener()
    }

    /**
     * 滑动监听
     *
     */
    private fun nestedScrollViewOnScrollChangeListener() {
        val adapter = rv.adapter
        val pp = rl.layoutParams as LinearLayout.LayoutParams
        nsv.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            //设置随滑动改变位置
            pp.topMargin = scrollY
            rl.layoutParams = pp
            if (whichClassNow == -1 || !ClassScheduleUtils.haveClassAfterTime(classScheduleList)) {
                return@OnScrollChangeListener
            }
            if (scrollY <= SLIDE_UP_THRESHOLD && !mTop!!.get()) {
                mTop!!.set(true)
                adapter!!.notifyItemMoved(finalIndex, 0)
            } else if (mTop!!.get() && scrollY == rv.height - rv.height / classScheduleList.size) {
                mTop!!.set(false)
                adapter!!.notifyItemMoved(0, finalIndex)
            }
        })
    }

    /**
     * 初始化课程数据
     */
    private fun initClassScheduleListData() {
        val nowWeekNum = (PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!.toInt() - 1).toString()
        val daoSession = (requireActivity().application as App).daoSession
        val section = App.sharedPreferences.getInt(ConstantPool.Str.CLASS_SECTION.get(), 5)
        classScheduleList = ClassScheduleUtils
                .orderListBySection(daoSession
                        .classScheduleDao
                        .queryBuilder()
                        .where(ClassScheduleDao.Properties.Week.eq(DateUtils.week))
                        .list()
                        .filter { ClassScheduleUtils.isThisWeekOfClassSchedule(it, nowWeekNum) }
                        .filter { it.section <= section }
                        .toMutableList())
    }

    /**
     * 设置面板文字
     *
     */
    private fun setPanelText() {
        if (courseInfoBinder != null) {
            val sparseArray = courseInfoBinder!!.nowCourseInfo
            tv_remind_remind.text = sparseArray.get(1)
            tv_remind_name.text = sparseArray.get(2)
            tv_remind_location.text = sparseArray.get(3)
            tv_remind_time.text = sparseArray.get(4)
        }
    }

    internal inner class CourseInfoConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected: $service")
            courseInfoBinder = service as CourseInfoService.CourseInfoBinder
            setPanelText()
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
        if (whichClassNow != -1 && ClassScheduleUtils.haveClassAfterTime(classScheduleList)) {
            a@ while (true) {
                if (whichClassNow == 0) {
                    index = 0
                    break
                }
                for (c in classScheduleList) {
                    if (c.section == whichClassNow) {
                        index = classScheduleList.indexOf(c)
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
        val viewProgress = todayRecyclerViewAdapter.viewProgress
        if (viewProgress != null) {
            val display = (Objects.requireNonNull(Objects.requireNonNull<Context>(context).getSystemService(Context.WINDOW_SERVICE)) as WindowManager).defaultDisplay
            val size = Point()
            display.getSize(size)
            val layoutParams = viewProgress.layoutParams
            layoutParams.width = DateUtils.getNowProgress(size.x, classScheduleList)
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
            classScheduleList = ClassScheduleUtils.orderListBySection(classScheduleList)
            val adapter = rv.adapter
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
