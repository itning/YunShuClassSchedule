package top.itning.yunshuclassschedule.util;

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.ClassSchedule;

/**
 * 时间工具
 *
 * @author itning
 */
public class DateUtils {
    private static final String TAG = "DateUtils";
    public static final SimpleDateFormat DF = new SimpleDateFormat("HH:mm", Locale.CHINESE);
    private static final List<String> TIME_LIST = new ArrayList<>();
    private static final Calendar CAL = Calendar.getInstance();

    static {
        TIME_LIST.add(App.sharedPreferences.getString("1", "08:20-09:50"));
        TIME_LIST.add(App.sharedPreferences.getString("2", "10:05-11:35"));
        TIME_LIST.add(App.sharedPreferences.getString("3", "12:55-14:25"));
        TIME_LIST.add(App.sharedPreferences.getString("4", "14:40-16:10"));
        TIME_LIST.add(App.sharedPreferences.getString("5", "17:30-20:00"));
    }

    private DateUtils() {
    }

    /**
     * 获取上课进度
     *
     * @param max               最大进度
     * @param classScheduleList 课程集合
     * @return 当前进度
     */
    @CheckResult
    public static int getNowProgress(int max, List<ClassSchedule> classScheduleList) {
        int whichClassNow = getWhichClassNow();
        if (whichClassNow == -1) {
            return 0;
        }
        boolean have = false;
        for (ClassSchedule c : classScheduleList) {
            if (c.getSection() == getWhichClassNow() + 1) {
                have = true;
            }
        }
        if (!have) {
            return 0;
        }
        try {
            String[] classItemArray = TIME_LIST.get(whichClassNow).split("-");
            String start = classItemArray[0];
            String end = classItemArray[1];
            long startTime = DF.parse(start).getTime();
            long endTime = DF.parse(end).getTime();
            long nowTime = DF.parse(DF.format(new Date())).getTime();
            long totalTime = endTime - startTime;
            if (nowTime <= startTime) {
                return 0;
            } else if (nowTime >= endTime) {
                return max;
            } else {
                double l = (nowTime - startTime) / (double) totalTime;
                return (int) (l * max);
            }
        } catch (ParseException e) {
            Log.e(TAG, "get progress parse exception ", e);
            return 0;
        }
    }

    /**
     * 获取哪节课正在上,或者要上
     *
     * @return 第几节课, 没有返回-1,注意返回从0开始
     */
    @CheckResult
    public static int getWhichClassNow() {
        int i = 0;
        String endTimeStr = null;
        for (String s : TIME_LIST) {
            String[] timeArray = s.split("-");
            if (isInDateInterval(timeArray[0], timeArray[1])) {
                return i;
            }
            if (i != 0 && isInDateInterval(endTimeStr, timeArray[0])) {
                return i;
            }
            endTimeStr = timeArray[1];
            i++;
        }

        return -1;
    }

    /**
     * 获取时间集合
     *
     * @return 集合
     */
    @CheckResult
    public static List<String> getTimeList() {
        return TIME_LIST;
    }

    /**
     * 刷新时间数据集合
     */
    public static void refreshTimeList() {
        TIME_LIST.clear();
        TIME_LIST.add(App.sharedPreferences.getString("1", "08:20-09:50"));
        TIME_LIST.add(App.sharedPreferences.getString("2", "10:05-11:35"));
        TIME_LIST.add(App.sharedPreferences.getString("3", "12:55-14:25"));
        TIME_LIST.add(App.sharedPreferences.getString("4", "14:40-16:10"));
        TIME_LIST.add(App.sharedPreferences.getString("5", "17:30-20:00"));
    }

    /**
     * 获取星期
     *
     * @return 1~7
     */
    @CheckResult
    public static int getWeek() {
        CAL.setTime(new Date());
        int i = CAL.get(Calendar.DAY_OF_WEEK);
        if (i == Calendar.SUNDAY) {
            //星期日
            return 7;
        } else {
            return CAL.get(Calendar.DAY_OF_WEEK) - 1;
        }

    }

