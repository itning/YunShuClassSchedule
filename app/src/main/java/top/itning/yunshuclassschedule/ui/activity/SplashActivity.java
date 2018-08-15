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
 * 闪屏页
 *
 * @author itning
 */
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 2000);
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
    public void onBackPressed() {
        //do nothing
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
