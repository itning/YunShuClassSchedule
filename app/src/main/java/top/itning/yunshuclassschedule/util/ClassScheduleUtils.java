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
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.ClassSchedule;

/**
 * 课程表工具类
 *
 * @author itning
 */
@SuppressWarnings("unused")
public class ClassScheduleUtils {
    private ClassScheduleUtils() {

    }

    private static final List<ClassSchedule> ORDER_LIST = new ArrayList<>();

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
     * @param context           {@link Context}
     * @param activity          {@link Activity}
     */
    public static void loadingView(List<ClassSchedule> classScheduleList, @NonNull GridLayout gridLayout, @NonNull Context context, @NonNull Activity activity) {
        initColorArray(context);
        Display display = Objects.requireNonNull(activity).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                gridLayout.addView(setNull(context), setParams(i + 1, j + 1, size));
            }
        }
        if (classScheduleList != null) {
            for (ClassSchedule classSchedule : classScheduleList) {
                gridLayout.addView(setClass(classSchedule.getName(), getColor(classSchedule.getName()), context), setParams(classSchedule.getSection(), classSchedule.getWeek(), size));
            }
        }
    }

    /**
     * 设置单元格内容
     *
     * @param text            课程字符串
     * @param backgroundColor 背景颜色
     * @param context         {@link Context}
     * @return {@link View}
     */
    @CheckResult
    private static View setClass(String text, @ColorInt int backgroundColor, @NonNull Context context) {
        CardView cardView = new CardView(context);
        cardView.setCardBackgroundColor(Color.TRANSPARENT);
        TextView textView = new TextView(context);
        textView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        textView.setPadding(5, 5, 5, 5);
        textView.setText(text);
        textView.setBackgroundColor(backgroundColor);
        textView.setTextSize(12);
        cardView.addView(textView);
        return cardView;
    }

    /**
     * 设置空课
     *
     * @param context {@link Context}
     * @return {@link View}
     */
    @CheckResult
    private static View setNull(@NonNull Context context) {
        return new TextView(context);
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
     * 获取文字
     *
     * @param text 课程字符串
     * @return 格式化课程字符串
     */
    @CheckResult
    private static String getText(String text) {
        String[] strings = text.split("@");
        return strings[0] + "@" + strings[1] + "\n" + strings[2];
    }

    @CheckResult
    public static List<ClassSchedule> orderListBySection(List<ClassSchedule> classScheduleList) {
        int order = 1;

        int whichClassNow = DateUtils.getWhichClassNow();
        if (whichClassNow != -1) {
            for (int i = 0; i < classScheduleList.size(); i++) {
                if (classScheduleList.get(i).getSection() == whichClassNow + 1) {
                    ORDER_LIST.add(classScheduleList.get(i));
                    break;
                }
            }
        }
        while (true) {
            for (int i = 0; i < classScheduleList.size(); i++) {
                int section = classScheduleList.get(i).getSection();
                if (section == order && section != whichClassNow + 1) {
                    ORDER_LIST.add(classScheduleList.get(i));
                    break;
                }
            }
            order++;
            if (ORDER_LIST.size() == classScheduleList.size()) {
                break;
            }
        }
        classScheduleList.clear();
        classScheduleList.addAll(ORDER_LIST);
        ORDER_LIST.clear();
        return classScheduleList;
    }
}
