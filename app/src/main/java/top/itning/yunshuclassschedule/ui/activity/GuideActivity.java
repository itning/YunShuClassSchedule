package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;

/**
 * 引导页
 *
 * @author itning
 */
public class GuideActivity extends AppCompatActivity {

    private static final String TAG = "GuideActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        new Handler().postDelayed(this::enterMainActivity, 2000);
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
     * 进入主Activity
     */
    private void enterMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
