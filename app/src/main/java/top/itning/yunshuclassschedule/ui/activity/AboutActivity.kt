package top.itning.yunshuclassschedule.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.CheckResult
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.BaseActivity
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.util.ThemeChangeUtil

/**
 * 关于
 *
 * @author itning
 */
class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeChangeUtil.changeTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        EventBus.getDefault().register(this)
        initView()
    }

    private fun initView() {
        Log.d(TAG, "init view")
        //设置返回箭头
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.title = "关于"
        }
        tv_version.text = getPackageVersionName(this)
        cv_href.setOnClickListener { onCvHrefClicked() }
        cv_introduction.setOnClickListener { onIntroductionClicked() }
    }

    /**
     * 获取当前应用版本
     *
     * @param appCompatActivity [AppCompatActivity]
     * @return 版本信息
     */
    @CheckResult
    private fun getPackageVersionName(@NonNull appCompatActivity: AppCompatActivity): String {
        return try {
            appCompatActivity.packageManager.getPackageInfo(appCompatActivity.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("SplashActivity", "Package name not found:", e)
            ""
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(eventEntity: EventEntity) {

    }

    private fun onCvHrefClicked() {
        val uri = Uri.parse("https://github.com/itning/YunShuClassSchedule")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun onIntroductionClicked() {
        startActivity(Intent(this, MoneyActivity::class.java))
    }

    companion object {
        private const val TAG = "AboutActivity"
    }
}
