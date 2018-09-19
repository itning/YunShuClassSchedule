package top.itning.yunshuclassschedule.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.ui.fragment.CheckScoreFragment;
import top.itning.yunshuclassschedule.ui.fragment.ClassScheduleFragment;
import top.itning.yunshuclassschedule.util.DateUtils;
import top.itning.yunshuclassschedule.util.Glide4Engine;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 主活动
 *
 * @author itning
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_CHOOSE = 101;
    public static final int REQUEST_CODE = 103;
    public static final int SETTING_REQUEST_CODE = 104;

    private FragmentManager supportFragmentManager;
    private SparseArray<Fragment> fragmentSparseArray;
    private long firstPressedTime;
    private static final SimpleDateFormat ACTION_BAR_TITLE_FORMAT = new SimpleDateFormat("MM月dd日 E", Locale.CHINESE);

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    private SwitchCompat drawerSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChangeUtil.changeTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        fragmentSparseArray = new SparseArray<>();
        fragmentSparseArray.put(R.id.nav_class_schedule, new ClassScheduleFragment());
        fragmentSparseArray.put(R.id.nav_check_score, new CheckScoreFragment());
    }

    /**
     * 初始化视图
     */
    private void initView() {
        //设置主标题
        toolbar.setTitle(ACTION_BAR_TITLE_FORMAT.format(new Date()));
        //设置导航
        setSupportActionBar(toolbar);
        ThemeChangeUtil.initColor(this, drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        //默认选中第一项
        navView.getMenu().getItem(0).setChecked(true);
        drawerSwitch = (SwitchCompat) navView.getMenu().findItem(R.id.nav_day_night).getActionView();
        if (ThemeChangeUtil.isChange) {
            drawerSwitch.setChecked(true);
        }
        drawerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> ThemeChangeUtil.changeNightMode(this));
        navView.setNavigationItemSelectedListener(this);

        supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager
                .beginTransaction()
                .add(R.id.frame_container, fragmentSparseArray.get(R.id.nav_class_schedule))
                .commit();
        App.sharedPreferences.edit().putInt(ConstantPool.Str.LAST_DATE.get(), Calendar.getInstance().get(Calendar.DATE)).apply();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case TIME_TICK_CHANGE: {
                if (DateUtils.isNewDay()) {
                    fragmentSparseArray.remove(R.id.nav_class_schedule);
                    fragmentSparseArray.put(R.id.nav_class_schedule, new ClassScheduleFragment());
                    if (supportFragmentManager.findFragmentById(R.id.frame_container) instanceof ClassScheduleFragment) {
                        supportFragmentManager
                                .beginTransaction()
                                .replace(R.id.frame_container, fragmentSparseArray.get(R.id.nav_class_schedule))
                                .commitAllowingStateLoss();
                    }
                }
                break;
            }
            case APP_COLOR_CHANGE: {
                Log.d(TAG, "app color change , now afresh view");
                ThemeChangeUtil.initColor(this, drawerLayout);
                break;
            }
            case DESTROY_ACTIVITY: {
                finish();
                break;
            }
            default:
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        //设置主标题
        toolbar.setTitle(ACTION_BAR_TITLE_FORMAT.format(new Date()));
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!isConsumption()) {
                if (System.currentTimeMillis() - firstPressedTime < ConstantPool.Int.EXIT_DELAY.get()) {
                    moveTaskToBack(false);
                } else {
                    Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                    firstPressedTime = System.currentTimeMillis();
                }
            }
        }
    }

    /**
     * fragment 是否消费返回事件
     *
     * @return 已消费返回真
     */
    private boolean isConsumption() {
        Fragment f = supportFragmentManager.findFragmentById(R.id.frame_container);
        //查询成绩页面
        if (f instanceof CheckScoreFragment) {
            CheckScoreFragment checkScoreFragment = (CheckScoreFragment) f;
            return checkScoreFragment.eventTrigger();
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_show_teacher_info)
                .setTitle(
                        App.sharedPreferences.getBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), false)
                                ? "隐藏授课教师" : "显示授课教师");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //菜单回调
        switch (item.getItemId()) {
            case R.id.action_set_text_size: {
                changeWeekFragmentFont();
                return true;
            }
            case R.id.action_set_background_image: {
                if (checkPermission()) {
                    startSelectImageActivity();
                }
                return true;
            }
            case R.id.action_show_teacher_info: {
                if (App.sharedPreferences.getBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), false)) {
                    App.sharedPreferences.edit().putBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), false).apply();
                    item.setTitle("显示授课教师");
                } else {
                    App.sharedPreferences.edit().putBoolean(ConstantPool.Str.TEACHER_INFO_STATUS.get(), true).apply();
                    item.setTitle("隐藏授课教师");
                }
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.REFRESH_WEEK_FRAGMENT_DATA));
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 显示弹窗
     */
    private void showDialogToUser() {
        new AlertDialog.Builder(this).setTitle("需要外置存储权限和相机权限")
                .setMessage("请授予外置存储权限和相机权限,才能够更换背景图片")
                .setCancelable(false)
                .setPositiveButton("确定", (dialog1, which1) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CODE))
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPermission()) {
                    startSelectImageActivity();
                }
            } else {
                new AlertDialog.Builder(this).setTitle("需要外置存储权限和相机权限")
                        .setMessage("请授予外置存储权限和相机权限,才能够更换背景图片")
                        .setCancelable(false)
                        .setPositiveButton("确定", (dialog, which) -> startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)), SETTING_REQUEST_CODE))
                        .setNegativeButton("取消", null)
                        .show();
            }
        }
    }

    /**
     * 检查权限
     *
     * @return 权限通过
     */
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showDialogToUser();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * 开启选择图片Activity
     */
    @RequiresPermission(allOf = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    private void startSelectImageActivity() {
        Matisse.from(MainActivity.this)
                //图片类型
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.BMP))
                //true:选中后显示数字;false:选中后显示对号
                .countable(true)
                //可选的最大数
                .maxSelectable(1)
                //选择照片时，是否显示拍照
                .capture(true)
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .captureStrategy(new CaptureStrategy(true, "top.itning.yunshuclassschedule.fileProvider"))
                //图片加载引擎
                .imageEngine(new Glide4Engine())
                .theme(com.zhihu.matisse.R.style.Matisse_Dracula)
                .forResult(REQUEST_CODE_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> result = Matisse.obtainResult(data);
            if (result != null && !result.isEmpty()) {
                Log.d(TAG, "the result uri:" + result.get(0).toString());
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.NOTIFICATION_BACKGROUND_CHANGE, "", result.get(0)));
            } else {
                Toast.makeText(this, "背景图片设置失败", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == SETTING_REQUEST_CODE) {
            if (checkPermission()) {
                startSelectImageActivity();
            }
        }
    }

    /**
     * 更改本周课程字体大小
     */
    private void changeWeekFragmentFont() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.setting_week_font, null);
        TextView tvFontPreview = view.findViewById(R.id.tv_font_preview);
        float setFont = App.sharedPreferences.getFloat(ConstantPool.Str.WEEK_FONT_SIZE.get(), 12);
        tvFontPreview.setText(MessageFormat.format("字体大小:{0}", setFont));
        tvFontPreview.setTextSize(setFont);
        AppCompatSeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setProgress((int) setFont - 10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress + 10;
                tvFontPreview.setText(MessageFormat.format("字体大小:{0}", this.progress));
                tvFontPreview.setTextSize(this.progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                App.sharedPreferences.edit().putFloat(ConstantPool.Str.WEEK_FONT_SIZE.get(), progress).apply();
            }
        });
        new AlertDialog.Builder(this)
                .setView(view)
                .setTitle("更改字体大小")
                .setPositiveButton("确定", null)
                .setOnDismissListener(dialog -> EventBus.getDefault().post(new EventEntity(ConstantPool.Int.REFRESH_WEEK_FRAGMENT_DATA)))
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //导航抽屉回调
        switch (item.getItemId()) {
            case R.id.nav_class_schedule: {
                setFragment(R.id.nav_class_schedule);
                break;
            }
            case R.id.nav_check_score: {
                setFragment(R.id.nav_check_score);
                break;
            }
            case R.id.nav_settings: {
                startActivity(new Intent(this, SettingActivity.class));
                break;
            }
            case R.id.nav_day_night: {
                drawerSwitch.setChecked(!drawerSwitch.isChecked());
                return true;
            }
            default:
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 设置Fragment
     *
     * @param id id
     */
    private void setFragment(@IdRes int id) {
        Fragment fragment = fragmentSparseArray.get(id);
        Fragment f = supportFragmentManager.findFragmentById(R.id.frame_container);
        if (f != fragment) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();
        }
    }
}
