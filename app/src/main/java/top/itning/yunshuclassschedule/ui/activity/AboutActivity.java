package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 关于
 *
 * @author itning
 */
public class AboutActivity extends BaseActivity {

    private static final String TAG = "AboutActivity";
    @BindView(R.id.cv_href)
    CardView cvHref;
    @BindView(R.id.cv_introduction)
    CardView cvIntroduction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChangeUtil.changeSettingTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    private void initView() {
        Log.d(TAG, "init view");
        //设置返回箭头
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("关于");
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {

    }

    @OnClick(R.id.cv_href)
    public void onCvHrefClicked() {
        Uri uri = Uri.parse("https://github.com/itning/YunShuClassSchedule");
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    @OnClick(R.id.cv_introduction)
    public void onIntroductionClicked() {
        startActivity(new Intent(this, MoneyActivity.class));
    }
}
