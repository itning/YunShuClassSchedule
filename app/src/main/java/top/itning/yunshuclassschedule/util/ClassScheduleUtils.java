package top.itning.yunshuclassschedule.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.ui.adapter.ClassScheduleItemLongClickListener;

/**
 * 课程表工具类
 *
 * @author itning
 */
@SuppressWarnings("unused")
public class ClassScheduleUtils {
    private static final String TAG = "ClassScheduleUtils";

    private static final int CLASS_SECTION = 5;
    private static final int CLASS_WEEK = 7;
    private static float weekFont;

    private ClassScheduleUtils() {

    }

    private static final List<ClassSchedule> ORDER_LIST = new ArrayList<>();
    public static final List<String> COPY_LIST = new ArrayList<>();
    /**
     * 颜色数组
     */
    private static int[] colorArray = new int[7];
    /**
     * 存储课程颜色
     */
    private static SparseIntArray sparseArray = new SparseIntArray();
    /**
     * 课程计数
     */
    private static int scheduleCount = 0;

    /**
     * 加载课程视图
     *
     * @param classScheduleList 课程
     * @param gridLayout        {@link GridLayout}
     * @param activity          {@link Activity}
     */
    public static void loadingView(List<ClassSchedule> classScheduleList, @NonNull GridLayout gridLayout, @NonNull ClassScheduleItemLongClickListener clickListener, @NonNull Activity activity) {
        initColorArray(activity);
        initFontSize();
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        gridLayout.removeViews(13, gridLayout.getChildCount() - 13);
        for (int i = 0; i < CLASS_SECTION; i++) {
            for (int j = 0; j < CLASS_WEEK; j++) {
                gridLayout.addView(setNull(activity, i + 1, j + 1), setParams(i + 1, j + 1, size));
            }
        }
        if (classScheduleList != null) {
            for (ClassSchedule classSchedule : classScheduleList) {
                gridLayout.removeView(gridLayout.findViewWithTag(classSchedule.getSection() + "-" + classSchedule.getWeek()));
                gridLayout.addView(setClass(showText(classSchedule), getColor(classSchedule.getName()), activity, classSchedule.getSection(), classSchedule.getWeek()), setParams(classSchedule.getSection(), classSchedule.getWeek(), size));
            }
        }

        int childCount = gridLayout.getChildCount();
        clickListener.updateBtnBackgroundTintList();
        for (int i = 0; i < childCount; i++) {
            View view = gridLayout.getChildAt(i);
            view.setOnLongClickListener(clickListener);
        }
    }

    /**
     * 设置单元格内容
     *
     * @param text            课程字符串
     * @param backgroundColor 背景颜色
     * @param context         {@link Context}
     * @param x               坐标
     * @param y               坐标
     * @return {@link View}
     */
    @CheckResult
    private static View setClass(String text, @ColorInt int backgroundColor, @NonNull Context context, int x, int y) {
        CardView cardView = new CardView(context);
        cardView.setCardBackgroundColor(Color.TRANSPARENT);
        TextView textView = new TextView(context);
        textView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        textView.setPadding(5, 5, 5, 5);
        textView.setText(text);
        textView.setBackgroundColor(backgroundColor);
        textView.setTextSize(weekFont);
        cardView.addView(textView);
        cardView.setTag(x + "-" + y);
        Log.d(TAG, "card view tag is " + cardView.getTag());
        return cardView;
    }

    /**
     * 设置空课
     *
     * @param context {@link Context}
     * @param x       坐标
     * @param y       坐标
     * @return {@link View}
     */
    @CheckResult
    private static View setNull(@NonNull Context context, int x, int y) {
        View view = new View(context);
        view.setTag(x + "-" + y);
        Log.d(TAG, "null view tag is " + view.getTag());
        return view;
    }


    /**
     * 设置单元格样式
     *
     * @param row    行
     * @param column 列
     * @param size   {@link Point}
     * @return {@link GridLayout.LayoutParams}
     */
    @CheckResult
    private static GridLayout.LayoutParams setParams(int row, int column, Point size) {
        //设置它的行和列
        GridLayout.Spec rowSpec = GridLayout.spec(row, 1.0f);
        GridLayout.Spec columnSpec = GridLayout.spec(column, 1.0f);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
        params.setGravity(Gravity.FILL);
        params.setMargins(2, 2, 2, 2);
        //设置宽高
        params.height = size.y / 6;
        params.width = size.x / 8 - 5;
        return params;
    }

