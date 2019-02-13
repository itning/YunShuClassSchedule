package top.itning.yunshuclassschedule.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.FOREGROUND_SERVICE_STATUS

/**
 * @author itning
 */
class JobSchedulerService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "onStartJob(): params = [$params]")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
                startForegroundService(Intent(this, CommonService::class.java))
                startForegroundService(Intent(this, RemindService::class.java))
                startForegroundService(Intent(this, TodayWidgetService::class.java))
            }
        } else {
            startService(Intent(this, CommonService::class.java))
            startService(Intent(this, RemindService::class.java))
            startService(Intent(this, TodayWidgetService::class.java))
        }
        jobFinished(params, false)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG, "onStopJob(): params = [$params]")
        return false
    }

    companion object {
        private const val TAG = "JobSchedulerService"
    }
}
