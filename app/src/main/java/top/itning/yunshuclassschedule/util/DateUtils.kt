package top.itning.yunshuclassschedule.util

import android.content.Context
import android.util.Log
import androidx.annotation.CheckResult
import androidx.appcompat.app.AlertDialog
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.ClassSchedule
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间工具
 *
 * @author itning
 */
object DateUtils {
    private const val TAG = "DateUtils"
    val DF = SimpleDateFormat("HH:mm", Locale.CHINESE)
    private val TIME_LIST = ArrayList<String>()
    private val CAL = Calendar.getInstance()

    /**
     * 获取哪节课正在上,或者要上
     *
     * @return 第几节课, 没有返回-1,注意返回从0开始
     */
    val whichClassNow: Int
        @CheckResult
        get() {
            var endTimeStr: String? = null
            for ((i, s) in TIME_LIST.withIndex()) {
                val timeArray = s.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (isInDateInterval(timeArray[0], timeArray[1])) {
                    return i
                }
                if (i != 0 && isInDateInterval(endTimeStr, timeArray[0])) {
                    return i
                }
                endTimeStr = timeArray[1]
            }
            return -1
        }

    /**
     * 获取时间集合
     *
     * @return 集合
     */
    val timeList: List<String>
        @CheckResult
        get() = TIME_LIST

    /**
     * 获取星期
     *
     * @return 1~7
     */
    //星期日
    val week: Int
        @CheckResult
        get() {
            CAL.time = Date()
            val i = CAL.get(Calendar.DAY_OF_WEEK)
            return if (i == Calendar.SUNDAY) {
                7
            } else {
                CAL.get(Calendar.DAY_OF_WEEK) - 1
            }

        }

    /**
     * 是否需要重新加载数据<br></br>
     * 新的一天需要重新加载数据
     *
     * @return 需要返回true
     */
    val isNewDay: Boolean
        @CheckResult
        get() {
            val last = App.sharedPreferences.getInt(ConstantPool.Str.LAST_DATE.get(), 0)
            val i = Calendar.getInstance().get(Calendar.DATE)
            App.sharedPreferences.edit().putInt(ConstantPool.Str.LAST_DATE.get(), i).apply()
            Log.d(TAG, "need refresh : " + (last != i))
            return last != i
        }

    init {
        for (i in 1..App.sharedPreferences.getInt(ConstantPool.Str.CLASS_SECTION.get(), 5)) {
            TIME_LIST.add(App.sharedPreferences.getString(i.toString(), "12:00-12:01")!!)
        }
    }

    /**
     * 获取上课进度
     *
     * @param max               最大进度
     * @param classScheduleList 课程集合
     * @return 当前进度
     */
    @CheckResult
    fun getNowProgress(max: Int, classScheduleList: List<ClassSchedule>): Int {
        val whichClassNow = whichClassNow
        if (whichClassNow == -1) {
            return 0
        }
        var have = false
        for (c in classScheduleList) {
            if (c.section == whichClassNow + 1) {
                have = true
            }
        }
        if (!have) {
            return 0
        }
        try {
            val classItemArray = TIME_LIST[whichClassNow].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val start = classItemArray[0]
            val end = classItemArray[1]
            val startTime = DF.parse(start).time
            val endTime = DF.parse(end).time
            val nowTime = DF.parse(DF.format(Date())).time
            val totalTime = endTime - startTime
            return when {
                nowTime <= startTime -> 0
                nowTime >= endTime -> max
                else -> {
                    val l = (nowTime - startTime) / totalTime.toDouble()
                    (l * max).toInt()
                }
            }
        } catch (e: ParseException) {
            Log.e(TAG, "get progress parse exception ", e)
            return 0
        }

    }

    /**
     * 刷新时间数据集合
     */
    fun refreshTimeList() {
        TIME_LIST.clear()
        for (i in 1..App.sharedPreferences.getInt(ConstantPool.Str.CLASS_SECTION.get(), 5)) {
            TIME_LIST.add(App.sharedPreferences.getString(i.toString(), "12:00-12:01")!!)
        }
    }

    @CheckResult
    fun getTheRestOfTheTime(startTime: Date, endTime: String): Int {
        return try {
            val end = DF.parse(endTime).time
            val now = DF.parse(DF.format(startTime)).time
            val minutes = ((end - now) / (1000 * 60)).toInt()
            if (minutes < 0) {
                0
            } else minutes
        } catch (e: ParseException) {
            Log.e(TAG, "parse exception ", e)
            0
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
    private fun isInDateInterval(start: String?, end: String): Boolean {
        return try {
            isBelongCalendar(DF.parse(DF.format(Date())), DF.parse(start), DF.parse(end))
        } catch (e: ParseException) {
            Log.e(TAG, "parse exception:", e)
            e.printStackTrace()
            false
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
    fun isBelongCalendar(nowTime: Date, beginTime: Date, endTime: Date): Boolean {
        return nowTime.time >= beginTime.time && nowTime.time < endTime.time
    }

    /**
     * 时间区间合法
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 不合法返回真
     */
    @CheckResult
    private fun isTimeIintervalLegitimate(startTime: String, endTime: String): Boolean {
        return try {
            val start = DF.parse(startTime).time
            val end = DF.parse(endTime).time
            start >= end
        } catch (e: ParseException) {
            Log.e(TAG, "time format error: ", e)
            true
        }
    }

    /**
     * 数据合法性检查
     *
     * @return 合法返回真
     */
    fun isDataLegitimate(timeMap: TreeMap<String, String>, context: Context): Boolean {
        var lastEntry: Map.Entry<String, String>? = null
        for (entry in timeMap.entries) {
            val timeArray = entry.value.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            //检查每节课上下课时间合法性
            if (DateUtils.isTimeIintervalLegitimate(timeArray[0], timeArray[1])) {
                Log.d(TAG, "error1: " + timeArray[0] + "-->" + timeArray[1])
                showTimeErrorDialog(entry.key, 1, context)
                return false
            }
            if (lastEntry != null && "5" != entry.key) {
                val lastTimeArray = lastEntry.value.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (DateUtils.isTimeIintervalLegitimate(lastTimeArray[1], timeArray[0])) {
                    Log.d(TAG, "error2: " + lastTimeArray[1] + "-->" + timeArray[0])
                    showTimeErrorDialog(lastEntry.key, 2, context)
                    return false
                }
            }
            lastEntry = entry
        }
        return true
    }

    /**
     * 显示错误Dialog
     *
     * @param whichClass 哪节课
     * @param type       类型
     */
    private fun showTimeErrorDialog(whichClass: String, type: Int, context: Context) {
        val builder = AlertDialog.Builder(context)
                .setTitle("错误")
        if (type == 1) {
            builder.setMessage("第" + whichClass + "节课上下课时间冲突,请检查")

        } else {
            builder.setMessage("第" + whichClass + "节课下课和" + (Integer.parseInt(whichClass) + 1) + "节课时间冲突,请检查")
        }
        builder.setPositiveButton("确定", null)
                .show()
    }
}
