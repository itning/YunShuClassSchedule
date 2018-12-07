package top.itning.yunshuclassschedule.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URISyntaxException;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChangeUtil.changeTheme(this);
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
        new AlertDialog.Builder(this)
                .setTitle("谢谢")
                .setMessage("您的支持是我最大的动力(*^_^*)")
                .setPositiveButton("立即捐赠", (dialog, which) -> openPay())
                .setNegativeButton("晓得啦", null)
                .show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.cv_z)
    public void onQRCodeClicked(View view) {
        openPay();
    }

    private void openPay() {
        String s = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX04259Z8PDL9XBQG5PDF%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
        try {
            Intent intent = Intent.parseUri(s, Intent.URI_INTENT_SCHEME);
            startActivity(intent);
        } catch (URISyntaxException | ActivityNotFoundException e) {
            Log.e(TAG, " ", e);
            Toast.makeText(this, "没有找到支付宝APP", Toast.LENGTH_SHORT).show();
        }
    }
}
