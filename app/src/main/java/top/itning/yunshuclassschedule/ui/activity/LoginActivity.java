package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

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
 * 登陆
 *
 * @author itning
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private long firstPressedTime;

    @BindView(R.id.tl_username)
    TextInputLayout tlUsername;
    @BindView(R.id.tl_password)
    TextInputLayout tlPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.pb)
    ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case END_CHECK_CLASS_SCHEDULE_UPDATE: {
                pb.setVisibility(View.GONE);
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            }
            default:
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstPressedTime < ConstantPool.Int.EXIT_DELAY.get()) {
            finish();
        } else {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            firstPressedTime = System.currentTimeMillis();
        }
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

    @OnClick(R.id.btn_login)
    public void onLoginBtnClicked() {
        String s = tlUsername.getEditText().getText().toString().trim();
        if ("".equals(s)) {
            Toast.makeText(this, "输入", Toast.LENGTH_LONG).show();
            return;
        }
        App.sharedPreferences.edit()
                .putString(ConstantPool.Str.USER_USERNAME.get(), s)
                .putString(ConstantPool.Str.USER_CLASS_ID.get(), "2016010103")
                .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                .apply();
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.START_CHECK_CLASS_SCHEDULE_UPDATE));
        pb.setVisibility(View.VISIBLE);
    }
}
