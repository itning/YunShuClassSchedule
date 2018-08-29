package top.itning.yunshuclassschedule.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.BaseActivity;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.ClassEntity;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.entity.Profession;
import top.itning.yunshuclassschedule.http.ClassScheduleMetaData;
import top.itning.yunshuclassschedule.service.DataDownloadService;
import top.itning.yunshuclassschedule.util.HttpUtils;

/**
 * 登陆
 *
 * @author itning
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    /**
     * 加载登陆班级数据
     */
    private static final String LOADING_CLASS_DATA = "1";

    @BindView(R.id.btn_login)
    AppCompatButton btnLogin;
    @BindView(R.id.btn_refresh)
    AppCompatButton btnRefresh;
    @BindView(R.id.acs_profession)
    AppCompatSpinner acsProfession;
    @BindView(R.id.acs_class)
    AppCompatSpinner acsClass;
    private int professSelect;
    private int classSelect;
    private List<Profession> professionList;
    private final ClassScheduleMetaData classScheduleMetaData = HttpUtils.getRetrofit().create(ClassScheduleMetaData.class);
    private List<ClassEntity> classEntityList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        progressDialog = new ProgressDialog(this);
        startService(new Intent(this, DataDownloadService.class));
        initData();
    }

    /**
     * 加载专业数据
     */
    private void initData() {
        changeLoadingState(true, "加载专业信息");
        classScheduleMetaData.getProfession().enqueue(new Callback<List<Profession>>() {
            @Override
            public void onResponse(@NonNull Call<List<Profession>> call, @NonNull Response<List<Profession>> response) {
                professionList = response.body();
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.LOGIN_LOADING_PROFESSION_DATA));
            }

            @Override
            public void onFailure(@NonNull Call<List<Profession>> call, @NonNull Throwable t) {
                professionList = new ArrayList<>();
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.LOGIN_LOADING_PROFESSION_DATA));
            }
        });
    }

    /**
     * 加载班级数据
     */
    private void loadingClassData() {
        classScheduleMetaData.getClassInfo(professionList.get(professSelect).getId()).enqueue(new Callback<List<ClassEntity>>() {
            @Override
            public void onResponse(@NonNull Call<List<ClassEntity>> call, @NonNull Response<List<ClassEntity>> response) {
                classEntityList = response.body();
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.LOGIN_LOADING_CLASS_DATA, "0"));
            }

            @Override
            public void onFailure(@NonNull Call<List<ClassEntity>> call, @NonNull Throwable t) {
                classEntityList = new ArrayList<>();
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.LOGIN_LOADING_CLASS_DATA, "0"));
            }
        });
    }

    /**
     * 加载班级下拉框
     */
    private void initClassSpinner() {
        if (classEntityList.isEmpty()) {
            Toast.makeText(this, "该专业下没有班级数据", Toast.LENGTH_LONG).show();
            btnLogin.setVisibility(View.INVISIBLE);
            acsClass.setVisibility(View.INVISIBLE);
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            acsClass.setVisibility(View.VISIBLE);
        }
        List<String> classList = new ArrayList<>();
        for (ClassEntity classEntity : classEntityList) {
            classList.add(classEntity.getName());
        }
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        acsClass.setAdapter(classAdapter);
    }

    /**
     * 加载专业下拉框
     */
    private void initProfessionSpinner() {
        if (professionList.isEmpty()) {
            Toast.makeText(this, "专业数据加载失败", Toast.LENGTH_LONG).show();
            btnLogin.setVisibility(View.INVISIBLE);
            acsProfession.setVisibility(View.INVISIBLE);
            acsClass.setVisibility(View.INVISIBLE);
            btnRefresh.setVisibility(View.VISIBLE);
        } else {
            btnRefresh.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            acsProfession.setVisibility(View.VISIBLE);
            acsClass.setVisibility(View.VISIBLE);
        }
        List<String> professionNameList = new ArrayList<>();
        for (Profession profession : professionList) {
            professionNameList.add(profession.getName());
        }
        ArrayAdapter<String> professionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, professionNameList);
        professionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        acsProfession.setAdapter(professionAdapter);
    }

    /**
     * 更改加载
     *
     * @param loading 是否加载
     * @param msg     消息
     */
    private void changeLoadingState(boolean loading, String msg) {
        progressDialog.setTitle("加载中");
        progressDialog.setMessage(msg);
        // 能够返回
        progressDialog.setCancelable(false);
        // 点击外部返回
        progressDialog.setCanceledOnTouchOutside(false);
        if (loading) {
            progressDialog.show();
        } else {
            progressDialog.cancel();
        }
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case END_CHECK_CLASS_SCHEDULE_UPDATE: {
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.DESTROY_ACTIVITY));
                changeLoadingState(false, "登陆中");
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            }
            case LOGIN_LOADING_PROFESSION_DATA: {
                initProfessionSpinner();
                changeLoadingState(false, "加载专业信息");
                break;
            }
            case LOGIN_LOADING_CLASS_DATA: {
                if (LOADING_CLASS_DATA.equals(eventEntity.getMsg())) {
                    changeLoadingState(true, "加载班级信息");
                    loadingClassData();
                } else {
                    initClassSpinner();
                    changeLoadingState(false, "加载班级信息");
                }
                break;
            }
            default:
        }
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
     * 下拉框选择监听
     *
     * @param spinner  {@link AppCompatSpinner}
     * @param position 定位
     */
    @OnItemSelected(value = {R.id.acs_profession, R.id.acs_class})
    public void onItemSelected(AppCompatSpinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.acs_profession: {
                professSelect = position;
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.LOGIN_LOADING_CLASS_DATA, LOADING_CLASS_DATA));
                break;
            }
            case R.id.acs_class: {
                classSelect = position;
                break;
            }
            default:
        }
    }

    /**
     * 登录按钮按下监听
     */
    @OnClick(R.id.btn_login)
    public void onLoginBtnClicked() {
        changeLoadingState(true, "登陆中");
        App.sharedPreferences.edit()
                .putString(ConstantPool.Str.USER_USERNAME.get(), "test")
                .putString(ConstantPool.Str.USER_CLASS_ID.get(), classEntityList.get(classSelect).getId())
                .putBoolean(ConstantPool.Str.FIRST_IN_APP.get(), false)
                .apply();
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.START_CHECK_CLASS_SCHEDULE_UPDATE));
    }

    /**
     * 刷新按钮
     */
    @OnClick(R.id.btn_refresh)
    public void onRefreshBtnClicked() {
        this.initData();
    }
}
