package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;

/**
 * 引导页
 *
 * @author itning
 */
public class GuideActivity extends BaseActivity {

    private static final String TAG = "GuideActivity";
    @BindView(R.id.btn_start_login)
    Button btnStartLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Log.d(TAG, "init data");
        //周课表文字大小
        App.sharedPreferences.edit()
                .putFloat(ConstantPool.Str.WEEK_FONT_SIZE.get(), 12)
                .apply();
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

    @Override
    public void onBackPressed() {
        //do nothing
    }

    @OnClick(R.id.btn_start_login)
    public void onStartBtnClicked() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
