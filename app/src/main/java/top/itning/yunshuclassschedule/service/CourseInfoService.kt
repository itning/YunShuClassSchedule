package top.itning.yunshuclassschedule.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.util.SparseArray
import androidx.annotation.CheckResult
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.tencent.bugly.crashreport.CrashReport
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.ClassSchedule
import top.itning.yunshuclassschedule.entity.ClassScheduleDao
import top.itning.yunshuclassschedule.entity.DaoSession
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.activity.MainActivity
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.FOREGROUND_SERVICE_STATUS
import top.itning.yunshuclassschedule.util.ClassScheduleUtils
import top.itning.yunshuclassschedule.util.DateUtils
import java.text.ParseException
import java.util.Date
import kotlin.Comparator

/**
 * 课程信息服务
 *
 * @author itning
 */
class CourseInfoService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nowDate: Date
    private val courseArray = SparseArray<String>()

    /**
     * 今天是否有课<br></br>
     * 判断数据库中今天的课程数量是否为0
     *
     * @return 有课返回`true`
     */
    private val isHaveCourseThisDay: Boolean
        @CheckResult
        get() {
            val nowWeekNum = (PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!.toInt() - 1).toString()
            val section = App.sharedPreferences.getInt(ConstantPool.Str.CLASS_SECTION.get(), 5)
            return classScheduleDao
                    .queryBuilder()
                    .where(ClassScheduleDao.Properties.Week.eq(DateUtils.week))
                    .list()
                    .filter { it.section <= section }
                    .any { ClassScheduleUtils.isThisWeekOfClassSchedule(it, nowWeekNum) }
        }

    /**
     * 当前时间是否是在第一节课之前
     *
     * @return 在第一节课之前返回`true`
     * @throws ParseException ParseException
     */
    private val isBeforeTheFirstCourse: Boolean
        @CheckResult
        @Throws(ParseException::class)
        get() {
            val classScheduleList = soredClassSchedules
            val firstTime = DateUtils.timeList[classScheduleList[0].section - 1]
            val startFirstTime = firstTime.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            return DateUtils.DF.parse(startFirstTime).after(nowDate)
        }

    /**
     * 当前时间是否是在最后一节课之后
     *
     * @return 在最后一节课之后`true`
     * @throws ParseException ParseException
     */
    private//最后一节课下课时间是否在当前时间之前
    val isAfterTheLastCourse: Boolean
        @CheckResult
        @Throws(ParseException::class)
        get() {
            val classScheduleList = soredClassSchedules
            val lastTime = DateUtils.timeList[classScheduleList[classScheduleList.size - 1].section - 1]
            val endLastTime = lastTime.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            return DateUtils.DF.parse(endLastTime).before(nowDate)
        }

    /**
     * 下节是否有课
     *
     * @return 下节有课返回`true`
     * @throws ParseException ParseException
     */
    private val isHaveNextCourse: Boolean
        @CheckResult
        @Throws(ParseException::class)
        get() {
            val classScheduleList = soredClassSchedules
            for (i in classScheduleList.indices) {
                val times = DateUtils.timeList[classScheduleList[i].section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (DateUtils.isBelongCalendar(nowDate, DateUtils.DF.parse(times[0]), DateUtils.DF.parse(times[1]))) {
                    return i + 1 != classScheduleList.size
                }
            }
            Log.e(TAG, "not found course, now date " + DateUtils.DF.format(nowDate))
            return false
        }

    /**
     * 当前时间在任何的课程时间段中
     *
     * @return 在任何的课程时间段中返回`true`
     * @throws ParseException ParseException
     */
    private val isInAnyCourses: Boolean
        @CheckResult
        @Throws(ParseException::class)
        get() {
            val classScheduleList = soredClassSchedules
            for (c in classScheduleList) {
                val times = DateUtils.timeList[c.section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (DateUtils.isBelongCalendar(nowDate, DateUtils.DF.parse(times[0]), DateUtils.DF.parse(times[1]))) {
                    return true
                }
            }
            return false
        }


    private val soredClassSchedules: List<ClassSchedule>
        @NonNull
        @CheckResult
        get() {
            val nowWeekNum = (PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!.toInt() - 1).toString()
            val section = App.sharedPreferences.getInt(ConstantPool.Str.CLASS_SECTION.get(), 5)
            val classScheduleList = classScheduleDao
                    .queryBuilder()
                    .where(ClassScheduleDao.Properties.Week.eq(DateUtils.week))
                    .list()
                    .filter { ClassScheduleUtils.isThisWeekOfClassSchedule(it, nowWeekNum) }
                    .filter { it.section <= section }
                    .toMutableList()
            classScheduleList.sortWith(Comparator { a, b -> Integer.compare(a.section, b.section) })
            return classScheduleList
        }

    private val daoSession: DaoSession
        @CheckResult
        get() = (this.application as App).daoSession

    private val classScheduleDao: ClassScheduleDao
        @CheckResult
        get() = this.daoSession.classScheduleDao

    init {
        getDate()
    }

    @Synchronized
    private fun getDate() {
        try {
            nowDate = DateUtils.DF.parse(DateUtils.DF.format(Date()))
        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }

    override fun onCreate() {
        Log.d(TAG, "on Create")
        EventBus.getDefault().register(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        getNowCourseInfoArray()
        setNotificationContentsIfOpen()
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d(TAG, "on Destroy")
        courseArray.clear()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "on Start Command")
        return Service.START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind: $intent")
        return CourseInfoBinder()
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind: $intent")
        return super.onUnbind(intent)
    }

    inner class CourseInfoBinder : Binder() {
        val nowCourseInfo: SparseArray<String>
            get() = courseArray
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.TIME_TICK_CHANGE -> {
                getNowCourseInfoArray()
                setNotificationContentsIfOpen()
            }
            ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT -> {
                getNowCourseInfoArray()
                setNotificationContentsIfOpen()
            }
            else -> {
            }
        }
    }

    /**
     * 获取现在的课程信息
     */
    @Synchronized
    private fun getNowCourseInfoArray() {
        getDate()
        try {
            //没有课程数据
            if (classScheduleDao.count() == 0L) {
                putStr2Map(courseArray, "(ヾﾉ･ω･`)", "没有课程数据", "请滑动到右侧", "长按空白处添加课程")
                return
            }
            //今天有课
            if (isHaveCourseThisDay) {
                //当前时间在第一节课之前
                if (isBeforeTheFirstCourse) {
                    //获取第一节课课程离上课时间
                    setFirstCourseInfo2Map(courseArray)
                } else {
                    //当前时间在最后一节课之后
                    if (isAfterTheLastCourse) {
                        putStr2Map(courseArray, "", "今天课全都上完了", "(๑•̀ㅂ•́)و✧", "")
                    } else {
                        //当前时间在某课程中
                        if (isInAnyCourses) {
                            //下节是否有课
                            if (isHaveNextCourse) {
                                setInNextCourseAndTime2Map(courseArray)
                            } else {
                                //最后一节课
                                setLastCourse2Map(courseArray)
                            }
                        } else {
                            //课间
                            //获取下节课地点剩余时间
                            setFreeCourseInfo2Map(courseArray)
                        }
                    }
                }
            } else {//今天没有课
                putStr2Map(courseArray, "", "今天没有课", "ヾ(≧∇≦*)ゝ", "")
            }
        } catch (e: Exception) {
            Log.e(TAG, "get exception: ", e)
            CrashReport.postCatchedException(e)
        } finally {
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.COURSE_INFO_ARRAY_UPDATE, "", courseArray))
        }
    }

    /**
     * 设置第一节课信息
     *
     * @param courseArray 课程Array
     */
    private fun setFirstCourseInfo2Map(@NonNull courseArray: SparseArray<String>) {
        val classSchedule = soredClassSchedules[0]
        val startFirstTime = DateUtils.timeList[classSchedule.section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val theRestOfTheTime = DateUtils.getTheRestOfTheTime(nowDate, startFirstTime)
        putStr2Map(courseArray, "下节课", classSchedule.name, classSchedule.location, "还有" + theRestOfTheTime + "分钟上课")
    }

    /**
     * 设置下节有课的Map数据
     *
     * @param courseArray 课程Array
     * @throws ParseException ParseException
     */
    @Throws(ParseException::class)
    private fun setInNextCourseAndTime2Map(@NonNull courseArray: SparseArray<String>) {
        val classScheduleList = soredClassSchedules
        for (i in classScheduleList.indices) {
            val times = DateUtils.timeList[classScheduleList[i].section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (DateUtils.isBelongCalendar(nowDate, DateUtils.DF.parse(times[0]), DateUtils.DF.parse(times[1]))) {
                val classSchedule = classScheduleList[i + 1]
                val theRestOfTheTime = DateUtils.getTheRestOfTheTime(nowDate, times[1])
                putStr2Map(courseArray, "下节课", classSchedule.name, classSchedule.location, "还有" + theRestOfTheTime + "分钟下课")
                break
            }
        }
    }

    /**
     * 设置下课时间,当前是最后一节课
     *
     * @param courseArray 课程Array
     */
    private fun setLastCourse2Map(@NonNull courseArray: SparseArray<String>) {
        val classScheduleList = soredClassSchedules
        val classSchedule = classScheduleList[classScheduleList.size - 1]
        val endTime = DateUtils.timeList[classSchedule.section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val theRestOfTheTime = DateUtils.getTheRestOfTheTime(nowDate, endTime)
        putStr2Map(courseArray, "", "这是最后一节课", "还有" + theRestOfTheTime + "分钟下课", "")
    }

    /**
     * 设置课间
     *
     * @param courseArray 课程Array
     */
    @Throws(ParseException::class)
    private fun setFreeCourseInfo2Map(@NonNull courseArray: SparseArray<String>) {
        val classScheduleList = soredClassSchedules
        for (i in classScheduleList.indices) {
            val times = DateUtils.timeList[classScheduleList[i].section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (i + 1 == classScheduleList.size) {
                break
            }
            val nextClassSchedule = classScheduleList[i + 1]
            val times2 = DateUtils.timeList[nextClassSchedule.section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (DateUtils.isBelongCalendar(nowDate, DateUtils.DF.parse(times[1]), DateUtils.DF.parse(times2[0]))) {
                val theRestOfTheTime = DateUtils.getTheRestOfTheTime(nowDate, times2[0])
                putStr2Map(courseArray, "下节课", nextClassSchedule.name, nextClassSchedule.location, "还有" + theRestOfTheTime + "分钟上课")
                break
            }
        }
    }

    /**
     * 将字符串存入Array中
     *
     * @param targetArray 要存入的Array
     * @param line1       String
     * @param line2       String
     * @param line3       String
     * @param line4       String
     */
    private fun putStr2Map(@NonNull targetArray: SparseArray<String>, @NonNull line1: String, @NonNull line2: String, @NonNull line3: String, @NonNull line4: String) {
        targetArray.put(1, line1)
        targetArray.put(2, line2)
        targetArray.put(3, line3)
        targetArray.put(4, line4)
    }

    /**
     * 开启前台服务
     */
    private fun setNotificationContentsIfOpen() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
            return
        }
        val a1 = courseArray.get(1)
        val a2 = courseArray.get(2)
        val a3 = courseArray.get(3)
        val a4 = courseArray.get(4)
        val titleBuilder = StringBuilder()
        val textBuilder = StringBuilder()
        if (NO_COURSE_DATA == a2) {
            titleBuilder.append(a1)
            textBuilder.append(a2)
        } else {
            if ("" == a1) {
                titleBuilder.append(a2)
                textBuilder.append(a3)
            } else {
                titleBuilder.append(a1).append("：").append(a3).append(" ").append(a2)
                textBuilder.append(a4)
            }
        }
        Log.d(TAG, "set Notification Contents")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        //用ComponentName得到class对象
        intent.component = ComponentName(this, MainActivity::class.java)
        // 关键的一步，设置启动模式，两种情况
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val pendingIntent = PendingIntent.getActivity(this, 88, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, "foreground_service")
                .setContentTitle(titleBuilder)
                .setContentText(textBuilder)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == FOREGROUND_SERVICE_STATUS) {
            if (sharedPreferences.getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
                getNowCourseInfoArray()
                setNotificationContentsIfOpen()
            } else {
                stopForeground(true)
            }
        }
    }

    companion object {
        private const val TAG = "CourseInfoService"
        private const val NO_COURSE_DATA = "没有课程数据"
    }
}
