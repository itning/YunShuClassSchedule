package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 登陆
 *
 * @author itning
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    private static final String FIRST_CLASS = "1";

    @BindView(R.id.btn_custom)
    AppCompatButton btnCustom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChangeUtil.changeTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        int nowThemeColorAccent = ThemeChangeUtil.getNowThemeColorAccent(this);
        btnCustom.setBackgroundTintList(ColorStateList.valueOf(nowThemeColorAccent));
        initData();
    }

    /**
     * 加载专业数据
     */
    private void initData() {
        if ("".equals(App.sharedPreferences.getString(FIRST_CLASS, ""))) {
            App.sharedPreferences.edit()
                    .putString("1", "08:20-09:50")
                    .putString("2", "10:05-11:35")
                    .putString("3", "12:55-14:25")
                    .putString("4", "14:40-16:10")
                    .putString("5", "17:30-20:00")
                    .apply();
        }
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    /**
     * 自定义课程
     */
    @OnClick(R.id.btn_custom)
    public void onCustomBtnClicked() {
        startActivity(new Intent(this, CustomActivity.class));
        finish();
    }
}
