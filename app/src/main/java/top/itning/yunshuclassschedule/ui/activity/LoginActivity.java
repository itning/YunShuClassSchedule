package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
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

    @BindView(R.id.btn_login)
    AppCompatButton btnLogin;
    @BindView(R.id.acs_profession)
    AppCompatSpinner acsProfession;
    @BindView(R.id.acs_class)
    AppCompatSpinner acsClass;
    private int professSelect;
    private int classSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    private void initView() {
        List<String> professionList = new ArrayList<>();
        professionList.add("软件工程");
        professionList.add("计算机科学与技术");
        professionList.add("电子商务");
        professionList.add("环境设计");
        professionList.add("视觉传达设计");
        professionList.add("电子信息工程");
        professionList.add("自动化");
        ArrayAdapter<String> professionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, professionList);
        professionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        acsProfession.setAdapter(professionAdapter);

        List<String> classList = new ArrayList<>();
        classList.add("软本1601");
        classList.add("软本1602");
        classList.add("软本1603");
        classList.add("软本1604");
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        acsClass.setAdapter(classAdapter);
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case END_CHECK_CLASS_SCHEDULE_UPDATE: {
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

    @OnItemSelected(value = {R.id.acs_profession, R.id.acs_class})
    public void onItemSelected(AppCompatSpinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.acs_profession: {
                professSelect = position;
                break;
            }
            case R.id.acs_class: {
                classSelect = position;
                break;
            }
            default:
        }
    }

    @OnClick(R.id.btn_login)
    public void onLoginBtnClicked() {
        App.sharedPreferences.edit()
                .putString(ConstantPool.Str.USER_USERNAME.get(), "test")
                .putString(ConstantPool.Str.USER_CLASS_ID.get(), "2016010103")
                .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                .apply();
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.START_CHECK_CLASS_SCHEDULE_UPDATE));
    }
}
