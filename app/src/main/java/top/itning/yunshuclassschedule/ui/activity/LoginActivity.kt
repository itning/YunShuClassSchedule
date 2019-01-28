package top.itning.yunshuclassschedule.ui.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.jaeger.library.StatusBarUtil
import com.tencent.bugly.crashreport.CrashReport
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
import top.itning.yunshuclassschedule.ui.activity.ShareActivity.Companion.TIME_LIST_SIZE
import top.itning.yunshuclassschedule.util.DateUtils
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
        if ("" == App.sharedPreferences.getString(FIRST_CLASS, "")) {
            App.sharedPreferences.edit()
                    .putString("1", "08:20-09:50")
                    .putString("2", "10:05-11:35")
                    .putString("3", "12:55-14:25")
                    .putString("4", "14:40-16:10")
                    .putString("5", "17:30-20:00")
                    .apply()
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
            val uri = data.data ?: kotlin.run {
                Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                return
            }
            Log.d(TAG, "File Uri: " + uri.toString())
            val openInputStream = contentResolver.openInputStream(uri) ?: kotlin.run {
                Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                return
            }
            val inputAsString = openInputStream.bufferedReader().use { it.readText() }
            val dataEntity = Gson().fromJson(inputAsString, DataEntity::class.java)
            val classScheduleList = dataEntity.classScheduleList
            val timeList = dataEntity.timeList
            if (classScheduleList == null || classScheduleList.isEmpty() || timeList == null || timeList.isEmpty() || timeList.size != TIME_LIST_SIZE) {
                Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                return
            }
            AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("即将导入课程数据，这会将原有课程信息清空，确定导入吗？")
                    .setPositiveButton("确定") { _, _ ->
                        val timeMap = TreeMap<String, String>()
                        timeMap["1"] = timeList[0]
                        timeMap["2"] = timeList[1]
                        timeMap["3"] = timeList[2]
                        timeMap["4"] = timeList[3]
                        timeMap["5"] = timeList[4]
                        if (!DateUtils.isDataLegitimate(timeMap, this)) {
                            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
                            return@setPositiveButton
                        }
                        if (App.sharedPreferences.edit()
                                        .putString("1", timeList[0])
                                        .putString("2", timeList[1])
                                        .putString("3", timeList[2])
                                        .putString("4", timeList[3])
                                        .putString("5", timeList[4])
                                        .commit()) {
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
                            Toast.makeText(this, "导入成功", Toast.LENGTH_LONG).show()
                            enterMainActivity()
                        } else {
                            Toast.makeText(this, "写入数据库失败,请重试", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
        } catch (e: Exception) {
            Log.e(TAG, " ", e)
            CrashReport.postCatchedException(e)
            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
        }
    }

    private fun enterMainActivity() {
        App.sharedPreferences.edit()
                .putString(ConstantPool.Str.APP_CLASS_SCHEDULE_VERSION.get(), "")
                .putString(ConstantPool.Str.USER_USERNAME.get(), "test")
                .putString(ConstantPool.Str.USER_CLASS_ID.get(), "-1")
                .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                .apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val FIRST_CLASS = "1"
    }
}
