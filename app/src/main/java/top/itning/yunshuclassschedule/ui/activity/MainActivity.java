package top.itning.yunshuclassschedule.ui.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.ui.fragment.CheckScoreFragment;
import top.itning.yunshuclassschedule.ui.fragment.ClassScheduleFragment;
import top.itning.yunshuclassschedule.util.ApkInstallUtils;

/**
 * 主活动
 *
 * @author itning
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        //默认选中第一项
        navView.getMenu().getItem(0).setChecked(true);
        navView.setNavigationItemSelectedListener(this);

        supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager
                .beginTransaction()
                .add(R.id.frame_container, fragmentSparseArray.get(R.id.nav_class_schedule))
                .commit();
    }

    /**
     * 消息事件
     *
     * @param what what
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Integer what) {
        Log.d(TAG, what + "");
    }


    @Override
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case INSTALL_APK: {
                ApkInstallUtils.installApk(new File(Environment.getExternalStorageDirectory(), eventEntity.getMsg()), this, true, true);
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
            if (System.currentTimeMillis() - firstPressedTime < ConstantPool.Int.EXIT_DELAY.get()) {
                moveTaskToBack(false);
            } else {
                Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                firstPressedTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //菜单回调
        switch (item.getItemId()) {
            case R.id.action_set_text_size: {
                Toast.makeText(this, "更改字体", Toast.LENGTH_LONG).show();
                return true;
            }
            case R.id.action_course_error: {
                Toast.makeText(this, "课程错误", Toast.LENGTH_LONG).show();
                return true;
            }
            case R.id.action_feedback: {
                Toast.makeText(this, "反馈建议", Toast.LENGTH_LONG).show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
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
                Toast.makeText(this, "设置", Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.nav_about: {
                Toast.makeText(this, "关于", Toast.LENGTH_LONG).show();
                break;
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
