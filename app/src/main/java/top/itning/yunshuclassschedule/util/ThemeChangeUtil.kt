package top.itning.yunshuclassschedule.util

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayout
import com.jaeger.library.StatusBarUtil
import org.greenrobot.eventbus.EventBus
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.APP_COLOR_ACCENT
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.APP_COLOR_PRIMARY
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.APP_COLOR_PRIMARY_DARK
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.APP_COLOR_PROGRESS

/**
 * 主题更换工具类
 *
 * @author itning
 */
object ThemeChangeUtil {
    private const val TAG = "ThemeChangeUtil"

    var isChange = App.sharedPreferences.getBoolean("night_mode", false)
    private var defaultColorPrimary: Int = 0
    private var defaultColorPrimaryDark: Int = 0
    private var defaultColorAccent: Int = 0
    private var defaultColorProgress: Int = 0

    /**
     * 更换夜间模式
     *
     * @param activity [AppCompatActivity]
     */
    @Synchronized
    fun changeNightMode(@NonNull activity: AppCompatActivity) {
        isChange = !isChange
        App.sharedPreferences.edit().putBoolean("night_mode", isChange).apply()
        activity.startActivity(Intent(activity, activity.javaClass))
        activity.finish()
    }

    /**
     * 更新颜色事件
     */
    @Synchronized
    fun changeColor() {
        Log.d(TAG, "app color change , send event")
        EventBus.getDefault().post(EventEntity(ConstantPool.Int.APP_COLOR_CHANGE))
    }

    /**
     * 初始化有DrawerLayout的Activity的颜色
     *
     * @param activity     [AppCompatActivity]
     * @param drawerLayout [DrawerLayout]
     */
    fun initColor(@NonNull activity: AppCompatActivity, drawerLayout: DrawerLayout) {
        if (!isChange) {
            val supportActionBar = activity.supportActionBar
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            if (supportActionBar != null) {
                val appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary)
                supportActionBar.setBackgroundDrawable(ColorDrawable(appColorPrimary))
            }
            val appColorPrimaryDark = sharedPreferences.getInt(APP_COLOR_PRIMARY_DARK, defaultColorPrimaryDark)
            StatusBarUtil.setColorForDrawerLayout(activity, drawerLayout, appColorPrimaryDark, 10)
        }
    }

    /**
     * 更新主Activity主题
     *
     * @param activity [AppCompatActivity]
     */
    fun changeMainActivityTheme(@NonNull activity: AppCompatActivity) {
        initDefaultColor(activity)
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme)
            val window = activity.window
            //设置状态栏透明
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.navigationBarColor = ContextCompat.getColor(activity, R.color.nightThemeColorPrimary)
        }
    }

    /**
     * 简单设置主题
     *
     * @param activity [AppCompatActivity]
     */
    fun simpleSetTheme(@NonNull activity: AppCompatActivity) {
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme_Setting)
        }
    }

    /**
     * 初始化默认颜色
     *
     * @param activity [AppCompatActivity]
     */
    private fun initDefaultColor(@NonNull activity: AppCompatActivity) {
        if (defaultColorPrimary != 0) {
            return
        }
        defaultColorPrimary = ContextCompat.getColor(activity, R.color.colorPrimary)
        defaultColorPrimaryDark = ContextCompat.getColor(activity, R.color.colorPrimaryDark)
        defaultColorAccent = ContextCompat.getColor(activity, R.color.colorAccent)
        defaultColorProgress = ContextCompat.getColor(activity, R.color.color_progress)
    }

    /**
     * 更新设置页面主题
     *
     * @param activity [AppCompatActivity]
     */
    fun changeTheme(@NonNull activity: AppCompatActivity) {
        initDefaultColor(activity)
        if (isChange) {
            activity.setTheme(R.style.AppTheme_NightTheme_Setting)
            return
        }
        val actionBar = activity.supportActionBar
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        if (actionBar != null) {
            val appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary)
            actionBar.setBackgroundDrawable(ColorDrawable(appColorPrimary))
        }
        val appColorPrimaryDark = sharedPreferences.getInt(APP_COLOR_PRIMARY_DARK, defaultColorPrimaryDark)
        StatusBarUtil.setColor(activity, appColorPrimaryDark, 30)
    }

    /**
     * 设置TabLayout颜色
     *
     * @param context   [Context]
     * @param tabLayout [TabLayout]
     */
    fun setTabLayoutColor(@NonNull context: Context, @NonNull tabLayout: TabLayout) {
        val colorNormal = ContextCompat.getColor(context, android.R.color.white)
        if (isChange) {
            val colorAccent = ContextCompat.getColor(context, R.color.nightThemeColorAccent)
            tabLayout.setBackgroundResource(R.color.nightThemeColorPrimary)
            tabLayout.setSelectedTabIndicatorColor(colorAccent)
            tabLayout.setTabTextColors(colorNormal, colorAccent)
            return
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary)
        val appColorAccent = sharedPreferences.getInt(APP_COLOR_ACCENT, defaultColorAccent)
        tabLayout.setBackgroundColor(appColorPrimary)
        tabLayout.setSelectedTabIndicatorColor(appColorAccent)
        tabLayout.setTabTextColors(colorNormal, appColorAccent)
    }

    /**
     * 设置背景Resource
     *
     * @param context [Context]
     * @param views   [View]
     */
    fun setBackgroundResources(@NonNull context: Context, @NonNull vararg views: View) {
        if (isChange) {
            for (v in views) {
                v.setBackgroundResource(R.color.nightThemeColorPrimary)
            }
            return
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val appColorPrimary = sharedPreferences.getInt(APP_COLOR_PRIMARY, defaultColorPrimary)
        for (v in views) {
            if (v.id == R.id.view_center || v.id == R.id.view_top || v.id == R.id.view_bottom) {
                continue
            }
            v.setBackgroundColor(appColorPrimary)
        }
    }

    /**
     * 设置进度条颜色
     *
     * @param context [Context]
     * @param view    [View]
     */
    fun setProgressBackgroundResource(@NonNull context: Context, @NonNull view: View) {
        if (isChange) {
            view.setBackgroundResource(R.color.color_progress_night)
            return
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val appColorProgress = sharedPreferences.getInt(APP_COLOR_PROGRESS, defaultColorProgress)
        view.setBackgroundColor(appColorProgress)
    }

    /**
     * 获取当前强调色颜色
     *
     * @param context [Context]
     * @return 颜色数值
     */
    @CheckResult
    @ColorInt
    fun getNowThemeColorAccent(@NonNull context: Context): Int {
        return if (isChange) {
            ContextCompat.getColor(context, R.color.nightThemeColorAccent)
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).getInt(APP_COLOR_ACCENT, defaultColorAccent)
        }
    }

    /**
     * 设置TextView 颜色
     *
     * @param context   [Context]
     * @param textViews [TextView]
     */
    fun setTextViewsColorByTheme(@NonNull context: Context, @NonNull vararg textViews: TextView) {
        val isChange = isChange
        val whiteColor = ContextCompat.getColor(context, android.R.color.white)
        val blackColor = ContextCompat.getColor(context, android.R.color.black)
        for (textView in textViews) {
            if (isChange) {
                textView.setTextColor(whiteColor)
            } else {
                textView.setTextColor(blackColor)
            }
        }
    }
}
