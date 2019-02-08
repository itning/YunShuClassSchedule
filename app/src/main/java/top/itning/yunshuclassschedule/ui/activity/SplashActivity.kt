package top.itning.yunshuclassschedule.ui.activity

import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_splash.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.BaseActivity
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.service.CourseInfoService
import top.itning.yunshuclassschedule.service.JobSchedulerService
import top.itning.yunshuclassschedule.util.GlideApp

/**
 * 闪屏页
 *
 * @author itning
 */
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            finish()
            return
        }
        setContentView(R.layout.activity_splash)
        EventBus.getDefault().register(this)
        initBackGroundImage()

        startService(Intent(this, CourseInfoService::class.java))

        initJobScheduler()
        startTime = System.currentTimeMillis()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //勿扰权限判定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("phone_mute_status", false).apply()
        }
        Handler().postDelayed({ this.enterMainActivity() }, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get() - (System.currentTimeMillis() - startTime))
    }

    /**
     * 初始化背景图片
     */
    private fun initBackGroundImage() {
        val display = this.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        GlideApp
                .with(this)
                .load(R.drawable.splash_background)
                .override(size.x, size.y)
                .centerCrop()
                .into(iv_splash)
    }

    /**
     * init Job Scheduler
     */
    private fun initJobScheduler() {
        Log.d(TAG, "init Job Scheduler")
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancelAll()
        val jobInfo = JobInfo.Builder(1024, ComponentName(packageName, JobSchedulerService::class.java.name))
                //10 minutes
                .setPeriodic((10 * 60 * 1000).toLong())
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .build()
        val schedule = jobScheduler.schedule(jobInfo)
        if (schedule <= 0) {
            Log.e(TAG, "schedule error！")
        }
    }

    /**
     * 进入主Activity
     */
    private fun enterMainActivity() {
        val intent: Intent = if (App.sharedPreferences.getBoolean(ConstantPool.Str.FIRST_IN_APP.get(), true)) {
            //第一次进入APP
            Intent(this, LoginActivity::class.java)
        } else {
            //非第一次,肯定已经登陆
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.ENTER_HOME_ACTIVITY -> {
                Handler().postDelayed({ this.enterMainActivity() }, ConstantPool.Int.DELAY_INTO_MAIN_ACTIVITY_TIME.get() - (System.currentTimeMillis() - startTime))
            }
            else -> {
            }
        }
    }

    override fun onBackPressed() {
        //do nothing
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SplashActivity"
        private var startTime: Long = 0
    }
}
