package top.itning.yunshuclassschedule.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import androidx.annotation.CheckResult
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.activity_custom.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.BaseActivity
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.util.DateUtils
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.util.*

/**
 * 自定义课程
 *
 * @author itning
 */
class CustomActivity : BaseActivity(), TimePickerDialog.OnTimeSetListener {

    private lateinit var msg: String
    private var classSchedule = 5
    private var timeMap: TreeMap<String, String> = TreeMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeChangeUtil.changeTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)
        EventBus.getDefault().register(this)
        init()
    }

    /**
     * 初始化数据
     */
    @SuppressLint("ApplySharedPref")
    private fun initData() {
        if (App.sharedPreferences.getBoolean(ConstantPool.Str.FIRST_IN_APP.get(), true)) {
            App.sharedPreferences.edit().putString("1", "08:20-09:50").commit()
            App.sharedPreferences.edit().putString("2", "10:05-11:35").commit()
            App.sharedPreferences.edit().putString("3", "12:55-14:25").commit()
            App.sharedPreferences.edit().putString("4", "14:40-16:10").commit()
            App.sharedPreferences.edit().putString("5", "17:30-20:00").commit()
            App.sharedPreferences.edit().putString("6", "12:00-12:01").commit()
            App.sharedPreferences.edit().putString("7", "12:00-12:01").commit()
            App.sharedPreferences.edit().putString("8", "12:00-12:01").commit()
            App.sharedPreferences.edit().putString("9", "12:00-12:01").commit()
            App.sharedPreferences.edit().putString("10", "12:00-12:01").commit()
            App.sharedPreferences.edit().putString("11", "12:00-12:01").commit()
            App.sharedPreferences.edit().putString("12", "12:00-12:01").commit()
        }
        timeMap.clear()
        for (i in 1..classSchedule) {
            timeMap[i.toString()] = App.sharedPreferences.getString(i.toString(), "12:00-12:01")!!
        }
    }

    /**
     * 设置面板
     */
    private fun setText(key: String = "") {
        if (key == "") {
            for (i in 1..classSchedule) {
                val a = timeMap[i.toString()]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                getOneClass(i.toString(), a[0], a[1])
            }
        } else {
            val up = ll.findViewWithTag<RelativeLayout>("$key-s")
            val down = ll.findViewWithTag<RelativeLayout>("$key-x")
            val upView: AppCompatTextView = up.getChildAt(1) as AppCompatTextView
            val downView: AppCompatTextView = down.getChildAt(1) as AppCompatTextView
            upView.text = timeMap[key]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            downView.text = timeMap[key]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        }
    }

    /**
     * 初始化视图
     */
    private fun init() {
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.title = "课时设置"
        }
        val appCompatSpinner = AppCompatSpinner(this)
        val list = listOf(5, 6, 7, 8, 9, 10, 11, 12)
        appCompatSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, list)
        val defaultClassSection = App.sharedPreferences.getInt(ConstantPool.Str.CLASS_SECTION.get(), 5)
        val index = list.indexOf(defaultClassSection)
        val i = if (index == -1) {
            0
        } else {
            index
        }
        appCompatSpinner.setSelection(i)
        AlertDialog.Builder(this).setTitle("设置每天课程节数")
                .setView(appCompatSpinner)
                .setCancelable(false)
                .setPositiveButton("保存") { dialog, _ ->
                    classSchedule = (appCompatSpinner.selectedItem as Int)
                    initData()
                    dialog.cancel()
                    setText()
                }
                .show()
    }

    private fun getOneClass(classText: CharSequence, upTime: CharSequence, downTime: CharSequence) {
        val up = getOneRowRelativeLayout("第${classText}节上课", upTime, View.OnClickListener { onViewClicked("$classText-s", it) })
        val down = getOneRowRelativeLayout("第${classText}节下课", downTime, View.OnClickListener { onViewClicked("$classText-x", it) })
        up.tag = "$classText-s"
        down.tag = "$classText-x"
        ll.addView(up)
        ll.addView(down)
    }

    private fun getOneRowRelativeLayout(leftText: CharSequence, rightText: CharSequence, l: View.OnClickListener?): RelativeLayout {
        val relativeLayout = RelativeLayout(this)
        val outValue = TypedValue()
        this.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        relativeLayout.setBackgroundResource(outValue.resourceId)
        val paddingInDp = 16
        val scale = this.resources.displayMetrics.density
        val paddingInPx = (paddingInDp * scale + 0.5f).toInt()

        val appCompatTextView = AppCompatTextView(this)
        appCompatTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        appCompatTextView.textSize = 16f
        appCompatTextView.text = leftText

        val timeAppCompatTextView = AppCompatTextView(this)
        timeAppCompatTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        timeAppCompatTextView.textSize = 16f
        timeAppCompatTextView.text = rightText
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)

        ThemeChangeUtil.setTextViewsColorByTheme(this, appCompatTextView, timeAppCompatTextView)

        relativeLayout.addView(appCompatTextView)
        relativeLayout.addView(timeAppCompatTextView, layoutParams)
        relativeLayout.setOnClickListener(l)
        return relativeLayout
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_custom_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.done -> run {
                if (DateUtils.isDataLegitimate(timeMap, this)) {
                    updateSharedPreferences()
                    if (App.sharedPreferences.edit()
                                    .putString(ConstantPool.Str.APP_CLASS_SCHEDULE_VERSION.get(), "")
                                    .putString(ConstantPool.Str.USER_USERNAME.get(), "test")
                                    .putString(ConstantPool.Str.USER_CLASS_ID.get(), "-1")
                                    .putInt(ConstantPool.Str.CLASS_SECTION.get(), classSchedule)
                                    .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                                    .commit()) {
                        DateUtils.refreshTimeList()
                    }
                    if (!isTaskRoot) {
                        EventBus.getDefault().post(EventEntity(ConstantPool.Int.TIME_TICK_CHANGE, ""))
                        finish()
                        return@run
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    return@run
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 更新SharedPreferences
     */
    private fun updateSharedPreferences() {
        val edit = App.sharedPreferences.edit()
        for ((key, value) in timeMap) {
            edit.putString(key, value)
        }
        edit.apply()
        DateUtils.refreshTimeList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(eventEntity: EventEntity) {

    }

    private fun onViewClicked(id: String, view: View) {
        this.msg = id
        showTimePickerDialog()
    }

    /**
     * 显示时间选择器
     */
    private fun showTimePickerDialog() {
        val type = msg.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val stringBuilder = StringBuilder(6).append("第${type[0]}节")
        if (type[1] == "s") {
            stringBuilder.append("上课")
        } else {
            stringBuilder.append("下课")
        }
        val timePickerDialog = getTimePickerDialog(type[1], timeMap[type[0]]!!)
        timePickerDialog.title = stringBuilder.toString()
        timePickerDialog.show(fragmentManager, "TimePickerDialog")
    }

    /**
     * 获取时间选择器
     *
     * @param type 上课还是下课
     * @param time 时间
     * @return [TimePickerDialog]
     */
    @NonNull
    @CheckResult
    private fun getTimePickerDialog(type: String, time: String): TimePickerDialog {
        val hour: Int
        val minute: Int
        if ("" == time) {
            val now = Calendar.getInstance()
            hour = now.get(Calendar.HOUR_OF_DAY)
            minute = now.get(Calendar.MINUTE)
        } else {
            if (CLASS_UP == type) {
                val timeArray = time.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                hour = Integer.parseInt(timeArray[0])
                minute = Integer.parseInt(timeArray[1])
            } else {
                val timeArray = time.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                hour = Integer.parseInt(timeArray[0])
                minute = Integer.parseInt(timeArray[1])
            }
        }
        return TimePickerDialog.newInstance(this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
        Log.d(TAG, "hourOfDay:$hourOfDay minute:$minute")
        if (hourOfDay != -1 && minute != -1) {
            //2-s -> 2 s
            val typeInfo = msg.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var h = hourOfDay.toString()
            var m = minute.toString()
            if (hourOfDay < MIN_TIME) {
                h = "0$h"
            }
            if (minute < MIN_TIME) {
                m = "0$m"
            }
            val time = "$h:$m"
            val s = timeMap[typeInfo[0]] ?: return
            if ("" != s) {
                val insertStr: String = if (CLASS_UP == typeInfo[1]) {
                    time + "-" + s.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                } else {
                    s.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "-" + time
                }
                Log.d(TAG, "insert :$insertStr")
                timeMap[typeInfo[0]] = insertStr
                setText(typeInfo[0])
            }
        }
    }

    companion object {
        private const val TAG = "CustomActivity"
        private const val CLASS_UP = "s"
        private const val MIN_TIME = 10
    }
}
