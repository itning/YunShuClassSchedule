package top.itning.yunshuclassschedule.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.ui.activity.MainActivity;
import top.itning.yunshuclassschedule.util.DateUtils;

import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.FOREGROUND_SERVICE_STATUS;

/**
 * 课程信息服务
 *
 * @author itning
 */
public class CourseInfoService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "CourseInfoService";
    private static final String NO_COURSE_DATA = "没有课程数据";

    private SharedPreferences sharedPreferences;
    private Date nowDate;
    private final SparseArray<String> courseArray = new SparseArray<>();

    {
        getDate();
    }

    private synchronized void getDate() {
        try {
            nowDate = DateUtils.DF.parse(DateUtils.DF.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "on Create");
        EventBus.getDefault().register(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        getNowCourseInfoArray();
        setNotificationContentsIfOpen();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "on Destroy");
        courseArray.clear();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on Start Command");
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: " + intent);
        return new CourseInfoBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: " + intent);
        return super.onUnbind(intent);
    }

    public class CourseInfoBinder extends Binder {
        public SparseArray<String> getNowCourseInfo() {
            return courseArray;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case TIME_TICK_CHANGE: {
                getNowCourseInfoArray();
                setNotificationContentsIfOpen();
                break;
            }
            case REFRESH_CLASS_SCHEDULE_FRAGMENT: {
                getNowCourseInfoArray();
                setNotificationContentsIfOpen();
                break;
            }
            default:
        }
    }

    /**
     * 获取现在的课程信息
     */
    private synchronized void getNowCourseInfoArray() {
        getDate();
        try {
            //没有课程数据
            if (getClassScheduleDao().count() == 0) {
                putStr2Map(courseArray, "(ヾﾉ･ω･`)", "没有课程数据", "请滑动到右侧", "长按空白处添加课程");
                return;
            }
            //今天有课
            if (isHaveCourseThisDay()) {
                //当前时间在第一节课之前
                if (isBeforeTheFirstCourse()) {
                    //获取第一节课课程离上课时间
                    setFirstCourseInfo2Map(courseArray);
                } else {
                    //当前时间在最后一节课之后
                    if (isAfterTheLastCourse()) {
                        putStr2Map(courseArray, "", "今天课全都上完了", "(๑•̀ㅂ•́)و✧", "");
                    } else {
                        //当前时间在某课程中
                        if (isInAnyCourses()) {
                            //下节是否有课
                            if (isHaveNextCourse()) {
                                setInNextCourseAndTime2Map(courseArray);
                            } else {
                                //最后一节课
                                setLastCourse2Map(courseArray);
                            }
                        } else {
                            //课间
                            //获取下节课地点剩余时间
                            setFreeCourseInfo2Map(courseArray);
                        }
                    }
                }
            } else {//今天没有课
                putStr2Map(courseArray, "", "今天没有课", "ヾ(≧∇≦*)ゝ", "");
            }
        } catch (Exception e) {
            Log.e(TAG, "get exception: ", e);
            CrashReport.postCatchedException(e);
        } finally {
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.COURSE_INFO_ARRAY_UPDATE, "", courseArray));
        }
    }

    /**
     * 设置第一节课信息
     *
     * @param courseArray 课程Array
     */
    private void setFirstCourseInfo2Map(@NonNull SparseArray<String> courseArray) {
        ClassSchedule classSchedule = getSoredClassSchedules().get(0);
        String startFirstTime = DateUtils.getTimeList().get(classSchedule.getSection() - 1).split("-")[0];
        int theRestOfTheTime = DateUtils.getTheRestOfTheTime(nowDate, startFirstTime);
        putStr2Map(courseArray, "下节课", classSchedule.getName(), classSchedule.getLocation(), "还有" + theRestOfTheTime + "分钟上课");
    }

    /**
     * 设置下节有课的Map数据
     *
     * @param courseArray 课程Array
     * @throws ParseException ParseException
     */
    private void setInNextCourseAndTime2Map(@NonNull SparseArray<String> courseArray) throws ParseException {
        List<ClassSchedule> classScheduleList = getSoredClassSchedules();
        for (int i = 0; i < classScheduleList.size(); i++) {
            String[] times = DateUtils.getTimeList().get(classScheduleList.get(i).getSection() - 1).split("-");
            if (DateUtils.isBelongCalendar(nowDate, DateUtils.DF.parse(times[0]), DateUtils.DF.parse(times[1]))) {
                ClassSchedule classSchedule = classScheduleList.get(i + 1);
                int theRestOfTheTime = DateUtils.getTheRestOfTheTime(nowDate, times[1]);
                putStr2Map(courseArray, "下节课", classSchedule.getName(), classSchedule.getLocation(), "还有" + theRestOfTheTime + "分钟下课");
                break;
            }
        }
    }

    /**
     * 设置下课时间,当前是最后一节课
     *
     * @param courseArray 课程Array
     */
    private void setLastCourse2Map(@NonNull SparseArray<String> courseArray) {
        List<ClassSchedule> classScheduleList = getSoredClassSchedules();
        ClassSchedule classSchedule = classScheduleList.get(classScheduleList.size() - 1);
        String endTime = DateUtils.getTimeList().get(classSchedule.getSection() - 1).split("-")[1];
        int theRestOfTheTime = DateUtils.getTheRestOfTheTime(nowDate, endTime);
        putStr2Map(courseArray, "", "这是最后一节课", "还有" + theRestOfTheTime + "分钟下课", "");
    }

    /**
     * 设置课间
     *
     * @param courseArray 课程Array
     */
    private void setFreeCourseInfo2Map(@NonNull SparseArray<String> courseArray) throws ParseException {
        List<ClassSchedule> classScheduleList = getSoredClassSchedules();
        for (int i = 0; i < classScheduleList.size(); i++) {
            String[] times = DateUtils.getTimeList().get(classScheduleList.get(i).getSection() - 1).split("-");
            if ((i + 1) == classScheduleList.size()) {
                break;
            }
            ClassSchedule nextClassSchedule = classScheduleList.get(i + 1);
            String[] times2 = DateUtils.getTimeList().get(nextClassSchedule.getSection() - 1).split("-");
            if (DateUtils.isBelongCalendar(nowDate, DateUtils.DF.parse(times[1]), DateUtils.DF.parse(times2[0]))) {
                int theRestOfTheTime = DateUtils.getTheRestOfTheTime(nowDate, times2[0]);
                putStr2Map(courseArray, "下节课", nextClassSchedule.getName(), nextClassSchedule.getLocation(), "还有" + theRestOfTheTime + "分钟上课");
                break;
            }
        }
    }

    /**
     * 今天是否有课<br>
     * 判断数据库中今天的课程数量是否为0
     *
     * @return 有课返回<code>true</code>
     */
    @CheckResult
    private boolean isHaveCourseThisDay() {
        return getClassScheduleDao()
                .queryBuilder()
                .where(ClassScheduleDao.Properties.Week.eq(DateUtils.getWeek()))
                .count() != 0;
    }

    /**
     * 当前时间是否是在第一节课之前
     *
     * @return 在第一节课之前返回<code>true</code>
     * @throws ParseException ParseException
     */
    @CheckResult
    private boolean isBeforeTheFirstCourse() throws ParseException {
        List<ClassSchedule> classScheduleList = getSoredClassSchedules();
        String firstTime = DateUtils.getTimeList().get(classScheduleList.get(0).getSection() - 1);
        String startFirstTime = firstTime.split("-")[0];
        return DateUtils.DF.parse(startFirstTime).after(nowDate);
    }

    /**
     * 当前时间是否是在最后一节课之后
     *
     * @return 在最后一节课之后<code>true</code>
     * @throws ParseException ParseException
     */
    @CheckResult
    private boolean isAfterTheLastCourse() throws ParseException {
        List<ClassSchedule> classScheduleList = getSoredClassSchedules();
        String lastTime = DateUtils.getTimeList().get(classScheduleList.get(classScheduleList.size() - 1).getSection() - 1);
        String endLastTime = lastTime.split("-")[1];
        //最后一节课下课时间是否在当前时间之前
        return DateUtils.DF.parse(endLastTime).before(nowDate);
    }

    /**
     * 下节是否有课
     *
     * @return 下节有课返回<code>true</code>
     * @throws ParseException ParseException
     */
    @CheckResult
    private boolean isHaveNextCourse() throws ParseException {
        List<ClassSchedule> classScheduleList = getSoredClassSchedules();
        for (int i = 0; i < classScheduleList.size(); i++) {
            String[] times = DateUtils.getTimeList().get(classScheduleList.get(i).getSection() - 1).split("-");
            if (DateUtils.isBelongCalendar(nowDate, DateUtils.DF.parse(times[0]), DateUtils.DF.parse(times[1]))) {
                return (i + 1) != classScheduleList.size();
            }
        }
        Log.e(TAG, "not found course, now date " + DateUtils.DF.format(nowDate));
        return false;
    }

    /**
     * 当前时间在任何的课程时间段中
     *
     * @return 在任何的课程时间段中返回<code>true</code>
     * @throws ParseException ParseException
     */
    @CheckResult
    private boolean isInAnyCourses() throws ParseException {
        List<ClassSchedule> classScheduleList = getSoredClassSchedules();
        for (ClassSchedule c : classScheduleList) {
            String[] times = DateUtils.getTimeList().get(c.getSection() - 1).split("-");
            if (DateUtils.isBelongCalendar(nowDate, DateUtils.DF.parse(times[0]), DateUtils.DF.parse(times[1]))) {
                return true;
            }
        }
        return false;
    }


    @NonNull
    @CheckResult
    private List<ClassSchedule> getSoredClassSchedules() {
        List<ClassSchedule> classScheduleList = getClassScheduleDao()
                .queryBuilder()
                .where(ClassScheduleDao.Properties.Week.eq(DateUtils.getWeek())).list();
        Collections.sort(classScheduleList, (a, b) -> Integer.compare(a.getSection(), b.getSection()));
        return classScheduleList;
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
    private void putStr2Map(@NonNull SparseArray<String> targetArray, @NonNull String line1, @NonNull String line2, @NonNull String line3, @NonNull String line4) {
        targetArray.put(1, line1);
        targetArray.put(2, line2);
        targetArray.put(3, line3);
        targetArray.put(4, line4);
    }

    @CheckResult
    private DaoSession getDaoSession() {
        return ((App) this.getApplication()).getDaoSession();
    }

    @CheckResult
    private ClassScheduleDao getClassScheduleDao() {
        return this.getDaoSession().getClassScheduleDao();
    }

    /**
     * 开启前台服务
     */
    private void setNotificationContentsIfOpen() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
            return;
        }
        String a1 = courseArray.get(1);
        String a2 = courseArray.get(2);
        String a3 = courseArray.get(3);
        String a4 = courseArray.get(4);
        StringBuilder titleBuilder = new StringBuilder();
        StringBuilder textBuilder = new StringBuilder();
        if (NO_COURSE_DATA.equals(a2)) {
            titleBuilder.append(a1);
            textBuilder.append(a2);
        } else {
            if ("".equals(a1)) {
                titleBuilder.append(a2);
                textBuilder.append(a3);
            } else {
                titleBuilder.append(a1).append("：").append(a3).append(" ").append(a2);
                textBuilder.append(a4);
            }
        }
        Log.d(TAG, "set Notification Contents");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //用ComponentName得到class对象
        intent.setComponent(new ComponentName(this, MainActivity.class));
        // 关键的一步，设置启动模式，两种情况
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 88, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "foreground_service")
                .setContentTitle(titleBuilder)
                .setContentText(textBuilder)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.logo))
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        Notification notification = builder.build();
        startForeground(111, notification);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(FOREGROUND_SERVICE_STATUS)) {
            if (sharedPreferences.getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
                getNowCourseInfoArray();
                setNotificationContentsIfOpen();
            } else {
                stopForeground(true);
            }
        }
    }
}
