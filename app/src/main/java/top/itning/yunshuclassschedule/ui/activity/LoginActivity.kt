package top.itning.yunshuclassschedule.ui.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_login.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.BaseActivity
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.DataEntity
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.activity.ShareActivity.Companion.FILE_SELECT_CODE
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
import top.itning.yunshuclassschedule.util.DateUtils
import top.itning.yunshuclassschedule.util.DateUtils.getNextMondayOfTimeInMillis
import java.util.*

/**
 * 登陆
 *
 * @author itning
 */
class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        EventBus.getDefault().register(this)
        StatusBarUtil.setTransparent(this)
        initData()
        btn_custom.setOnClickListener { onCustomBtnClicked() }
        btn_share.setOnClickListener { onViewClicked() }
    }

    /**
     * 加载专业数据
     */
    private fun initData() {
        if (App.sharedPreferences.getBoolean(ConstantPool.Str.FIRST_IN_APP.get(), true)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(SettingsFragment.NOW_WEEK_NUM, "1")
                    .apply()
            App.sharedPreferences.edit()
                    .putLong(ConstantPool.Str.NEXT_WEEK_OF_MONDAY.get(), getNextMondayOfTimeInMillis())
                    .putString("1", "08:20-09:50")
                    .putString("2", "10:05-11:35")
                    .putString("3", "12:55-14:25")
                    .putString("4", "14:40-16:10")
                    .putString("5", "17:30-20:00")
                    .putString("6", "20:05-20:08")
                    .putString("7", "20:10-20:15")
                    .putString("8", "20:18-20:20")
                    .putString("9", "20:30-20:35")
                    .putString("10", "20:40-20:45")
                    .putString("11", "20:50-21:01")
                    .putString("12", "21:10-21:15").apply()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(eventEntity: EventEntity) {
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    /**
     * 消息事件
     *
     * @param what what
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(what: Int?) {
        Log.d(TAG, what!!.toString() + "")
    }

    /**
     * 自定义课程
     */
    private fun onCustomBtnClicked() {
        startActivity(Intent(this, CustomActivity::class.java))
        finish()
    }

    private fun onViewClicked() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, "选择课程数据文件进行导入"), FILE_SELECT_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "没有找到文件管理APP", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            doImportFile(data!!)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun doImportFile(data: Intent) {
        try {
            val uri = data.data ?: run {
                Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                return
            }
            Log.d(TAG, "File Uri: $uri")
            val openInputStream = contentResolver.openInputStream(uri) ?: run {
                Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                return
            }
            val inputAsString = openInputStream.bufferedReader().use { it.readText() }
            val dataEntity = Gson().fromJson(inputAsString, DataEntity::class.java)
            val classScheduleList = dataEntity.classScheduleList
            val timeList = dataEntity.timeList
            if (classScheduleList == null || classScheduleList.isEmpty() || timeList == null || timeList.isEmpty()) {
                Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                return
            }
            val timeListSize = timeList.size
            val classScheduleMaxSection = classScheduleList.map { it.section }.max() ?: 12
            if (timeListSize > 12 || classScheduleMaxSection > 12) {
                Toast.makeText(this, "最大课程数为12节课，解析失败", Toast.LENGTH_LONG).show()
                return
            }
            Log.d(TAG, "classScheduleMaxSection: $classScheduleMaxSection timeListSize: $timeListSize")
            val section = if (timeListSize < classScheduleMaxSection) {
                classScheduleMaxSection
            } else {
                timeListSize
            }
            AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("即将导入课程数据，这会将原有课程信息清空，确定导入吗？")
                    .setPositiveButton("确定") { _, _ ->
                        val timeMap = TreeMap<Int, String>()
                        for ((index, value) in timeList.withIndex()) {
                            timeMap[index + 1] = value
                        }
                        if (!DateUtils.isDataLegitimate(timeMap, this)) {
                            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                            return@setPositiveButton
                        }
                        val edit = App.sharedPreferences.edit()
                        for ((index, value) in timeList.withIndex()) {
                            edit.putString((index + 1).toString(), value)
                        }
                        if (edit.commit()) {
                            DateUtils.refreshTimeList()
                        } else {
                            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                            return@setPositiveButton
                        }
                        val classScheduleDao = (application as App).daoSession.classScheduleDao
                        classScheduleDao.deleteAll()
                        classScheduleList.forEach { classScheduleDao.insert(it) }
                        EventBus.getDefault().post(EventEntity(ConstantPool.Int.TIME_TICK_CHANGE, ""))
                        if (classScheduleList.size.toLong() == classScheduleDao.count()) {
                            if (App.sharedPreferences.edit()
                                            .putInt(ConstantPool.Str.CLASS_SECTION.get(), section)
                                            .commit()) {
                                DateUtils.refreshTimeList()
                                Toast.makeText(this, "导入成功", Toast.LENGTH_LONG).show()
                                enterMainActivity()
                            } else {
                                Toast.makeText(this, "写入课程节数失败,请重试", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "写入数据库失败,请重试", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
        } catch (e: Exception) {
            Log.e(TAG, " ", e)
            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
        }
    }

    private fun enterMainActivity() {
        App.sharedPreferences.edit()
                .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                .apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
