package top.itning.yunshuclassschedule.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
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

import static top.itning.yunshuclassschedule.ui.activity.ShareActivity.FILE_SELECT_CODE;
import static top.itning.yunshuclassschedule.ui.activity.ShareActivity.TIME_LIST_SIZE;

/**
 * 登陆
 *
 * @author itning
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    private static final String FIRST_CLASS = "1";

    @BindView(R.id.btn_custom)
    AppCompatButton btnCustom;
    @BindView(R.id.btn_share)
    AppCompatButton btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        StatusBarUtil.setTransparent(this);
        initData();
    }

    /**
     * 加载专业数据
     */
    private void initData() {
        if ("".equals(App.sharedPreferences.getString(FIRST_CLASS, ""))) {
            App.sharedPreferences.edit()
                    .putString("1", "08:20-09:50")
                    .putString("2", "10:05-11:35")
                    .putString("3", "12:55-14:25")
                    .putString("4", "14:40-16:10")
                    .putString("5", "17:30-20:00")
                    .apply();
        }
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
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
     * 自定义课程
     */
    @OnClick(R.id.btn_custom)
    public void onCustomBtnClicked() {
        startActivity(new Intent(this, CustomActivity.class));
        finish();
    }

    @OnClick(R.id.btn_share)
    public void onViewClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择课程数据文件进行导入"), FILE_SELECT_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "没有找到文件管理APP", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            doImportFile(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doImportFile(Intent data) {
        Uri uri = data.getData();
        if (uri == null) {
            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "File Uri: " + uri.toString());
        try (InputStreamReader i = new InputStreamReader(Objects.requireNonNull(getContentResolver().openInputStream(uri)));
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
            if (classScheduleList == null || classScheduleList.isEmpty() || timeList == null || timeList.isEmpty() || timeList.size() != TIME_LIST_SIZE) {
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
                            enterMainActivity();
                        } else {
                            Toast.makeText(this, "写入数据库失败,请重试", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, " ", e);
            Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
        }
    }

    private void enterMainActivity() {
        App.sharedPreferences.edit()
                .putString(ConstantPool.Str.APP_CLASS_SCHEDULE_VERSION.get(), "")
                .putString(ConstantPool.Str.USER_USERNAME.get(), "test")
                .putString(ConstantPool.Str.USER_CLASS_ID.get(), "-1")
                .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                .apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
