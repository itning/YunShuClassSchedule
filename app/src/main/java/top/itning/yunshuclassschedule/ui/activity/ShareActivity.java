package top.itning.yunshuclassschedule.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatTextView;
import android.view.MenuItem;
import android.view.View;

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
 * 课程表分享活动
 *
 * @author itning
 */
public class ShareActivity extends BaseActivity {
    @BindView(R.id.tv_import_title)
    AppCompatTextView tvImportTitle;
    @BindView(R.id.tv_import_file)
    AppCompatTextView tvImportFile;
    @BindView(R.id.tv_import_qr_code)
    AppCompatTextView tvImportQrCode;
    @BindView(R.id.tv_export_title)
    AppCompatTextView tvExportTitle;
    @BindView(R.id.tv_export_file)
    AppCompatTextView tvExportFile;
    @BindView(R.id.tv_export_qr_code)
    AppCompatTextView tvExportQrCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeChangeUtil.simpleSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("分享课程表");
        }
        int nowThemeColorAccent = ThemeChangeUtil.getNowThemeColorAccent(this);
        tvImportTitle.setTextColor(nowThemeColorAccent);
        tvExportTitle.setTextColor(nowThemeColorAccent);
        ThemeChangeUtil.setTextViewsColorByTheme(this, tvImportFile, tvImportQrCode, tvExportFile, tvExportQrCode);
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

    @OnClick({R.id.tv_import_file, R.id.tv_import_qr_code, R.id.tv_export_file, R.id.tv_export_qr_code})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_import_file:
                break;
            case R.id.tv_import_qr_code:
                break;
            case R.id.tv_export_file:
                break;
            case R.id.tv_export_qr_code:
                break;
            default:
        }
    }
}
