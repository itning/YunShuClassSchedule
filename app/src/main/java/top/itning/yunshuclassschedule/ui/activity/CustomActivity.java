package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.DateUtils;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 自定义课程
 *
 * @author itning
 */
public class CustomActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener {
    private static final String TAG = "CustomActivity";

    private static final String CLASS_UP = "s";
    private static final int MIN_TIME = 10;

    private String msg;
    private TreeMap<String, String> timeMap;

    @BindView(R.id.rl_1_s)
    RelativeLayout rl1S;
    @BindView(R.id.rl_1_x)
    RelativeLayout rl1X;
    @BindView(R.id.rl_2_s)
    RelativeLayout rl2S;
    @BindView(R.id.rl_2_x)
    RelativeLayout rl2X;
    @BindView(R.id.rl_3_s)
    RelativeLayout rl3S;
    @BindView(R.id.rl_3_x)
    RelativeLayout rl3X;
    @BindView(R.id.rl_4_s)
    RelativeLayout rl4S;
    @BindView(R.id.rl_4_x)
    RelativeLayout rl4X;
    @BindView(R.id.rl_5_s)
    RelativeLayout rl5S;
    @BindView(R.id.rl_5_x)
    RelativeLayout rl5X;
    @BindView(R.id.s_1)
    AppCompatTextView s1;
    @BindView(R.id.x_1)
    AppCompatTextView x1;
    @BindView(R.id.s_2)
    AppCompatTextView s2;
    @BindView(R.id.x_2)
    AppCompatTextView x2;
    @BindView(R.id.s_3)
    AppCompatTextView s3;
    @BindView(R.id.x_3)
    AppCompatTextView x3;
    @BindView(R.id.s_4)
    AppCompatTextView s4;
    @BindView(R.id.x_4)
    AppCompatTextView x4;
    @BindView(R.id.s_5)
    AppCompatTextView s5;
    @BindView(R.id.x_5)
    AppCompatTextView x5;
    @BindView(R.id.tv_morning)
    AppCompatTextView tvMorning;
    @BindView(R.id.tv_afternoon)
    AppCompatTextView tvAfternoon;
    @BindView(R.id.tv_at_night)
    AppCompatTextView tvAtNight;

