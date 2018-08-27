package top.itning.yunshuclassschedule.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * @author itning
 */
public class MoneyActivity extends BaseActivity {

    private static final String TAG = "MoneyActivity";
    @BindView(R.id.cv_z)
    CardView cvZ;
    @BindView(R.id.cv_v)
    CardView cvV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChangeUtil.changeSettingTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money);
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
            supportActionBar.setTitle("捐赠");
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

    @OnClick({R.id.cv_z, R.id.cv_v})
    public void onQRCodeClicked(View view) {
        switch (view.getId()) {
            case R.id.cv_z:
                String s = "intent://platformapi/startapp?saId=10000007&" +
                        "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2Ftsx04810zmikyotlvzoyk80%3F_s" +
                        "%3Dweb-other&_t=1472443966571#Intent;" +
                        "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                try {
                    Intent intent = Intent.parseUri(
                            s,
                            Intent.URI_INTENT_SCHEME
                    );
                    startActivity(intent);
                } catch (URISyntaxException | ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.cv_v:
                break;
            default:
        }
    }
}
