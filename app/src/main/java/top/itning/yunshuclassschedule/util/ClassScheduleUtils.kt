package top.itning.yunshuclassschedule.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.util.Log
import android.util.SparseIntArray
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.ClassSchedule
import top.itning.yunshuclassschedule.ui.adapter.ClassScheduleItemLongClickListener
import java.text.ParseException
import java.util.*

/**
 * 课程表工具类
 *
 * @author itning
 */
object ClassScheduleUtils {
    private const val TAG = "ClassScheduleUtils"

    private const val CLASS_SECTION = 5
    private const val CLASS_WEEK = 7
    private var weekFont: Float = 0.toFloat()
    private val ORDER_LIST = ArrayList<ClassSchedule>()
    val COPY_LIST: List<String> = ArrayList()
    /**
     * 颜色数组
     */
    private val colorArray = IntArray(7)
    /**
     * 存储课程颜色
     */
    private val SPARSE_ARRAY = SparseIntArray()
    /**
     * 课程计数
     */
    private var scheduleCount = 0

    /**
     * 加载课程视图
     *
     * @param classScheduleList 课程
     * @param gridLayout        [GridLayout]
     * @param activity          [Activity]
     */
    fun loadingView(classScheduleList: List<ClassSchedule>?, @NonNull gridLayout: GridLayout, @NonNull clickListener: ClassScheduleItemLongClickListener, @NonNull activity: Activity) {
        initColorArray(activity)
        initFontSize()
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        gridLayout.removeViews(8, gridLayout.childCount - 8)
        for (i in 0 until 5) {
            val textView = TextView(activity)
            textView.text = String.format("%d", i + 1)
            val rowSpec = GridLayout.spec(i + 1, 1.0f)
            val columnSpec = GridLayout.spec(0, 1.0f)
            val params = GridLayout.LayoutParams(rowSpec, columnSpec)
            params.setGravity(Gravity.CENTER_VERTICAL)
            gridLayout.addView(textView, params)
        }
        for (i in 0 until CLASS_SECTION) {
            for (j in 0 until CLASS_WEEK) {
                gridLayout.addView(setNull(activity, i + 1, j + 1), setParams(i + 1, j + 1, size))
            }
        }
        if (classScheduleList != null) {
            for (classSchedule in classScheduleList) {
                gridLayout.removeView(gridLayout.findViewWithTag(classSchedule.section.toString() + "-" + classSchedule.week))
                gridLayout.addView(setClass(showText(classSchedule), getColor(classSchedule.name), activity, classSchedule.section, classSchedule.week), setParams(classSchedule.section, classSchedule.week, size))
            }
        }

        val childCount = gridLayout.childCount
        clickListener.updateBtnBackgroundTintList()
        for (i in 0 until childCount) {
            val view = gridLayout.getChildAt(i)
            view.setOnLongClickListener(clickListener)
        }
    }

    /**
     * 设置单元格内容
     *
     * @param text            课程字符串
     * @param backgroundColor 背景颜色
     * @param context         [Context]
     * @param x               坐标
     * @param y               坐标
     * @return [View]
     */
    @CheckResult
    private fun setClass(text: String, @ColorInt backgroundColor: Int, @NonNull context: Context, x: Int, y: Int): View {
        val cardView = CardView(context)
        cardView.setCardBackgroundColor(Color.TRANSPARENT)
        val textView = TextView(context)
        textView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        textView.setPadding(5, 5, 5, 5)
        textView.text = text
        textView.setBackgroundColor(backgroundColor)
        textView.textSize = weekFont
        cardView.addView(textView)
        cardView.tag = "$x-$y"
        Log.d(TAG, "card view tag is " + cardView.tag)
        return cardView
    }

    /**
     * 设置空课
     *
     * @param context [Context]
     * @param x       坐标
     * @param y       坐标
     * @return [View]
     */
    @CheckResult
    private fun setNull(@NonNull context: Context, x: Int, y: Int): View {
        val view = View(context)
        view.tag = "$x-$y"
        Log.d(TAG, "null view tag is " + view.tag)
        return view
    }


    /**
     * 设置单元格样式
     *
     * @param row    行
     * @param column 列
     * @param size   [Point]
     * @return [GridLayout.LayoutParams]
     */
    @CheckResult
    private fun setParams(row: Int, column: Int, size: Point): GridLayout.LayoutParams {
        //设置它的行和列
        val rowSpec = GridLayout.spec(row, 1.0f)
        val columnSpec = GridLayout.spec(column, 1.0f)
        val params = GridLayout.LayoutParams(rowSpec, columnSpec)
        params.setGravity(Gravity.FILL)
        params.setMargins(2, 2, 2, 2)
        //设置宽高
        params.height = size.y / 6
        params.width = size.x / 8 - 5
        return params
    }

