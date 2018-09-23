package top.itning.yunshuclassschedule.ui.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.DataEntity;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.DateUtils;
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
    private static final int SCAN_CODE_REQUEST_CODE = 3;
    private static final int QR_CODE_REQUEST_CODE = 4;

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
                importFile();
                break;
            case R.id.tv_import_qr_code:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, QR_CODE_REQUEST_CODE);
                    break;
                }
                startScanQrCode();
                break;
            case R.id.tv_export_file:
                exportFile();
                break;
            case R.id.tv_export_qr_code:
                Toast.makeText(this, "程序员正在思考", Toast.LENGTH_LONG).show();
                break;
            default:
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case QR_CODE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        startScanQrCode();
                    }
                }
                break;
            }
            default:
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 导出文件
     */
    private void exportFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application / octet-stream");
        String fileName = "云舒课表课程数据" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINESE).format(new Date()) + ".json";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        try {
            startActivityForResult(intent, WRITE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "没有找到文件管理APP", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 导入文件
     */
    private void importFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application / octet-stream");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择课程数据文件进行导入"), FILE_SELECT_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "没有找到文件管理APP", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开启扫描二维码
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    private void startScanQrCode() {
        Intent intent = new Intent(ShareActivity.this, ScanCodeActivity.class);
        startActivityForResult(intent, SCAN_CODE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE: {
                if (resultCode == RESULT_OK) {
                    doImportFile(data);
                }
                break;
            }
            case WRITE_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    doExportFile(data);
                }
                break;
            }
            case SCAN_CODE_REQUEST_CODE: {
                //处理扫描结果（在界面上显示）
                if (null != data) {
                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        Log.d(TAG, "bundle is null");
                        return;
                    }
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                    } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                        Toast.makeText(this, "解析二维码失败", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
            default:
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 导出文件
     *
     * @param data {@link Intent}
     */
    private void doExportFile(Intent data) {
        Uri uri = data.getData();
        if (uri == null) {
            Toast.makeText(this, "导出失败", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "File Uri: " + uri.toString());
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
            DataEntity dataEntity = new DataEntity((App) getApplication());
            Gson gson = new Gson();
            byte[] bytes = gson.toJson(dataEntity).getBytes();
            bufferedOutputStream.write(bytes, 0, bytes.length);
            bufferedOutputStream.flush();
            Toast.makeText(this, "导出成功", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, " ", e);
            Toast.makeText(this, "导出失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 导入文件
     *
     * @param data {@link Intent}
     */
    private void doImportFile(Intent data) {
        Uri uri = data.getData();
        if (uri == null) {
            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "File Uri: " + uri.toString());
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             InputStreamReader i = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(i)) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            Gson gson = new Gson();
            DataEntity dataEntity = gson.fromJson(stringBuilder.toString(), DataEntity.class);
            List<ClassSchedule> classScheduleList = dataEntity.getClassScheduleList();
            List<String> timeList = dataEntity.getTimeList();
            if (classScheduleList == null || classScheduleList.isEmpty() || timeList == null || timeList.isEmpty() || timeList.size() != 5) {
                Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("即将导入课程数据，这会将原有课程信息清空，确定导入吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        TreeMap<String, String> timeMap = new TreeMap<>();
                        timeMap.put("1", timeList.get(0));
                        timeMap.put("2", timeList.get(1));
                        timeMap.put("3", timeList.get(2));
                        timeMap.put("4", timeList.get(3));
                        timeMap.put("5", timeList.get(4));
                        if (!DateUtils.isDataLegitimate(timeMap, this)) {
                            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (App.sharedPreferences.edit()
                                .putString("1", timeList.get(0))
                                .putString("2", timeList.get(1))
                                .putString("3", timeList.get(2))
                                .putString("4", timeList.get(3))
                                .putString("5", timeList.get(4))
                                .commit()) {
                            DateUtils.refreshTimeList();
                        } else {
                            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
                            return;
                        }
                        ClassScheduleDao classScheduleDao = ((App) getApplication()).getDaoSession().getClassScheduleDao();
                        classScheduleDao.deleteAll();
                        for (ClassSchedule classSchedule : classScheduleList) {
                            classScheduleDao.insert(classSchedule);
                        }
                        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.TIME_TICK_CHANGE, ""));
                        if (classScheduleList.size() == classScheduleDao.count()) {
                            Toast.makeText(this, "导入成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "导入失败", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, " ", e);
            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
        }
    }
}
