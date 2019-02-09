package top.itning.yunshuclassschedule.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.tencent.bugly.crashreport.CrashReport
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.CaptureStrategy
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.BaseActivity
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.fragment.CheckScoreFragment
import top.itning.yunshuclassschedule.ui.fragment.ClassScheduleFragment
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
import top.itning.yunshuclassschedule.util.DateUtils
import top.itning.yunshuclassschedule.util.FileUtils
import top.itning.yunshuclassschedule.util.Glide4Engine
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 主活动
 *
 * @author itning
 */
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentSparseArray: SparseArray<Fragment>
    private var firstPressedTime: Long = 0
    private lateinit var drawerSwitch: SwitchCompat

    /**
     * fragment 是否消费返回事件
     *
     * @return 已消费返回真
     */
    private//查询成绩页面
    val isConsumption: Boolean
        get() {
            val f = supportFragmentManager.findFragmentById(R.id.frame_container)
            return (f as? CheckScoreFragment)?.eventTrigger() ?: false
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeChangeUtil.changeMainActivityTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        initData()
        initView()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        tempNumberOfWeek = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!
        fragmentSparseArray = SparseArray()
        fragmentSparseArray.put(R.id.nav_class_schedule, ClassScheduleFragment())
        fragmentSparseArray.put(R.id.nav_check_score, CheckScoreFragment())
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        //设置主标题
        toolbar.title = ACTION_BAR_TITLE_FORMAT.format(Date())
        toolbar.setOnClickListener {
            val now = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!
            if (tempNumberOfWeek != now) {
                tempNumberOfWeek = now
                EventBus.getDefault().post(EventEntity(ConstantPool.Int.CLASS_WEEK_CHANGE, tempNumberOfWeek))
                Toast.makeText(this, "回到当前周", Toast.LENGTH_SHORT).show()
            }
        }
        toolbar.setOnLongClickListener {
            val appCompatSpinner = AppCompatSpinner(this)
            val list = 1..50
            appCompatSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, list.toList())
            appCompatSpinner.setSelection(tempNumberOfWeek.toInt() - 1)
            AlertDialog.Builder(this)
                    .setTitle("快速跳转到其它周")
                    .setView(appCompatSpinner)
                    .setPositiveButton("确定") { _, _ ->
                        if (appCompatSpinner.selectedItem.toString() != tempNumberOfWeek) {
                            tempNumberOfWeek = appCompatSpinner.selectedItem.toString()
                            toolbar.title = "第${tempNumberOfWeek}周"
                            EventBus.getDefault().post(EventEntity(ConstantPool.Int.CLASS_WEEK_CHANGE, tempNumberOfWeek))
                        }
                    }
                    .show()
            true
        }
        //设置导航
        setSupportActionBar(toolbar)
        ThemeChangeUtil.initColor(this, drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(@NonNull drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(@NonNull drawerView: View) {}

            override fun onDrawerClosed(@NonNull drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)
            }
        })
        toggle.syncState()
        //默认选中第一项
        nav_view.menu.getItem(0).isChecked = true
        drawerSwitch = nav_view.menu.findItem(R.id.nav_day_night).actionView as SwitchCompat
        if (ThemeChangeUtil.isChange) {
            drawerSwitch.isChecked = true
        }
        drawerSwitch.setOnCheckedChangeListener { _, _ -> ThemeChangeUtil.changeNightMode(this) }
        nav_view.setNavigationItemSelectedListener(this)
        App.sharedPreferences.edit().putInt(ConstantPool.Str.LAST_DATE.get(), Calendar.getInstance().get(Calendar.DATE)).apply()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.TIME_TICK_CHANGE -> {
                if (DateUtils.isNewDay || eventEntity.msg != null) {
                    Log.d(TAG, "time tick event , now new ClassScheduleFragment")
                    fragmentSparseArray.remove(R.id.nav_class_schedule)
                    fragmentSparseArray.put(R.id.nav_class_schedule, ClassScheduleFragment())
                    if (supportFragmentManager.findFragmentById(R.id.frame_container) is ClassScheduleFragment) {
                        supportFragmentManager
                                .beginTransaction()
                                .replace(R.id.frame_container, fragmentSparseArray.get(R.id.nav_class_schedule))
                                .commitAllowingStateLoss()
                    }
                }
            }
            ConstantPool.Int.APP_COLOR_CHANGE -> {
                Log.d(TAG, "app color change , now afresh view")
                ThemeChangeUtil.initColor(this, drawer_layout)
            }
            else -> {
            }
        }
    }

    override fun onDestroy() {
        fragmentSparseArray.clear()
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onStop() {
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        if (fragment is ClassScheduleFragment) {
            supportFragmentManager.beginTransaction().remove(fragmentSparseArray.get(R.id.nav_class_schedule)).commitAllowingStateLoss()
        }
        super.onStop()
    }

    override fun onStart() {
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        if (fragment == null || fragment is ClassScheduleFragment) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.frame_container, fragmentSparseArray.get(R.id.nav_class_schedule))
                    .commitAllowingStateLoss()
        }
        //设置主标题
        toolbar.title = ACTION_BAR_TITLE_FORMAT.format(Date())
        super.onStart()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (!isConsumption) {
                if (System.currentTimeMillis() - firstPressedTime < ConstantPool.Int.EXIT_DELAY.get()) {
                    moveTaskToBack(false)
                } else {
                    Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show()
                    firstPressedTime = System.currentTimeMillis()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_show_teacher_info).title = if (App.sharedPreferences.getBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), false))
            "隐藏授课教师"
        else
            "显示授课教师"
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //菜单回调
        when (item.itemId) {
            R.id.action_set_text_size -> {
                changeWeekFragmentFont()
                return true
            }
            R.id.action_set_background_image -> {
                if (checkPermission()) {
                    startSelectImageActivity()
                }
                return true
            }
            R.id.action_show_teacher_info -> {
                if (App.sharedPreferences.getBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), false)) {
                    App.sharedPreferences.edit().putBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), false).apply()
                    item.title = "显示授课教师"
                } else {
                    App.sharedPreferences.edit().putBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), true).apply()
                    item.title = "隐藏授课教师"
                }
                EventBus.getDefault().post(EventEntity(ConstantPool.Int.REFRESH_WEEK_FRAGMENT_DATA))
                return true
            }
            R.id.action_last_week -> {
                if (tempNumberOfWeek == "1") {
                    return true
                }
                val t = tempNumberOfWeek.toInt() - 1
                tempNumberOfWeek = t.toString()
                EventBus.getDefault().post(EventEntity(ConstantPool.Int.CLASS_WEEK_CHANGE, tempNumberOfWeek))
                return true
            }
            R.id.action_next_week -> {
                if (tempNumberOfWeek == "50") {
                    return true
                }
                val t = tempNumberOfWeek.toInt() + 1
                tempNumberOfWeek = t.toString()
                EventBus.getDefault().post(EventEntity(ConstantPool.Int.CLASS_WEEK_CHANGE, tempNumberOfWeek))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * 显示弹窗
     */
    private fun showDialogToUser() {
        AlertDialog.Builder(this).setTitle("需要外置存储权限")
                .setMessage("请授予外置存储权限,才能够更换背景图片")
                .setCancelable(false)
                .setPositiveButton("确定") { _, _ -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE) }
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            var granted = true
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                }
            }
            if (granted) {
                if (checkPermission()) {
                    startSelectImageActivity()
                }
            } else {
                AlertDialog.Builder(this).setTitle("需要外置存储权限")
                        .setMessage("请授予外置存储权限,才能够更换背景图片")
                        .setCancelable(false)
                        .setPositiveButton("确定") { _, _ -> startActivityForResult(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)), SETTING_REQUEST_CODE) }
                        .setNegativeButton("取消", null)
                        .show()
            }
        }
    }

    /**
     * 检查权限
     *
     * @return 权限通过
     */
    private fun checkPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showDialogToUser()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            }
            false
        } else {
            true
        }
    }

    /**
     * 开启选择图片Activity
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun startSelectImageActivity() {
        Matisse.from(this@MainActivity)
                //图片类型
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.BMP))
                //true:选中后显示数字;false:选中后显示对号
                .countable(true)
                //可选的最大数
                .maxSelectable(1)
                //选择照片时，是否显示拍照
                .capture(false)
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .captureStrategy(CaptureStrategy(true, "top.itning.yunshuclassschedule.fileProvider"))
                //图片加载引擎
                .imageEngine(Glide4Engine())
                .theme(com.zhihu.matisse.R.style.Matisse_Dracula)
                .forResult(REQUEST_CODE_CHOOSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            val result = Matisse.obtainResult(data)
            if (result != null && !result.isEmpty()) {
                Log.d(TAG, "the result uri:" + result[0].toString())
                FileUtils.transferFile(this, result[0], "background_img")
            } else {
                Toast.makeText(this, "背景图片设置失败", Toast.LENGTH_LONG).show()
                CrashReport.postCatchedException(Throwable("background image set failure"))
            }
        }
        if (requestCode == SETTING_REQUEST_CODE) {
            if (checkPermission()) {
                startSelectImageActivity()
            }
        }
    }

    /**
     * 更改本周课程字体大小
     */
    private fun changeWeekFragmentFont() {
        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(this).inflate(R.layout.view_range, null)
        val tvFontPreview = view.findViewById<TextView>(R.id.tv_font_preview)
        val setFont = App.sharedPreferences.getFloat(ConstantPool.Str.WEEK_FONT_SIZE.get(), 12f)
        tvFontPreview.text = MessageFormat.format("字体大小:{0}", setFont)
        tvFontPreview.textSize = setFont
        val bubbleSeekBar = view.findViewById<RangeSeekBar>(R.id.seekBar)
        bubbleSeekBar.setValue(setFont)
        var size = 1f
        bubbleSeekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            @Suppress("UsePropertyAccessSyntax")
            override fun onRangeChanged(view: RangeSeekBar, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                tvFontPreview.text = MessageFormat.format("字体大小:{0}", leftValue)
                tvFontPreview.textSize = leftValue
                size = leftValue
            }

            override fun onStartTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
                App.sharedPreferences.edit().putFloat(ConstantPool.Str.WEEK_FONT_SIZE.get(), size).apply()
            }
        })
        AlertDialog.Builder(this)
                .setView(view)
                .setTitle("更改字体大小")
                .setPositiveButton("确定", null)
                .setOnDismissListener { EventBus.getDefault().post(EventEntity(ConstantPool.Int.REFRESH_WEEK_FRAGMENT_DATA)) }
                .show()
    }

    override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
        //导航抽屉回调
        when (item.itemId) {
            R.id.nav_class_schedule -> {
                setFragment(R.id.nav_class_schedule)
            }
            R.id.nav_check_score -> {
                setFragment(R.id.nav_check_score)
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingActivity::class.java))
            }
            R.id.nav_day_night -> {
                drawerSwitch.isChecked = !drawerSwitch.isChecked
                return true
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * 设置Fragment
     *
     * @param id id
     */
    private fun setFragment(@IdRes id: Int) {
        val fragment = fragmentSparseArray.get(id)
        val f = supportFragmentManager.findFragmentById(R.id.frame_container)
        if (f !== fragment) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_CHOOSE = 101
        private const val REQUEST_CODE = 103
        private const val SETTING_REQUEST_CODE = 104
        val ACTION_BAR_TITLE_FORMAT = SimpleDateFormat("MM月dd日 E", Locale.CHINESE)
        var tempNumberOfWeek = "1"
    }
}