    /**
     * 初始化颜色数组
     *
     * @param context [Context]
     */
    private fun initColorArray(@NonNull context: Context) {
        colorArray[0] = ContextCompat.getColor(context, R.color.class_color_1)
        colorArray[1] = ContextCompat.getColor(context, R.color.class_color_2)
        colorArray[2] = ContextCompat.getColor(context, R.color.class_color_3)
        colorArray[3] = ContextCompat.getColor(context, R.color.class_color_4)
        colorArray[4] = ContextCompat.getColor(context, R.color.class_color_5)
        colorArray[5] = ContextCompat.getColor(context, R.color.class_color_6)
        colorArray[6] = ContextCompat.getColor(context, R.color.class_color_7)
    }

    /**
     * 获取填充颜色<br></br>
     * 相同课程确保同一种颜色
     *
     * @param text 课程字符串
     * @return 颜色
     */
    @ColorInt
    @CheckResult
    private fun getColor(text: String): Int {
        val hashCode = text.hashCode()
        val i = SPARSE_ARRAY.get(hashCode)
        return if (i != 0) {
            //重复课程
            i
        } else {
            val color = colorArray[scheduleCount % colorArray.size]
            SPARSE_ARRAY.put(hashCode, color)
            scheduleCount++
            color
        }
    }

    /**
     * 未来时间段内是否有课
     *
     * @param classScheduleList 课程列表
     * @return 有课返回true
     */
    fun haveClassAfterTime(classScheduleList: List<ClassSchedule>): Boolean {
        if (classScheduleList.isEmpty()) {
            return false
        }
        var whichClassNow = DateUtils.whichClassNow
        if (whichClassNow == -1) {
            val classSchedule = classScheduleList[0]
            val firstTimeArray = DateUtils.timeList[classSchedule.section - 1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return try {
                DateUtils.DF.parse(DateUtils.DF.format(Date())).time <= DateUtils.DF.parse(firstTimeArray[0]).time
            } catch (e: ParseException) {
                false
            }

        }
        while (true) {
            for (classSchedule in classScheduleList) {
                if (classSchedule.section == whichClassNow + 1) {
                    return true
                }
            }
            whichClassNow++
            if (whichClassNow > 5) {
                return false
            }
        }
    }

    /**
     * 初始化字体大小
     */
    private fun initFontSize() {
        weekFont = App.sharedPreferences.getFloat(ConstantPool.Str.WEEK_FONT_SIZE.get(), 12f)
    }

    /**
     * 格式化展示在TextView的文字
     *
     * @param classSchedule [ClassSchedule]
     * @return 格式化完的文字
     */
    @CheckResult
    private fun showText(classSchedule: ClassSchedule): String {
        return if (App.sharedPreferences.getBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), false)) {
            classSchedule.name + "@" + classSchedule.location + "\n" + classSchedule.teacher
        } else {
            classSchedule.name + "@" + classSchedule.location
        }

    }

    /**
     * 重新将给定的课程集合排序<br></br>
     * 排序规则:当前正在上的课,课程节数
     *
     * @param classScheduleList 课程集合
     * @return 已排序课程集合
     */
    @CheckResult
    fun orderListBySection(classScheduleList: MutableList<ClassSchedule>): MutableList<ClassSchedule> {
        if (classScheduleList.isEmpty() || classScheduleList.size == 1) {
            return classScheduleList
        }
        var order = 1
        var whichClassNow = DateUtils.whichClassNow
        if (whichClassNow != -1) {
            while (true) {
                if (ORDER_LIST.isEmpty()) {
                    for (i in classScheduleList.indices) {
                        if (classScheduleList[i].section == whichClassNow + 1) {
                            ORDER_LIST.add(classScheduleList[i])
                            break
                        }
                    }
                    whichClassNow++
                    if (whichClassNow > 5) {
                        break
                    }
                } else {
                    break
                }
            }
        }
        whichClassNow--
        do {
            for (i in classScheduleList.indices) {
                val section = classScheduleList[i].section
                if (section == order && section != whichClassNow + 1) {
                    ORDER_LIST.add(classScheduleList[i])
                    break
                }
            }
            order++
        } while (ORDER_LIST.size != classScheduleList.size)
        classScheduleList.clear()
        classScheduleList.addAll(ORDER_LIST)
        ORDER_LIST.clear()
        return classScheduleList
    }
}
