package top.itning.yunshuclassschedule.ui.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.android.synthetic.main.activity_share.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.BaseActivity
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.DataEntity
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.util.DateUtils
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 课程表分享活动
 *
 * @author itning
 */
class ShareActivity : BaseActivity() {
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        ThemeChangeUtil.simpleSetTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        EventBus.getDefault().register(this)
        initView()

        tv_import_file.setOnClickListener { importFile() }
        tv_export_file.setOnClickListener { exportFile() }
        tv_export_share.setOnClickListener { shareFile() }
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.title = "分享课程表"
        }
        val nowThemeColorAccent = ThemeChangeUtil.getNowThemeColorAccent(this)
        tv_import_title.setTextColor(nowThemeColorAccent)
        tv_export_title.setTextColor(nowThemeColorAccent)
        ThemeChangeUtil.setTextViewsColorByTheme(this, tv_import_file, tv_export_file, tv_export_share)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(eventEntity: EventEntity) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 分享课程数据文件
     */
    private fun shareFile() {
        val dataEntity = DataEntity(application as App)
        val gson = Gson()
        val bytes = gson.toJson(dataEntity).toByteArray()
        val fileName = cacheDir.toString() + File.separator + "云舒课表课程数据.json"
        try {
            FileOutputStream(fileName).use { fileOutputStream ->
                fileOutputStream.write(bytes, 0, bytes.size)
                fileOutputStream.flush()
            }
        } catch (e: IOException) {
            Log.e(TAG, " ", e)
            Toast.makeText(this, "生成数据失败", Toast.LENGTH_SHORT).show()
            CrashReport.postCatchedException(e)
        }

        val uri = FileProvider.getUriForFile(this, "top.itning.yunshuclassschedule.fileProvider", File(fileName))
        val share = Intent(Intent.ACTION_SEND)
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.type = "application/octet-stream"
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(share, "分享课程数据文件"))
    }

    /**
     * 导出文件
     */
    private fun exportFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val fileName = "云舒课表课程数据" + SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINESE).format(Date()) + ".json"
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        try {
            startActivityForResult(intent, WRITE_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "没有找到文件管理APP", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * 导入文件
     */
    private fun importFile() {
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
        when (requestCode) {
            FILE_SELECT_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    doImportFile(data!!)
                }
            }
            WRITE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    doExportFile(data!!)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 导出文件
     *
     * @param data [Intent]
     */
    private fun doExportFile(data: Intent) {
        val uri = data.data ?: kotlin.run {
            Toast.makeText(this, "导出失败", Toast.LENGTH_LONG).show()
            return
        }
        Log.d(TAG, "File Uri: $uri")
        try {
            BufferedOutputStream(Objects.requireNonNull<OutputStream>(contentResolver.openOutputStream(uri))).use { bufferedOutputStream ->
                val dataEntity = DataEntity(application as App)
                val bytes = Gson().toJson(dataEntity).toByteArray()
                bufferedOutputStream.write(bytes, 0, bytes.size)
                bufferedOutputStream.flush()
                Toast.makeText(this, "导出成功", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, " ", e)
            CrashReport.postCatchedException(e)
            Toast.makeText(this, "导出失败", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * 导入文件
     *
     * @param data [Intent]
     */
    private fun doImportFile(data: Intent) {
        val uri = data.data ?: kotlin.run {
            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show()
            return
        }
        Log.d(TAG, "File Uri: $uri")
        try {
            val openInputStream = contentResolver.openInputStream(uri) ?: kotlin.run {
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
                            Toast.makeText(this, "导入成功", Toast.LENGTH_LONG).show()
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

    companion object {
        private const val TAG = "ShareActivity"
        const val FILE_SELECT_CODE = 1
        private const val WRITE_REQUEST_CODE = 2
    }
}