    @BindView(R.id.a1)
    AppCompatTextView a1;
    @BindView(R.id.a2)
    AppCompatTextView a2;
    @BindView(R.id.a3)
    AppCompatTextView a3;
    @BindView(R.id.a4)
    AppCompatTextView a4;
    @BindView(R.id.a5)
    AppCompatTextView a5;
    @BindView(R.id.a6)
    AppCompatTextView a6;
    @BindView(R.id.a7)
    AppCompatTextView a7;
    @BindView(R.id.a8)
    AppCompatTextView a8;
    @BindView(R.id.a9)
    AppCompatTextView a9;
    @BindView(R.id.a10)
    AppCompatTextView a10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChangeUtil.changeTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        String time1 = App.sharedPreferences.getString("1", "08:20-09:50");
        String time2 = App.sharedPreferences.getString("2", "10:05-11:35");
        String time3 = App.sharedPreferences.getString("3", "12:55-14:25");
        String time4 = App.sharedPreferences.getString("4", "14:40-16:10");
        String time5 = App.sharedPreferences.getString("5", "17:30-20:00");
        timeMap = new TreeMap<>();
        timeMap.put("1", time1);
        timeMap.put("2", time2);
        timeMap.put("3", time3);
        timeMap.put("4", time4);
        timeMap.put("5", time5);
        setText();
    }

    /**
     * 设置面板
     */
    private void setText() {
        String[] a1 = timeMap.get("1").split("-");
        String[] a2 = timeMap.get("2").split("-");
        String[] a3 = timeMap.get("3").split("-");
        String[] a4 = timeMap.get("4").split("-");
        String[] a5 = timeMap.get("5").split("-");
        s1.setText(a1[0]);
        s2.setText(a2[0]);
        s3.setText(a3[0]);
        s4.setText(a4[0]);
        s5.setText(a5[0]);

        x1.setText(a1[1]);
        x2.setText(a2[1]);
        x3.setText(a3[1]);
        x4.setText(a4[1]);
        x5.setText(a5[1]);
    }

    /**
     * 初始化视图
     */
    private void initView() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("课时设置");
        }
        int nowThemeColorAccent = ThemeChangeUtil.getNowThemeColorAccent(this);
        tvMorning.setTextColor(nowThemeColorAccent);
        tvAfternoon.setTextColor(nowThemeColorAccent);
        tvAtNight.setTextColor(nowThemeColorAccent);
        ThemeChangeUtil.setTextViewsColorByTheme(this,
                s1, x1, s2, x2, s3, x3, s4, x4, s5, x5,
                a1, a2, a3, a4, a5, a6, a7, a8, a9, a10);
        new AlertDialog.Builder(this).setTitle("关于课节")
                .setMessage("我们将两节小课合成为1节课,这样上午有2节课,下午有2节课,晚自习(如果有)1节课\n全天共计5节课")
                .setPositiveButton("我知道了", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_custom_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.done: {
                if (DateUtils.isDataLegitimate(timeMap, this)) {
                    updateSharedPreferences();
                    App.sharedPreferences.edit()
                            .putString(ConstantPool.Str.APP_CLASS_SCHEDULE_VERSION.get(), "")
                            .putString(ConstantPool.Str.USER_USERNAME.get(), "test")
                            .putString(ConstantPool.Str.USER_CLASS_ID.get(), "-1")
                            .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                            .apply();
                    if (!isTaskRoot()) {
                        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.TIME_TICK_CHANGE, ""));
                        finish();
                        break;
                    }
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
                }
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 更新SharedPreferences
     */
    private void updateSharedPreferences() {
        SharedPreferences.Editor edit = App.sharedPreferences.edit();
        for (Map.Entry<String, String> entry : timeMap.entrySet()) {
            edit.putString(entry.getKey(), entry.getValue());
        }
        edit.apply();
        DateUtils.refreshTimeList();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {

    }

    @OnClick({R.id.rl_1_s, R.id.rl_1_x, R.id.rl_2_s, R.id.rl_2_x, R.id.rl_3_s, R.id.rl_3_x, R.id.rl_4_s, R.id.rl_4_x, R.id.rl_5_s, R.id.rl_5_x})
    public void onViewClicked(View view) {
        String msg;
        switch (view.getId()) {
            case R.id.rl_1_s:
                msg = "1-s";
                break;
            case R.id.rl_1_x:
                msg = "1-x";
                break;
            case R.id.rl_2_s:
                msg = "2-s";
                break;
            case R.id.rl_2_x:
                msg = "2-x";
                break;
            case R.id.rl_3_s:
                msg = "3-s";
                break;
            case R.id.rl_3_x:
                msg = "3-x";
                break;
            case R.id.rl_4_s:
                msg = "4-s";
                break;
            case R.id.rl_4_x:
                msg = "4-x";
                break;
            case R.id.rl_5_s:
                msg = "5-s";
                break;
            case R.id.rl_5_x:
                msg = "5-x";
                break;
            default:
                msg = "";
        }
        this.msg = msg;
        showTimePickerDialog();
    }

    /**
     * 显示时间选择器
     */
    private void showTimePickerDialog() {
        String type = msg.split("-")[1];
        String time;
        String title;
        switch (msg) {
            case "1-s": {
                time = timeMap.get("1");
                title = "第一节上课";
                break;
            }
            case "1-x": {
                time = timeMap.get("1");
                title = "第一节下课";
                break;
            }
            case "2-s": {
                time = timeMap.get("2");
                title = "第二节上课";
                break;
            }
            case "2-x": {
                time = timeMap.get("2");
                title = "第二节下课";
                break;
            }
            case "3-s": {
                time = timeMap.get("3");
                title = "第三节上课";
                break;
            }
            case "3-x": {
                time = timeMap.get("3");
                title = "第三节下课";
                break;
            }
            case "4-s": {
                time = timeMap.get("4");
                title = "第四节上课";
                break;
            }
            case "4-x": {
                time = timeMap.get("4");
                title = "第四节下课";
                break;
            }
            case "5-s": {
                time = timeMap.get("5");
                title = "第五节上课";
                break;
            }
            case "5-x": {
                time = timeMap.get("5");
                title = "第五节下课";
                break;
            }
            default:
                time = "";
                title = "";
        }
        TimePickerDialog timePickerDialog = getTimePickerDialog(type, time);
        timePickerDialog.setTitle(title);
        timePickerDialog.show(getFragmentManager(), "TimePickerDialog");
    }

    /**
     * 获取时间选择器
     *
     * @param type 上课还是下课
     * @param time 时间
     * @return {@link TimePickerDialog}
     */
    @NonNull
    @CheckResult
    private TimePickerDialog getTimePickerDialog(String type, String time) {
        int hour;
        int minute;
        if ("".equals(time)) {
            Calendar now = Calendar.getInstance();
            hour = now.get(Calendar.HOUR_OF_DAY);
            minute = now.get(Calendar.MINUTE);
        } else {
            if (CLASS_UP.equals(type)) {
                String[] timeArray = time.split("-")[0].split(":");
                hour = Integer.parseInt(timeArray[0]);
                minute = Integer.parseInt(timeArray[1]);
            } else {
                String[] timeArray = time.split("-")[1].split(":");
                hour = Integer.parseInt(timeArray[0]);
                minute = Integer.parseInt(timeArray[1]);
            }
        }
        return TimePickerDialog.newInstance(this, hour, minute, true);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Log.d(TAG, "hourOfDay:" + hourOfDay + " minute:" + minute);
        if (hourOfDay != -1 && minute != -1) {
            //2-s -> 2 s
            String[] typeInfo = msg.split("-");
            String h = hourOfDay + "";
            String m = minute + "";
            if (hourOfDay < MIN_TIME) {
                h = "0" + h;
            }
            if (minute < MIN_TIME) {
                m = "0" + m;
            }
            String time = h + ":" + m;
            String s = timeMap.get(typeInfo[0]);
            if (s == null) {
                return;
            }
            if (!"".equals(s)) {
                String insertStr;
                if (CLASS_UP.equals(typeInfo[1])) {
                    insertStr = time + "-" + s.split("-")[1];
                } else {
                    insertStr = s.split("-")[0] + "-" + time;
                }
                Log.d(TAG, "insert :" + insertStr);
                timeMap.put(typeInfo[0], insertStr);
                setText();
            }
        }
    }
}
