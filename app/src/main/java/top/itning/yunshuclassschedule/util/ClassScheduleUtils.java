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

import java.util.Objects;

import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.ClassSchedule;

/**
 * 课程表工具类
 *
 * @author itning
 */
public class ClassScheduleUtils {
    private ClassScheduleUtils() {

    }

    private static int[] colorArray = new int[7];
    private static SparseIntArray sparseArray = new SparseIntArray();
    private static int scheduleCount = 0;

    public static void loadingView(@NonNull GridLayout gridLayout, @NonNull Context context, @NonNull Activity activity) {
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setId("2016010103");
        classSchedule.setClassArray(new String[][]{
                {"计算机网络技术@B313@山镇会", "软件工程@B211@六心理", "概率论与数理统计@A102@赵微然", "WEB程序设计@B218@于洪", "", "", ""},
                {"轮滑@篮球场1@孙熏陶", "", "马克思主义基本原理概论@A401@孙建伟", "软件工程@B211@六心理", "概率论与数理统计@A102@赵微然", "", ""},
                {"四六级英语@A201@李鑫", "形式与政策@A401@余冬梅", "四六级英语@A201@李鑫", "数据库原理与应用@B216@高璐", "WEB程序设计@B218@于洪", "", ""},
                {"", "", "数据库原理与应用@B216@高璐", "马克思主义基本原理概论@A401@孙建伟", "计算机网络技术@B313@山镇会", "", ""},
                {"", "", "", "", "", "", ""},
        });

        initColorArray(context);
        Display display = Objects.requireNonNull(activity).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        String[][] classArray = classSchedule.getClassArray();
        for (int i = 0; i < classArray.length; i++) {
            for (int j = 0; j < classArray[i].length; j++) {
                if ("".equals(classArray[i][j])) {
                    gridLayout.addView(setNull(context), setParams(i + 1, j + 1, size));
                } else {
                    gridLayout.addView(setClass(getText(classArray[i][j]), getColor(classArray[i][j]), context), setParams(i + 1, j + 1, size));
                }
            }
        }
    }

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

    @CheckResult
    private static View setNull(@NonNull Context context) {
        return new TextView(context);
    }


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

    private static void initColorArray(@NonNull Context context) {
        colorArray[0] = ContextCompat.getColor(context, R.color.class_color_1);
        colorArray[1] = ContextCompat.getColor(context, R.color.class_color_2);
        colorArray[2] = ContextCompat.getColor(context, R.color.class_color_3);
        colorArray[3] = ContextCompat.getColor(context, R.color.class_color_4);
        colorArray[4] = ContextCompat.getColor(context, R.color.class_color_5);
        colorArray[5] = ContextCompat.getColor(context, R.color.class_color_6);
        colorArray[6] = ContextCompat.getColor(context, R.color.class_color_7);
    }

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

    @CheckResult
    private static String getText(String text) {
        String[] strings = text.split("@");
        return strings[0] + "@" + strings[1] + "\n" + strings[2];
    }
}
