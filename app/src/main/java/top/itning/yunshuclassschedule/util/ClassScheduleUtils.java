package top.itning.yunshuclassschedule.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.view.Display;
import android.view.Gravity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.ClassSchedule;

import static android.net.wifi.WifiConfiguration.Status.strings;

/**
 * 课程表工具类
 *
 * @author itning
 */
public class ClassScheduleUtils {
    private ClassScheduleUtils() {

    }

    public static void loadingView(@NonNull GridLayout gridLayout, @NonNull Context context, @NonNull Activity activity) {
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setId("2016010103");
        classSchedule.setClassArray(new String[][]{
                {"马克思哲学基本原理概论@A101@哈哈", "软件工程@A101@哈哈", "软件工程@A101@哈哈", "", "", "", ""},
                {"", "", "软件工程@A101@哈哈", "软件工程@A101@哈哈", "软件工程@A101@哈哈", "", ""},
                {"软件工程@A101@哈哈", "", "软件工程@A101@哈哈", "", "", "", ""},
                {"软件工程@A101@哈哈", "", "", "软件工程@A101@哈哈", "软件工程@A101@哈哈", "", ""},
                {"", "软件工程@A101@哈哈", "", "软件工程@A101@哈哈", "软件工程@A101@哈哈", "", ""},
        });

        Display display = Objects.requireNonNull(activity).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        String[][] classArray = classSchedule.getClassArray();
        for (int i = 0; i < classArray.length; i++) {
            for (int j = 0; j < classArray[i].length; j++) {
                if ("".equals(classArray[i][j])) {
                    TextView textView = new TextView(context);
                    //设置它的行和列
                    GridLayout.Spec rowSpec = GridLayout.spec(i + 1);
                    GridLayout.Spec columnSpec = GridLayout.spec(j + 1);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
                    params.setGravity(Gravity.FILL);
                    params.setMargins(1, 1, 1, 1);
                    //设置宽高
                    params.height = size.y / 6;
                    params.width = size.x / 8 - 5;
                    gridLayout.addView(textView, params);
                } else {
                    String[] strings = classArray[i][j].split("@");
                    if (strings.length != 3) {
                        throw new IllegalArgumentException("数组分隔不正确,确保是@分隔符.->" + classArray[i][j]);
                    }
                    set(strings[0] + "@" + strings[1] + "\n" + strings[2], i + 1, j + 1, size, gridLayout, R.color.colorAccent, context);
                }
            }
        }
    }

    private static void set(String text, int row, int column, Point size, @NonNull GridLayout gridLayout, @ColorRes int backgroundColor, @NonNull Context context) {
        CardView cardView = new CardView(context);
        cardView.setCardElevation(12);
        cardView.setTranslationZ(12);

        TextView textView = new TextView(context);
        textView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        textView.setPadding(5, 5, 5, 5);
        textView.setText(text);
        textView.setBackgroundColor(ContextCompat.getColor(context, backgroundColor));
        //设置它的行和列
        GridLayout.Spec rowSpec = GridLayout.spec(row);
        GridLayout.Spec columnSpec = GridLayout.spec(column);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
        params.setGravity(Gravity.FILL);
        params.setMargins(1, 1, 1, 1);
        //设置宽高
        params.height = size.y / 6;
        params.width = size.x / 8 - 5;
        cardView.addView(textView);
        gridLayout.addView(cardView, params);
    }
}
