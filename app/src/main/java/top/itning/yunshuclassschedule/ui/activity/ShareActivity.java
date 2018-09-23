package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    private static final String TAG = "ShareActivity";

    private static final int FILE_SELECT_CODE = 1;
    private static final int WRITE_REQUEST_CODE = 2;

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
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "选择课程数据文件进行导入"), FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "没有找到文件管理APP", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_import_qr_code:
                break;
            case R.id.tv_export_file:
                Intent intent1 = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent1.addCategory(Intent.CATEGORY_OPENABLE);
                intent1.setType("application/json");
                intent1.putExtra(Intent.EXTRA_TITLE, "aa.json");
                startActivityForResult(intent1, WRITE_REQUEST_CODE);
                break;
            case R.id.tv_export_qr_code:
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                inputStream));
                        Log.d(TAG, reader.readLine());
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, " ", e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "File Uri: " + uri.toString());
                    break;
                }
                break;
            }
            case WRITE_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    try {
                        getContentResolver().openOutputStream(uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            default:
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
