package top.itning.yunshuclassschedule.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import top.itning.yunshuclassschedule.entity.EventEntity;

/**
 * 手机自动静音服务
 *
 * @author itning
 */
public class PhoneMuteService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "PhoneMuteService";
    public static final String PHONE_MUTE_STATUS = "phone_mute_status";

    private SharedPreferences defaultSharedPreferences;
    private boolean phoneMuteStatus;
    private int currentVolume;
    private int index;
    private Runnable runnable = () -> changeVolume(index);
    private Handler handler = new Handler(Looper.getMainLooper());
    private AudioManager audioManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "on Create");
        EventBus.getDefault().register(this);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        start();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Phone Mute Service On Destroy");
        EventBus.getDefault().unregister(this);
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case CLASS_UP_TIME_CHANGE: {
                timeUpChange(Integer.parseInt(eventEntity.getMsg()));
                break;
            }
            case CLASS_DOWN_TIME_CHANGE: {
                timeDownChange(Integer.parseInt(eventEntity.getMsg()));
                break;
            }
            default:
        }
    }

    /**
     * 开始
     */
    private void start() {
        Log.d(TAG, "run thread name:" + Thread.currentThread().getName());
        phoneMuteStatus = defaultSharedPreferences.getBoolean(PHONE_MUTE_STATUS, true);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 离下课时间
     *
     * @param i 分钟
     */
    private void timeDownChange(int i) {
        Log.d(TAG, "Leaving class down have " + i + " minute");
        Log.d(TAG, "currentVolume:" + currentVolume);
        if (currentVolume == 0) {
            return;
        }
        if (phoneMuteStatus) {
            int phoneMuteBeforeTime = Integer.parseInt(defaultSharedPreferences.getString("phone_mute_before_time", "0"));
            if (phoneMuteBeforeTime == 0 && i == 1) {
                handler.removeCallbacks(runnable);
                index = currentVolume;
                handler.postDelayed(runnable, 60000);
                Log.d(TAG, "post delayed 1 minute turn volume to " + index);
                return;
            }
            if (phoneMuteBeforeTime == i) {
                Log.d(TAG, "turn volume to " + currentVolume);
                changeVolume(currentVolume);
            }
        }
    }

    /**
     * 离上课时间
     *
     * @param i 分钟
     */
    private void timeUpChange(int i) {
        Log.d(TAG, "Leaving class up have " + i + " minute");
        if (phoneMuteStatus) {
            int phoneMuteAfterTime = Integer.parseInt(defaultSharedPreferences.getString("phone_mute_after_time", "0"));
            //当前音量
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            Log.d(TAG, "get current volume:" + currentVolume);
            if (phoneMuteAfterTime == 0 && i == 1) {
                handler.removeCallbacks(runnable);
                index = 0;
                handler.postDelayed(runnable, 60000);
                Log.d(TAG, "post delayed 1 minute turn volume to 0");
                return;
            }
            if (phoneMuteAfterTime == i) {
                Log.d(TAG, "turn volume to 0");
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_SHOW_UI);
            }
        }
    }

    /**
     * 更改音量
     *
     * @param index 大小
     */
    private void changeVolume(int index) {
        audioManager.setStreamVolume(AudioManager.STREAM_RING, index, AudioManager.FLAG_SHOW_UI);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PHONE_MUTE_STATUS.equals(key)) {
            if (sharedPreferences.getBoolean(key, false)) {
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                assert notificationManager != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted()) {
                    Toast.makeText(this, "请授予免打扰权限", Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "权限授予后请重新开启自动静音", Toast.LENGTH_LONG).show();
                    if (sharedPreferences.edit().putBoolean(key, false).commit()) {
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                    }
                }
            }
            phoneMuteStatus = sharedPreferences.getBoolean(key, true);
        }
    }
}
