package top.itning.yunshuclassschedule.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

/**
 * @author itning
 */
public class JobSchedulerService extends JobService {
    private static final String TAG = "JobSchedulerService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob(): params = [" + params + "]");
        startService(new Intent(this, CommonService.class));
        startService(new Intent(this, RemindService.class));
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob(): params = [" + params + "]");
        return false;
    }
}
