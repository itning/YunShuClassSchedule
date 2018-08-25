package top.itning.yunshuclassschedule.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;

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

    private static boolean isChange = App.sharedPreferences.getBoolean("night_mode", false);
    private static int defaultColorPrimary;
    private static int defaultColorPrimaryDark;
    private static int defaultColorAccent;
    private static int defaultColorProgress;

    public synchronized static void changeNightMode(@NonNull AppCompatActivity activity) {
        ThemeChangeUtil.isChange = !ThemeChangeUtil.isChange;
        App.sharedPreferences.edit().putBoolean("night_mode", ThemeChangeUtil.isChange).apply();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.finish();
    }

    public synchronized static void changeColor() {
        Log.d(TAG, "app color change , send event");
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.APP_COLOR_CHANGE));
    }

    public static void initColor(@NonNull AppCompatActivity activity) {
        if (!isChange) {
            ActionBar supportActionBar = activity.getSupportActionBar();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            if (supportActionBar != null) {
                int appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary);
                supportActionBar.setBackgroundDrawable(new ColorDrawable(appColorPrimary));
            }
            Window window = activity.getWindow();
            //设置状态栏透明
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            int appColorPrimaryDark = sharedPreferences.getInt(APP_COLOR_PRIMARY_DARK, defaultColorPrimaryDark);
            window.setStatusBarColor(appColorPrimaryDark);
        }
    }

    public static void changeTheme(@NonNull AppCompatActivity activity) {
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme);
        }
        initDefaultColor(activity);
    }

    private static void initDefaultColor(@NonNull AppCompatActivity activity) {
        defaultColorPrimary = ContextCompat.getColor(activity, R.color.colorPrimary);
        defaultColorPrimaryDark = ContextCompat.getColor(activity, R.color.colorPrimaryDark);
        defaultColorAccent = ContextCompat.getColor(activity, R.color.colorAccent);
        defaultColorProgress = ContextCompat.getColor(activity, R.color.color_progress);
    }

    public static void changeSettingTheme(@NonNull AppCompatActivity activity) {
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
        Window window = activity.getWindow();
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        int appColorPrimaryDark = sharedPreferences.getInt(APP_COLOR_PRIMARY_DARK, defaultColorPrimaryDark);
        window.setStatusBarColor(appColorPrimaryDark);
    }

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

    public static void setProgressBackgroundResource(@NonNull Context context, @NonNull View view) {
        if (isChange) {
            view.setBackgroundResource(R.color.color_progress_night);
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int appColorProgress = sharedPreferences.getInt(APP_COLOR_PROGRESS, defaultColorProgress);
        view.setBackgroundColor(appColorProgress);
    }
}
