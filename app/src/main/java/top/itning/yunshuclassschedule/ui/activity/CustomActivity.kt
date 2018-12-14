package top.itning.yunshuclassschedule.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.CheckResult
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
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
    private var timeMap: TreeMap<String, String> = TreeMap()

    @BindView(R.id.rl_1_s)
    lateinit var rl1S: RelativeLayout
    @BindView(R.id.rl_1_x)
    lateinit var rl1X: RelativeLayout
    @BindView(R.id.rl_2_s)
    lateinit var rl2S: RelativeLayout
    @BindView(R.id.rl_2_x)
    lateinit var rl2X: RelativeLayout
    @BindView(R.id.rl_3_s)
    lateinit var rl3S: RelativeLayout
    @BindView(R.id.rl_3_x)
    lateinit var rl3X: RelativeLayout
    @BindView(R.id.rl_4_s)
    lateinit var rl4S: RelativeLayout
    @BindView(R.id.rl_4_x)
    lateinit var rl4X: RelativeLayout
    @BindView(R.id.rl_5_s)
    lateinit var rl5S: RelativeLayout
    @BindView(R.id.rl_5_x)
    lateinit var rl5X: RelativeLayout
    @BindView(R.id.s_1)
    lateinit var s1: AppCompatTextView
    @BindView(R.id.x_1)
    lateinit var x1: AppCompatTextView
    @BindView(R.id.s_2)
    lateinit var s2: AppCompatTextView
    @BindView(R.id.x_2)
    lateinit var x2: AppCompatTextView
    @BindView(R.id.s_3)
    lateinit var s3: AppCompatTextView
    @BindView(R.id.x_3)
    lateinit var x3: AppCompatTextView
    @BindView(R.id.s_4)
    lateinit var s4: AppCompatTextView
    @BindView(R.id.x_4)
    lateinit var x4: AppCompatTextView
    @BindView(R.id.s_5)
    lateinit var s5: AppCompatTextView
    @BindView(R.id.x_5)
    lateinit var x5: AppCompatTextView
    @BindView(R.id.tv_morning)
    lateinit var tvMorning: AppCompatTextView
    @BindView(R.id.tv_afternoon)
    lateinit var tvAfternoon: AppCompatTextView
    @BindView(R.id.tv_at_night)
    lateinit var tvAtNight: AppCompatTextView

    @BindView(R.id.a1)
    lateinit var a1: AppCompatTextView
    @BindView(R.id.a2)
    lateinit var a2: AppCompatTextView
    @BindView(R.id.a3)
    lateinit var a3: AppCompatTextView
    @BindView(R.id.a4)
    lateinit var a4: AppCompatTextView
    @BindView(R.id.a5)
    lateinit var a5: AppCompatTextView
    @BindView(R.id.a6)
    lateinit var a6: AppCompatTextView
    @BindView(R.id.a7)
    lateinit var a7: AppCompatTextView
    @BindView(R.id.a8)
    lateinit var a8: AppCompatTextView
    @BindView(R.id.a9)
    lateinit var a9: AppCompatTextView
    @BindView(R.id.a10)
    lateinit var a10: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeChangeUtil.changeTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)
        ButterKnife.bind(this)
        EventBus.getDefault().register(this)
        initData()
        initView()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        val time1 = App.sharedPreferences.getString("1", "08:20-09:50")!!
        val time2 = App.sharedPreferences.getString("2", "10:05-11:35")!!
        val time3 = App.sharedPreferences.getString("3", "12:55-14:25")!!
        val time4 = App.sharedPreferences.getString("4", "14:40-16:10")!!
        val time5 = App.sharedPreferences.getString("5", "17:30-20:00")!!
        timeMap.clear()
        timeMap["1"] = time1
        timeMap["2"] = time2
        timeMap["3"] = time3
        timeMap["4"] = time4
        timeMap["5"] = time5
        setText()
    }

    /**
     * 设置面板
     */
    private fun setText() {
        val a1 = timeMap["1"]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val a2 = timeMap["2"]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val a3 = timeMap["3"]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val a4 = timeMap["4"]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val a5 = timeMap["5"]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        s1.text = a1[0]
        s2.text = a2[0]
        s3.text = a3[0]
        s4.text = a4[0]
        s5.text = a5[0]

        x1.text = a1[1]
        x2.text = a2[1]
        x3.text = a3[1]
        x4.text = a4[1]
        x5.text = a5[1]
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.title = "课时设置"
        }
        val nowThemeColorAccent = ThemeChangeUtil.getNowThemeColorAccent(this)
        tvMorning.setTextColor(nowThemeColorAccent)
        tvAfternoon.setTextColor(nowThemeColorAccent)
        tvAtNight.setTextColor(nowThemeColorAccent)
        ThemeChangeUtil.setTextViewsColorByTheme(this,
                s1, x1, s2, x2, s3, x3, s4, x4, s5, x5,
                a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
        AlertDialog.Builder(this).setTitle("关于课节")
                .setMessage("我们将两节小课合成为1节课,这样上午有2节课,下午有2节课,晚自习(如果有)1节课\n全天共计5节课")
                .setPositiveButton("我知道了", null)
                .show()
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
                    App.sharedPreferences.edit()
                            .putString(ConstantPool.Str.APP_CLASS_SCHEDULE_VERSION.get(), "")
                            .putString(ConstantPool.Str.USER_USERNAME.get(), "test")
                            .putString(ConstantPool.Str.USER_CLASS_ID.get(), "-1")
                            .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                            .apply()
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

    @OnClick(R.id.rl_1_s, R.id.rl_1_x, R.id.rl_2_s, R.id.rl_2_x, R.id.rl_3_s, R.id.rl_3_x, R.id.rl_4_s, R.id.rl_4_x, R.id.rl_5_s, R.id.rl_5_x)
    fun onViewClicked(view: View) {
        this.msg = when (view.id) {
            R.id.rl_1_s -> "1-s"
            R.id.rl_1_x -> "1-x"
            R.id.rl_2_s -> "2-s"
            R.id.rl_2_x -> "2-x"
            R.id.rl_3_s -> "3-s"
            R.id.rl_3_x -> "3-x"
            R.id.rl_4_s -> "4-s"
            R.id.rl_4_x -> "4-x"
            R.id.rl_5_s -> "5-s"
            R.id.rl_5_x -> "5-x"
            else -> ""
        }
        showTimePickerDialog()
    }

    /**
     * 显示时间选择器
     */
    private fun showTimePickerDialog() {
        val type = msg.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val time: String
        val title: String
        when (msg) {
            "1-s" -> {
                time = timeMap["1"]!!
                title = "第一节上课"
            }
            "1-x" -> {
                time = timeMap["1"]!!
                title = "第一节下课"
            }
            "2-s" -> {
                time = timeMap["2"]!!
                title = "第二节上课"
            }
            "2-x" -> {
                time = timeMap["2"]!!
                title = "第二节下课"
            }
            "3-s" -> {
                time = timeMap["3"]!!
                title = "第三节上课"
            }
            "3-x" -> {
                time = timeMap["3"]!!
                title = "第三节下课"
            }
            "4-s" -> {
                time = timeMap["4"]!!
                title = "第四节上课"
            }
            "4-x" -> {
                time = timeMap["4"]!!
                title = "第四节下课"
            }
            "5-s" -> {
                time = timeMap["5"]!!
                title = "第五节上课"
            }
            "5-x" -> {
                time = timeMap["5"]!!
                title = "第五节下课"
            }
            else -> {
                time = ""
                title = ""
            }
        }
        val timePickerDialog = getTimePickerDialog(type, time)
        timePickerDialog.title = title
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
            var h = hourOfDay.toString() + ""
            var m = minute.toString() + ""
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
                setText()
            }
        }
    }

    companion object {
        private const val TAG = "CustomActivity"
        private const val CLASS_UP = "s"
        private const val MIN_TIME = 10
    }
}
