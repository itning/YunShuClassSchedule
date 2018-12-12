package top.itning.yunshuclassschedule.ui.fragment.setting

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.util.*

/**
 * 设置Fragment
 *
 * @author itning
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var prefs: SharedPreferences? = null
    private var defaultShowMainFragmentListPreference: ListPreference? = null
    private var classReminderUpTime: ListPreference? = null
    private var classReminderDownTime: ListPreference? = null
    private var phoneMuteBeforeTime: ListPreference? = null
    private var phoneMuteAfterTime: ListPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull<Context>(context))
        prefs!!.registerOnSharedPreferenceChangeListener(this)
        val bundle = arguments
        if (bundle == null) {
            defaultShowMainFragmentListPreference = findPreference(DEFAULT_SHOW_MAIN_FRAGMENT) as ListPreference
            defaultShowMainFragmentListPreference!!.summary = defaultShowMainFragmentListPreference!!.entry
            val foregroundServiceStatus = findPreference(FOREGROUND_SERVICE_STATUS)
            foregroundServiceStatus.setOnPreferenceChangeListener { _, newValue ->
                if (!(newValue as Boolean)) {
                    AlertDialog.Builder(requireContext()).setTitle("注意")
                            .setMessage("关闭后台常驻会导致提醒服务，手机自动静音服务不准确。建议您不要关闭！")
                            .setCancelable(true)
                            .setPositiveButton("我知道了", null)
                            .show()
                }
                true
            }
        } else {
            val key = bundle.getString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT)!!
            when (key) {
                "class_reminder" -> {
                    classReminderUpTime = findPreference(CLASS_REMINDER_UP_TIME) as ListPreference
                    classReminderDownTime = findPreference(CLASS_REMINDER_DOWN_TIME) as ListPreference
                    classReminderUpTime!!.summary = classReminderUpTime!!.entry
                    classReminderDownTime!!.summary = classReminderDownTime!!.entry
                }
                "phone_mute" -> {
                    phoneMuteBeforeTime = findPreference(PHONE_MUTE_BEFORE_TIME) as ListPreference
                    phoneMuteAfterTime = findPreference(PHONE_MUTE_AFTER_TIME) as ListPreference
                    val phoneMuteStatus = findPreference(PHONE_MUTE_STATUS)
                    phoneMuteStatus.setOnPreferenceChangeListener { _, newValue ->
                        if (newValue as Boolean) {
                            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted) {
                                Toast.makeText(requireContext(), "请授予免打扰权限", Toast.LENGTH_LONG).show()
                                Toast.makeText(requireContext(), "权限授予后请重新开启自动静音", Toast.LENGTH_LONG).show()
                                startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                                return@setOnPreferenceChangeListener false
                            } else {
                                return@setOnPreferenceChangeListener true
                            }
                        }
                        true
                    }
                    phoneMuteBeforeTime!!.summary = phoneMuteBeforeTime!!.entry
                    phoneMuteAfterTime!!.summary = phoneMuteAfterTime!!.entry
                }
            }
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "on Destroy View")
        prefs!!.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        val bundle = arguments
        if (bundle == null) {
            setPreferencesFromResource(R.xml.preference_settings, rootKey)
        } else {
            setPreferencesFromResource(R.xml.preference_settings, bundle.getString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT))
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            DEFAULT_SHOW_MAIN_FRAGMENT -> {
                defaultShowMainFragmentListPreference!!.summary = defaultShowMainFragmentListPreference!!.entry
            }
            CLASS_REMINDER_UP_TIME -> {
                if (classReminderUpTime != null) {
                    classReminderUpTime!!.summary = classReminderUpTime!!.entry
                }
            }
            CLASS_REMINDER_DOWN_TIME -> {
                if (classReminderDownTime != null) {
                    classReminderDownTime!!.summary = classReminderDownTime!!.entry
                }
            }
            PHONE_MUTE_BEFORE_TIME -> {
                if (phoneMuteBeforeTime != null) {
                    phoneMuteBeforeTime!!.summary = phoneMuteBeforeTime!!.entry
                }
            }
            PHONE_MUTE_AFTER_TIME -> {
                if (phoneMuteAfterTime != null) {
                    phoneMuteAfterTime!!.summary = phoneMuteAfterTime!!.entry
                }
            }
            APP_COLOR_PRIMARY -> {
                ThemeChangeUtil.changeColor()
            }
            APP_COLOR_PRIMARY_DARK -> {
                ThemeChangeUtil.changeColor()
            }
            APP_COLOR_ACCENT -> {
                ThemeChangeUtil.changeColor()
            }
            APP_COLOR_PROGRESS -> {
                ThemeChangeUtil.changeColor()
            }
        }
    }

    companion object {
        private const val TAG = "SettingsFragment"
        const val CLASS_REMINDER_UP_TIME = "class_reminder_up_time"
        const val CLASS_REMINDER_DOWN_TIME = "class_reminder_down_time"
        const val PHONE_MUTE_STATUS = "phone_mute_status"
        const val PHONE_MUTE_BEFORE_TIME = "phone_mute_before_time"
        const val PHONE_MUTE_AFTER_TIME = "phone_mute_after_time"
        const val DEFAULT_SHOW_MAIN_FRAGMENT = "default_show_main_fragment"
        const val APP_COLOR_PRIMARY = "app_color_primary"
        const val APP_COLOR_PRIMARY_DARK = "app_color_primary_dark"
        const val APP_COLOR_ACCENT = "app_color_accent"
        const val APP_COLOR_PROGRESS = "app_color_progress"
        const val FOREGROUND_SERVICE_STATUS = "foreground_service_status"
    }
}
