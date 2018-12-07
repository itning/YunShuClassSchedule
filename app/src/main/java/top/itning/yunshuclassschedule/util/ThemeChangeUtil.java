package top.itning.yunshuclassschedule.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.jaeger.library.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;

import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.APP_COLOR_ACCENT;
import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.APP_COLOR_PRIMARY;
import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.APP_COLOR_PRIMARY_DARK;
import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.APP_COLOR_PROGRESS;

/**
 * 主题更换工具类
 *
 * @author itning
 */
public class ThemeChangeUtil {
    private static final String TAG = "ThemeChangeUtil";

    public static boolean isChange = App.sharedPreferences.getBoolean("night_mode", false);
    private static int defaultColorPrimary;
    private static int defaultColorPrimaryDark;
    private static int defaultColorAccent;
    private static int defaultColorProgress;

    /**
     * 更换夜间模式
     *
     * @param activity {@link AppCompatActivity}
     */
    public synchronized static void changeNightMode(@NonNull AppCompatActivity activity) {
        ThemeChangeUtil.isChange = !ThemeChangeUtil.isChange;
        App.sharedPreferences.edit().putBoolean("night_mode", ThemeChangeUtil.isChange).apply();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.finish();
    }

    /**
     * 更新颜色事件
     */
    public synchronized static void changeColor() {
        Log.d(TAG, "app color change , send event");
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.APP_COLOR_CHANGE));
    }

    /**
     * 初始化有DrawerLayout的Activity的颜色
     *
     * @param activity     {@link AppCompatActivity}
     * @param drawerLayout {@link DrawerLayout}
     */
    public static void initColor(@NonNull AppCompatActivity activity, DrawerLayout drawerLayout) {
        if (!isChange) {
            ActionBar supportActionBar = activity.getSupportActionBar();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            if (supportActionBar != null) {
                int appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary);
                supportActionBar.setBackgroundDrawable(new ColorDrawable(appColorPrimary));
            }
            int appColorPrimaryDark = sharedPreferences.getInt(APP_COLOR_PRIMARY_DARK, defaultColorPrimaryDark);
            StatusBarUtil.setColorForDrawerLayout(activity, drawerLayout, appColorPrimaryDark, 10);
        }
    }

    /**
     * 更新主Activity主题
     *
     * @param activity {@link AppCompatActivity}
     */
    public static void changeMainActivityTheme(@NonNull AppCompatActivity activity) {
        initDefaultColor(activity);
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme);
            Window window = activity.getWindow();
            //设置状态栏透明
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 简单设置主题
     *
     * @param activity {@link AppCompatActivity}
     */
    public static void simpleSetTheme(@NonNull AppCompatActivity activity) {
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme_Setting);
        }
    }

    /**
     * 初始化默认颜色
     *
     * @param activity {@link AppCompatActivity}
     */
    private static void initDefaultColor(@NonNull AppCompatActivity activity) {
        if (defaultColorPrimary != 0) {
            return;
        }
        defaultColorPrimary = ContextCompat.getColor(activity, R.color.colorPrimary);
        defaultColorPrimaryDark = ContextCompat.getColor(activity, R.color.colorPrimaryDark);
        defaultColorAccent = ContextCompat.getColor(activity, R.color.colorAccent);
        defaultColorProgress = ContextCompat.getColor(activity, R.color.color_progress);
    }

    /**
     * 更新设置页面主题
     *
     * @param activity {@link AppCompatActivity}
     */
    public static void changeTheme(@NonNull AppCompatActivity activity) {
        initDefaultColor(activity);
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme_Setting);
            return;
        }
        ActionBar actionBar = activity.getSupportActionBar();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        if (actionBar != null) {
            int appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary);
            actionBar.setBackgroundDrawable(new ColorDrawable(appColorPrimary));
        }
        int appColorPrimaryDark = sharedPreferences.getInt(APP_COLOR_PRIMARY_DARK, defaultColorPrimaryDark);
        StatusBarUtil.setColor(activity, appColorPrimaryDark, 30);
    }

    /**
     * 设置TabLayout颜色
     *
     * @param context   {@link Context}
     * @param tabLayout {@link TabLayout}
     */
    public static void setTabLayoutColor(@NonNull Context context, @NonNull TabLayout tabLayout) {
        int colorNormal = ContextCompat.getColor(context, android.R.color.white);
        if (isChange) {
            int colorAccent = ContextCompat.getColor(context, R.color.nightThemeColorAccent);
            tabLayout.setBackgroundResource(R.color.nightThemeColorPrimary);
            tabLayout.setSelectedTabIndicatorColor(colorAccent);
            tabLayout.setTabTextColors(colorNormal, colorAccent);
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary);
        int appColorAccent = sharedPreferences.getInt(APP_COLOR_ACCENT, defaultColorAccent);
        tabLayout.setBackgroundColor(appColorPrimary);
        tabLayout.setSelectedTabIndicatorColor(appColorAccent);
        tabLayout.setTabTextColors(colorNormal, appColorAccent);
    }

    /**
     * 设置背景Resource
     *
     * @param context {@link Context}
     * @param views   {@link View}
     */
    public static void setBackgroundResources(@NonNull Context context, @NonNull View... views) {
        if (isChange) {
            for (View v : views) {
                v.setBackgroundResource(R.color.nightThemeColorPrimary);
            }
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary);
        for (View v : views) {
            if (v.getId() == R.id.view_center || v.getId() == R.id.view_top || v.getId() == R.id.view_bottom) {
                continue;
            }
            v.setBackgroundColor(appColorPrimary);
        }
    }

    /**
     * 设置进度条颜色
     *
     * @param context {@link Context}
     * @param view    {@link View}
     */
    public static void setProgressBackgroundResource(@NonNull Context context, @NonNull View view) {
        if (isChange) {
            view.setBackgroundResource(R.color.color_progress_night);
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int appColorProgress = sharedPreferences.getInt(APP_COLOR_PROGRESS, defaultColorProgress);
        view.setBackgroundColor(appColorProgress);
    }

    /**
     * 获取当前强调色颜色
     *
     * @param context {@link Context}
     * @return 颜色数值
     */
    @CheckResult
    @ColorInt
    public static int getNowThemeColorAccent(@NonNull Context context) {
        if (isChange) {
            return ContextCompat.getColor(context, R.color.nightThemeColorAccent);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(APP_COLOR_ACCENT, defaultColorAccent);
        }
    }

    /**
     * 设置TextView 颜色
     *
     * @param context   {@link Context}
     * @param textViews {@link TextView}
     */
    public static void setTextViewsColorByTheme(@NonNull Context context, @NonNull TextView... textViews) {
        final boolean isChange = ThemeChangeUtil.isChange;
        final int whiteColor = ContextCompat.getColor(context, android.R.color.white);
        final int blackColor = ContextCompat.getColor(context, android.R.color.black);
        for (TextView textView : textViews) {
            if (isChange) {
                textView.setTextColor(whiteColor);
            } else {
                textView.setTextColor(blackColor);
            }
        }
    }
}
