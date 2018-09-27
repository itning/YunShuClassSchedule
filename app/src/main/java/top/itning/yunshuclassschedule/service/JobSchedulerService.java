package top.itning.yunshuclassschedule.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.FOREGROUND_SERVICE_STATUS;

/**
 * @author itning
 */
public class JobSchedulerService extends JobService {
    private static final String TAG = "JobSchedulerService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob(): params = [" + params + "]");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FOREGROUND_SERVICE_STATUS, true)) {
                startForegroundService(new Intent(this, CommonService.class));
                startForegroundService(new Intent(this, RemindService.class));
            }
        } else {
            startService(new Intent(this, CommonService.class));
            startService(new Intent(this, RemindService.class));
        }
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob(): params = [" + params + "]");
        return false;
    }
}
