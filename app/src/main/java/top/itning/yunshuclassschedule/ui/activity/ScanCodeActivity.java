package top.itning.yunshuclassschedule.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.ImageUtils;

/**
 * Scan Code Activity
 *
 * @author itning
 */
public class ScanCodeActivity extends BaseActivity {
    private static final int REQUEST_IMAGE = 1;
    /**
     * flash light state flag
     */
    public static boolean isOpen = false;
    @BindView(R.id.btn_flash_light)
    AppCompatButton btnFlashLight;
    @BindView(R.id.btn_select)
    AppCompatButton btnSelect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initFlashLightBtn();
        btnSelect.getBackground().setAlpha(100);
        //执行扫面Fragment的初始化操作
        CaptureFragment captureFragment = new CaptureFragment();
        //为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.fragment_capture_custom);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        //替换我们的扫描控件
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_custom_container, captureFragment).commit();
    }

    private void initFlashLightBtn() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            btnFlashLight.setVisibility(View.GONE);
            return;
        }
        btnFlashLight.getBackground().setAlpha(100);
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

    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            ScanCodeActivity.this.setResult(RESULT_OK, resultIntent);
            ScanCodeActivity.this.finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            ScanCodeActivity.this.setResult(RESULT_OK, resultIntent);
            ScanCodeActivity.this.finish();
        }
    };

    @OnClick(R.id.btn_flash_light)
    public void onFlashBtnClicked() {
        if (!isOpen) {
            CodeUtils.isLightEnable(true);
            isOpen = true;
            btnFlashLight.setText("关灯");
        } else {
            CodeUtils.isLightEnable(false);
            isOpen = false;
            btnFlashLight.setText("开灯");
        }
    }

    @OnClick(R.id.btn_select)
    public void onSelectBtnClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                CodeUtils.analyzeBitmap(ImageUtils.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                    @Override
                    public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                        Intent resultIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
                        bundle.putString(CodeUtils.RESULT_STRING, result);
                        resultIntent.putExtras(bundle);
                        ScanCodeActivity.this.setResult(CodeUtils.RESULT_SUCCESS, resultIntent);
                        ScanCodeActivity.this.finish();
                    }

                    @Override
                    public void onAnalyzeFailed() {
                        Toast.makeText(ScanCodeActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
