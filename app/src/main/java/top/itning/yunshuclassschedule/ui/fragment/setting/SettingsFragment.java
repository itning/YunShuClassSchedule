package top.itning.yunshuclassschedule.ui.fragment.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.Objects;

import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 设置Fragment
 *
 * @author itning
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    public static final String CLASS_REMINDER_UP_TIME = "class_reminder_up_time";
    public static final String CLASS_REMINDER_DOWN_TIME = "class_reminder_down_time";
    public static final String PHONE_MUTE_BEFORE_TIME = "phone_mute_before_time";
    public static final String PHONE_MUTE_AFTER_TIME = "phone_mute_after_time";
    public static final String DEFAULT_SHOW_MAIN_FRAGMENT = "default_show_main_fragment";
    public static final String APP_COLOR_PRIMARY = "app_color_primary";
    public static final String APP_COLOR_PRIMARY_DARK = "app_color_primary_dark";
    public static final String APP_COLOR_ACCENT = "app_color_accent";
    public static final String APP_COLOR_PROGRESS = "app_color_progress";
    private static final String CLASS_SCHEDULE_UPDATE_FREQUENCY = "class_schedule_update_frequency";

    private SharedPreferences prefs;
    private ListPreference defaultShowMainFragmentListPreference;
    private ListPreference classScheduleUpdateFrequency;
    private ListPreference classReminderUpTime;
    private ListPreference classReminderDownTime;
    private ListPreference phoneMuteBeforeTime;
    private ListPreference phoneMuteAfterTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        prefs.registerOnSharedPreferenceChangeListener(this);
        Bundle bundle = getArguments();
        if (bundle == null) {
            defaultShowMainFragmentListPreference = (ListPreference) findPreference(DEFAULT_SHOW_MAIN_FRAGMENT);
            defaultShowMainFragmentListPreference.setSummary(defaultShowMainFragmentListPreference.getEntry());
            classScheduleUpdateFrequency = (ListPreference) findPreference(CLASS_SCHEDULE_UPDATE_FREQUENCY);
            classScheduleUpdateFrequency.setSummary(classScheduleUpdateFrequency.getEntry());
        } else {
            String key = bundle.getString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT);
            assert key != null;
            switch (key) {
                case "class_reminder": {
                    classReminderUpTime = (ListPreference) findPreference(CLASS_REMINDER_UP_TIME);
                    classReminderDownTime = (ListPreference) findPreference(CLASS_REMINDER_DOWN_TIME);
                    classReminderUpTime.setSummary(classReminderUpTime.getEntry());
                    classReminderDownTime.setSummary(classReminderDownTime.getEntry());
                    break;
                }
                case "phone_mute": {
                    phoneMuteBeforeTime = (ListPreference) findPreference(PHONE_MUTE_BEFORE_TIME);
                    phoneMuteAfterTime = (ListPreference) findPreference(PHONE_MUTE_AFTER_TIME);
                    phoneMuteBeforeTime.setSummary(phoneMuteBeforeTime.getEntry());
                    phoneMuteAfterTime.setSummary(phoneMuteAfterTime.getEntry());
                    break;
                }
                default:
            }
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "on Destroy View");
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            setPreferencesFromResource(R.xml.preference_settings, rootKey);
        } else {
            setPreferencesFromResource(R.xml.preference_settings, bundle.getString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case DEFAULT_SHOW_MAIN_FRAGMENT: {
                defaultShowMainFragmentListPreference.setSummary(defaultShowMainFragmentListPreference.getEntry());
                break;
            }
            case CLASS_SCHEDULE_UPDATE_FREQUENCY: {
                classScheduleUpdateFrequency.setSummary(classScheduleUpdateFrequency.getEntry());
                break;
            }
            case CLASS_REMINDER_UP_TIME: {
                if (classReminderUpTime != null) {
                    classReminderUpTime.setSummary(classReminderUpTime.getEntry());
                }
                break;
            }
            case CLASS_REMINDER_DOWN_TIME: {
                if (classReminderDownTime != null) {
                    classReminderDownTime.setSummary(classReminderDownTime.getEntry());
                }
                break;
            }
            case PHONE_MUTE_BEFORE_TIME: {
                if (phoneMuteBeforeTime != null) {
                    phoneMuteBeforeTime.setSummary(phoneMuteBeforeTime.getEntry());
                }
                break;
            }
            case PHONE_MUTE_AFTER_TIME: {
                if (phoneMuteAfterTime != null) {
                    phoneMuteAfterTime.setSummary(phoneMuteAfterTime.getEntry());
                }
                break;
            }
            case APP_COLOR_PRIMARY: {
                ThemeChangeUtil.changeColor();
                break;
            }
            case APP_COLOR_PRIMARY_DARK: {
                ThemeChangeUtil.changeColor();
                break;
            }
            case APP_COLOR_ACCENT: {
                ThemeChangeUtil.changeColor();
                break;
            }
            case APP_COLOR_PROGRESS: {
                ThemeChangeUtil.changeColor();
                break;
            }
            default:
        }
    }
}
