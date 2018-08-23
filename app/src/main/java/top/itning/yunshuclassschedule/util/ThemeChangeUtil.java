package top.itning.yunshuclassschedule.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import top.itning.yunshuclassschedule.R;

/**
 * 主题更换工具类
 *
 * @author itning
 */
public class ThemeChangeUtil {
    public static boolean isChange = false;

    public synchronized static void changeNightMode(@NonNull AppCompatActivity activity) {
        ThemeChangeUtil.isChange = !ThemeChangeUtil.isChange;
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.finish();
    }

    public static void changeTheme(@NonNull Activity activity) {
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme);
        }
    }

    public static void changeSettingTheme(@NonNull Activity activity) {
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme_Setting);
        }
    }

    public static void setTabLayoutColor(@NonNull Context context, @NonNull TabLayout tabLayout) {
        if (isChange) {
            int colorAccent = ContextCompat.getColor(context, R.color.nightThemeColorAccent);
            int colorNormal = ContextCompat.getColor(context, android.R.color.white);
            tabLayout.setBackgroundResource(R.color.nightThemeColorPrimary);
            tabLayout.setSelectedTabIndicatorColor(colorAccent);
            tabLayout.setTabTextColors(colorNormal, colorAccent);
        }
    }

    public static void setBackgroundResources(@NonNull View... views) {
        if (isChange) {
            for (View v : views) {
                v.setBackgroundResource(R.color.nightThemeColorPrimary);
            }
        }
    }

    public static void setProgressBackgroundResource(@NonNull View view) {
        if (isChange) {
            view.setBackgroundResource(R.color.color_progress_night);
        }
    }
}