    /**
     * 是否需要重新加载数据<br/>
     * 新的一天需要重新加载数据
     *
     * @return 需要返回true
     */
    @CheckResult
    public static boolean isNewDay() {
        int last = App.sharedPreferences.getInt(ConstantPool.Str.LAST_DATE.get(), 0);
        int i = Calendar.getInstance().get(Calendar.DATE);
        App.sharedPreferences.edit().putInt(ConstantPool.Str.LAST_DATE.get(), i).apply();
        Log.d(TAG, "need refresh : " + (last != i));
        return last != i;
    }

    /**
     * 返回现在到给定时间相差的分钟数
     *
     * @param endTime 结束时间
     * @return 相差分钟数
     */
    @CheckResult
    public static int getTheRestOfTheTime(String endTime) {
        try {
            long end = DF.parse(endTime).getTime();
            long now = DF.parse(DF.format(new Date())).getTime();
            int minutes = (int) ((end - now) / (1000 * 60));
            if (minutes < 0) {
                return 0;
            }
            return minutes;
        } catch (ParseException e) {
            Log.e(TAG, "parse exception ", e);
            return 0;
        }
    }

    /**
     * 检查当前时间是否在给定的开始结束时间内
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 在返回true
     */
    @CheckResult
    public static boolean isInDateInterval(String start, String end) {
        try {
            return belongCalendar(DF.parse(DF.format(new Date())), DF.parse(start), DF.parse(end));
        } catch (ParseException e) {
            Log.e(TAG, "parse exception:", e);
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 判断时间是否在时间段内
     *
     * @param nowTime   现在
     * @param beginTime 开始
     * @param endTime   结束
     * @return 是返回True
     */
    @CheckResult
    private static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        return nowTime.getTime() >= beginTime.getTime() && nowTime.getTime() < endTime.getTime();
    }

    /**
     * 时间区间合法
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 不合法返回真
     */
    @CheckResult
    public static boolean isTimeIintervalLegitimate(String startTime, String endTime) {
        try {
            long start = DF.parse(startTime).getTime();
            long end = DF.parse(endTime).getTime();
            return start >= end;
        } catch (ParseException e) {
            Log.e(TAG, "time format error: ", e);
            return true;
        }
    }

    /**
     * 数据合法性检查
     *
     * @return 合法返回真
     */
    public static boolean isDataLegitimate(TreeMap<String, String> timeMap, Context context) {
        Map.Entry<String, String> lastEntry = null;
        for (Map.Entry<String, String> entry : timeMap.entrySet()) {
            String[] timeArray = entry.getValue().split("-");
            //检查每节课上下课时间合法性
            if (DateUtils.isTimeIintervalLegitimate(timeArray[0], timeArray[1])) {
                Log.d(TAG, "error1: " + timeArray[0] + "-->" + timeArray[1]);
                showTimeErrorDialog(entry.getKey(), 1, context);
                return false;
            }
            if (lastEntry != null && !"5".equals(entry.getKey())) {
                String[] lastTimeArray = lastEntry.getValue().split("-");
                if (DateUtils.isTimeIintervalLegitimate(lastTimeArray[1], timeArray[0])) {
                    Log.d(TAG, "error2: " + lastTimeArray[1] + "-->" + timeArray[0]);
                    showTimeErrorDialog(lastEntry.getKey(), 2, context);
                    return false;
                }
            }
            lastEntry = entry;
        }
        return true;
    }

    /**
     * 显示错误Dialog
     *
     * @param whichClass 哪节课
     * @param type       类型
     */
    private static void showTimeErrorDialog(String whichClass, int type, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("错误");
        if (type == 1) {
            builder.setMessage("第" + whichClass + "节课上下课时间冲突,请检查");

        } else {
            builder.setMessage("第" + whichClass + "节课下课和" + (Integer.parseInt(whichClass) + 1) + "节课时间冲突,请检查");
        }
        builder.setPositiveButton("确定", null)
                .show();
    }
}