    /**
     * 初始化颜色数组
     *
     * @param context {@link Context}
     */
    private static void initColorArray(@NonNull Context context) {
        colorArray[0] = ContextCompat.getColor(context, R.color.class_color_1);
        colorArray[1] = ContextCompat.getColor(context, R.color.class_color_2);
        colorArray[2] = ContextCompat.getColor(context, R.color.class_color_3);
        colorArray[3] = ContextCompat.getColor(context, R.color.class_color_4);
        colorArray[4] = ContextCompat.getColor(context, R.color.class_color_5);
        colorArray[5] = ContextCompat.getColor(context, R.color.class_color_6);
        colorArray[6] = ContextCompat.getColor(context, R.color.class_color_7);
    }

    /**
     * 获取填充颜色<br/>
     * 相同课程确保同一种颜色
     *
     * @param text 课程字符串
     * @return 颜色
     */
    @ColorInt
    @CheckResult
    private static int getColor(String text) {
        int hashCode = text.hashCode();
        int i = sparseArray.get(hashCode);
        if (i != 0) {
            //重复课程
            return i;
        } else {
            int color = colorArray[scheduleCount % colorArray.length];
            sparseArray.put(hashCode, color);
            scheduleCount++;
            return color;
        }
    }

    /**
     * 未来时间段内是否有课
     *
     * @param classScheduleList 课程列表
     * @return 有课返回true
     */
    public static boolean haveClassAfterTime(List<ClassSchedule> classScheduleList) {
        if (classScheduleList.isEmpty()) {
            return false;
        }
        int whichClassNow = DateUtils.getWhichClassNow();
        if (whichClassNow == -1) {
            ClassSchedule classSchedule = classScheduleList.get(0);
            String[] firstTimeArray = DateUtils.getTimeList().get(classSchedule.getSection() - 1).split("-");
            try {
                return DateUtils.DF.parse(DateUtils.DF.format(new Date())).getTime() <= DateUtils.DF.parse(firstTimeArray[0]).getTime();
            } catch (ParseException e) {
                return false;
            }

        }
        while (true) {
            for (ClassSchedule classSchedule : classScheduleList) {
                if (classSchedule.getSection() == whichClassNow + 1) {
                    return true;
                }
            }
            whichClassNow++;
            if (whichClassNow > 5) {
                return false;
            }
        }
    }

    /**
     * 初始化字体大小
     */
    private static void initFontSize() {
        weekFont = App.sharedPreferences.getFloat(ConstantPool.Str.WEEK_FONT_SIZE.get(), 12);
    }

    /**
     * 格式化展示在TextView的文字
     *
     * @param classSchedule {@link ClassSchedule}
     * @return 格式化完的文字
     */
    @CheckResult
    private static String showText(ClassSchedule classSchedule) {
        if (App.sharedPreferences.getBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), false)) {
            return classSchedule.getName() + "@" + classSchedule.getLocation() + "\n" + classSchedule.getTeacher();
        } else {
            return classSchedule.getName() + "@" + classSchedule.getLocation();
        }

    }

    /**
     * 重新将给定的课程集合排序<br/>
     * 排序规则:当前正在上的课,课程节数
     *
     * @param classScheduleList 课程集合
     * @return 已排序课程集合
     */
    @CheckResult
    public static List<ClassSchedule> orderListBySection(List<ClassSchedule> classScheduleList) {
        if (classScheduleList.isEmpty() || classScheduleList.size() == 1) {
            return classScheduleList;
        }
        int order = 1;
        int whichClassNow = DateUtils.getWhichClassNow();
        if (whichClassNow != -1) {
            while (true) {
                if (ORDER_LIST.isEmpty()) {
                    for (int i = 0; i < classScheduleList.size(); i++) {
                        if (classScheduleList.get(i).getSection() == whichClassNow + 1) {
                            ORDER_LIST.add(classScheduleList.get(i));
                            break;
                        }
                    }
                    whichClassNow++;
                    if (whichClassNow > 5) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        whichClassNow--;
        do {
            for (int i = 0; i < classScheduleList.size(); i++) {
                int section = classScheduleList.get(i).getSection();
                if (section == order && section != whichClassNow + 1) {
                    ORDER_LIST.add(classScheduleList.get(i));
                    break;
                }
            }
            order++;
        } while (ORDER_LIST.size() != classScheduleList.size());
        classScheduleList.clear();
        classScheduleList.addAll(ORDER_LIST);
        ORDER_LIST.clear();
        return classScheduleList;
    }
}
