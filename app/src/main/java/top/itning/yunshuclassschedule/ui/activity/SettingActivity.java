package top.itning.yunshuclassschedule.ui.activity;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 设置Activity
 *
 * @author itning
 */
public class SettingActivity extends BaseActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    private static final String TAG = "SettingActivity";
    private FragmentManager supportFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChangeUtil.changeSettingTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        Log.d(TAG, "init view");
        //设置返回箭头
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("设置");
        }
        SettingsFragment settingsFragment = new SettingsFragment();
        supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, settingsFragment)
                .commit();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ignoreBatteryOptimization(this);
        }
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case APP_COLOR_CHANGE: {
                ThemeChangeUtil.changeSettingTheme(this);
                break;
            }
            case DESTROY_ACTIVITY: {
                finish();
                break;
            }
            default:
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        String key = pref.getKey();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, key);
        fragment.setArguments(args);
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(key)
                .commit();
        return true;
    }

    /**
     * 忽略电池优化
     */

    @TargetApi(Build.VERSION_CODES.M)
    private void ignoreBatteryOptimization(Activity activity) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        assert powerManager != null;
        boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            new AlertDialog.Builder(this).setTitle("更好的上下课通知")
                    .setMessage("为了确保准确的上下课静音和通知,最好让我能在后台运行")
                    .setCancelable(true)
                    .setPositiveButton("可以",
                            (dialog, which) -> {
                                @SuppressLint("BatteryLife")
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                                ComponentName componentName = intent.resolveActivity(getPackageManager());
                                if (componentName != null) {
                                    try {
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(this, "居然没有这个功能....", Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                    .setNegativeButton("算了", null)
                    .show();
        }
    }
}
