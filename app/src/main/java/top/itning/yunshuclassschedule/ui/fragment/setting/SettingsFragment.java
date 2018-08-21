package top.itning.yunshuclassschedule.ui.fragment.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import java.util.Objects;

import top.itning.yunshuclassschedule.R;

/**
 * 设置Fragment
 *
 * @author itning
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    private static final String DEFAULT_SHOW_MAIN_FRAGMENT = "default_show_main_fragment";
    private static final String CLASS_SCHEDULE_UPDATE_FREQUENCY = "class_schedule_update_frequency";
    private ListPreference defaultShowMainFragmentListPreference;
    private ListPreference classScheduleUpdateFrequency;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        prefs.registerOnSharedPreferenceChangeListener(this);
        if (getArguments() == null) {
            defaultShowMainFragmentListPreference = (ListPreference) findPreference(DEFAULT_SHOW_MAIN_FRAGMENT);
            defaultShowMainFragmentListPreference.setSummary(defaultShowMainFragmentListPreference.getEntry());
            classScheduleUpdateFrequency = (ListPreference) findPreference(CLASS_SCHEDULE_UPDATE_FREQUENCY);
            classScheduleUpdateFrequency.setSummary(classScheduleUpdateFrequency.getEntry());
        }
    }

    @Override
    public void onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
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
        if (DEFAULT_SHOW_MAIN_FRAGMENT.equals(key)) {
            defaultShowMainFragmentListPreference.setSummary(defaultShowMainFragmentListPreference.getEntry());
        }
        if (CLASS_SCHEDULE_UPDATE_FREQUENCY.equals(key)) {
            classScheduleUpdateFrequency.setSummary(classScheduleUpdateFrequency.getEntry());
        }
    }
}
