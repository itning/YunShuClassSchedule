package top.itning.yunshuclassschedule.ui.fragment.checkscore;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.entity.Hash;
import top.itning.yunshuclassschedule.entity.Score;
import top.itning.yunshuclassschedule.util.ImageHash;
import top.itning.yunshuclassschedule.util.NetWorkUtils;

/**
 * 登陆
 *
 * @author itning
 */
public class CheckScoreLoginFragment extends Fragment {
    private static final String TAG = "CheckScoreLoginFragment";

    private static final int NUMBER_OF_COPIES = 6;
    private static final int END_NUMBER = 9;
    private static final int START_ASCII = 97;
    private static final int END_ASCII = 122;
    private static final String WIFI_NAME = "\"HXGNET\"";
    private static final String UNKNOWN_SSID = "<unknown ssid>";
    private static final String CODE_ERROR_STR = "验证码有误";
    private static final String NAME_ERROR_STR = "你的用户名不存在";
    private static final String PWD_ERROR_STR = "密码不正确";
    private static final String REMEMBER_ID = "remember_id";
    private static final String REMEMBER_NAME = "remember_name";
    private static final String REMEMBER_STATUS = "remember_id_name";

    @BindView(R.id.et_name)
    TextInputEditText etName;
    @BindView(R.id.tl_name)
    TextInputLayout tlName;
    @BindView(R.id.et_id)
    TextInputEditText etId;
    @BindView(R.id.tl_id)
    TextInputLayout tlId;
    @BindView(R.id.btn_login)
    AppCompatButton btnLogin;
    @BindView(R.id.checkBox)
    AppCompatCheckBox checkBox;
    Unbinder unbinder;
    private Map<String, String> cookies;
    private String name;
    private String id;
    private ProgressDialog progressDialog;
    private String code;
    private Bitmap bitmap;
    private WifiManager wifiMgr;
    private AlertDialog alertDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiMgr = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(1);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "on Destroy");
        if (cookies != null) {
            cookies.clear();
            cookies = null;
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(Integer type) {
        switch (type) {
            case 1: {
                initLibData();
                break;
            }
            case 2: {
                getCookieAndImg();
                break;
            }
            case 3: {
                if (code != null && cookies != null) {
                    EventBus.getDefault().post(new EventEntity(ConstantPool.Int.SCORE_LOGIN_MSG, "进行登陆..."));
                    try {
                        startLogin(code, name, id);
                    } catch (IOException e) {
                        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "登陆失败:" + e.getMessage()));
                        Log.e(TAG, " ", e);
                    } finally {
                        progressDialog.cancel();
                    }
                }
                break;
            }
            default:
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case GET_COOKIE_AND_IMAGE_OK: {
                Toast.makeText(requireContext(), "服务器连接成功", Toast.LENGTH_SHORT).show();
                long startTime = System.currentTimeMillis();
                code = startCompared(bitmap);
                long endTime = System.currentTimeMillis();
                Toast.makeText(requireContext(), "验证码识别用时:" + (endTime - startTime) + "ms", Toast.LENGTH_LONG).show();
                progressDialog.cancel();
                break;
            }
            case GET_COOKIE_AND_IMAGE_FAILED: {
                new AlertDialog.Builder(requireContext())
                        .setTitle("服务器连接失败")
                        .setMessage("请检查HXGNET网络正常连接,并且已经登陆")
                        .setPositiveButton("重新连接", (a, b) -> {
                            EventBus.getDefault().post(2);
                            progressDialog.show();
                        })
                        .setNegativeButton("确定", null)
                        .show();
                break;
            }
            case RE_LOGIN_SCORE: {
                progressDialog.setTitle("连接成绩查询服务器");
                progressDialog.setMessage("获取验证码和Cookie信息");
                progressDialog.show();
                if (cookies != null) {
                    cookies.clear();
                    cookies = null;
                }
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                EventBus.getDefault().post(2);
                break;
            }
            case SCORE_LOGIN_MSG: {
                progressDialog.setMessage(eventEntity.getMsg());
                break;
            }
            default:
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_score_login, container, false);
        unbinder = ButterKnife.bind(this, view);
        etId.setText(App.sharedPreferences.getString(REMEMBER_ID, ""));
        etName.setText(App.sharedPreferences.getString(REMEMBER_NAME, ""));
        checkBox.setChecked(App.sharedPreferences.getBoolean(REMEMBER_STATUS, false));
        return view;
    }

    /**
     * 开始连接服务器
     */
    private void startConnectionServer() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(requireContext());
        }
        progressDialog.setTitle("连接成绩查询服务器");
        progressDialog.setMessage("获取验证码和Cookie信息");
        // 能够返回
        progressDialog.setCancelable(false);
        // 点击外部返回
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        EventBus.getDefault().post(2);
    }

    /**
     * 检查所需网络环境是否满足
     *
     * @return 满足返回真
     */
    @CheckResult
    private boolean netStatusOk() {
        if (NetWorkUtils.getConnectedType(requireContext()) != ConnectivityManager.TYPE_WIFI) {
            showAlert();
            return false;
        }
        if (wifiMgr != null) {
            int wifiState = wifiMgr.getWifiState();
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                WifiInfo info = wifiMgr.getConnectionInfo();
                String ssid = info.getSSID();
                Log.d(TAG, "get Wifi SSID:" + ssid);
                if (UNKNOWN_SSID.equals(ssid)) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        new AlertDialog.Builder(requireContext()).setTitle("权限说明")
                                .setMessage("由于Android系统限制,您必须授予定位权限才可以进行成绩查询")
                                .setPositiveButton("授予权限", (dialog, which) -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1))
                                .setNegativeButton("不用了", null)
                                .show();
                    } else {
                        if (NetWorkUtils.isGPSEnabled(requireContext())) {
                            return true;
                        } else {
                            new AlertDialog.Builder(requireContext()).setTitle("权限说明")
                                    .setMessage("由于Android系统限制,您必须打开GPS才可以进行成绩查询")
                                    .setPositiveButton("打开GPS", (dialog, which) -> startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)))
                                    .setNegativeButton("不用了", null)
                                    .show();
                        }
                    }
                    return false;
                }
                if (WIFI_NAME.equals(ssid)) {
                    return true;
                } else {
                    showAlert();
                    return false;
                }
            } else {
                showAlert();
                return false;
            }
        }
        return false;
    }

    /**
     * 显示Dialog
     */
    private void showAlert() {
        alertDialog = new AlertDialog.Builder(requireContext()).setTitle("需要连接WIFI")
                .setMessage("需要连接名为HXGNET的WIFI并且登陆后才能进行成绩查询")
                .setPositiveButton("打开设置页面", (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("不用了", null)
                .show();
    }

    /**
     * 开始裁剪图片并进行比对
     *
     * @param sourceBitmap {@link Bitmap}
     * @return 对比出来的字符
     */
    @CheckResult
    private String startCompared(@NonNull Bitmap sourceBitmap) {
        StringBuilder stringBuilder = new StringBuilder();
        int sWidth = sourceBitmap.getWidth() / 6;
        for (int i = 0; i < NUMBER_OF_COPIES; i++) {
            Bitmap bitmap = Bitmap.createBitmap(sourceBitmap, sWidth * i, 0, sourceBitmap.getWidth() / 6, sourceBitmap.getHeight());
            String hash1 = ImageHash.calculateFingerPrint(bitmap);
            DaoSession daoSession = ((App) requireActivity().getApplication()).getDaoSession();
            Hash hash = daoSession.getHashDao().load(hash1);
            if (hash != null) {
                stringBuilder.append(hash.getName());
            }
            bitmap.recycle();
        }
        sourceBitmap.recycle();
        return stringBuilder.toString();
    }

    /**
     * 初始化数据库数据
     */
    private void initLibData() {
        DaoSession daoSession = ((App) requireActivity().getApplication()).getDaoSession();
        if (daoSession.getHashDao().count() == 0) {
            AssetManager assets = requireActivity().getAssets();
            insert(assets);
        }
    }

    /**
     * 插入字符
     *
     * @param assets {@link AssetManager}
     */
    private void insert(AssetManager assets) {
        try {
            for (int i = START_ASCII; i <= END_ASCII; i++) {
                char c = (char) (i);
                String hash1 = ImageHash.calculateFingerPrint(BitmapFactory.decodeStream(assets.open(c + ".bmp")));
                doInsert(hash1, c + "");
            }
            for (int i = 0; i <= END_NUMBER; i++) {
                String hash2 = ImageHash.calculateFingerPrint(BitmapFactory.decodeStream(assets.open(i + ".bmp")));
                doInsert(hash2, i + "");
            }
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "数据初始化完成"));
        } catch (IOException e) {
            Log.e(TAG, " ", e);
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "初始化失败:" + e.getMessage()));
        }
    }

    /**
     * 将数据插入到数据库
     *
     * @param hash 指纹
     * @param name 对应字符
     */
    private void doInsert(@NonNull String hash, @NonNull String name) {
        DaoSession daoSession = ((App) requireActivity().getApplication()).getDaoSession();
        daoSession.getHashDao().insert(new Hash(hash, name));
    }

    /**
     * 获取Cookie和验证码
     */
    private void getCookieAndImg() {
        try {
            Connection.Response responseCode = Jsoup.connect("http://172.16.15.28/include/Code.asp")
                    .method(Connection.Method.GET)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                    .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                    .header("Host", "172.16.15.28")
                    .header("Referer", "http://172.16.15.28/W01_Login.asp")
                    .ignoreContentType(true)
                    .execute();
            cookies = responseCode.cookies();
            bitmap = BitmapFactory.decodeStream(responseCode.bodyStream());
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.GET_COOKIE_AND_IMAGE_OK));
        } catch (IOException e) {
            progressDialog.cancel();
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "获取验证码失败:" + e.getMessage()));
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.GET_COOKIE_AND_IMAGE_FAILED));
            Log.e(TAG, "error : ", e);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "on Resume");
        super.onResume();
        if (bitmap == null || code == null) {
            if (netStatusOk()) {
                startConnectionServer();
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "on Pause");
        if (progressDialog != null) {
            progressDialog.cancel();
        }
        if (alertDialog != null) {
            alertDialog.cancel();
        }
        super.onPause();
    }

    /**
     * 开始登陆
     *
     * @param verifyCode 验证码
     * @param name       姓名
     * @param id         学号
     * @throws IOException IOException
     */
    private void startLogin(@NonNull String verifyCode, @NonNull String name, @NonNull String id) throws IOException {
        Connection.Response response = Jsoup.connect("http://172.16.15.28/F02_Check.asp")
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Host", "172.16.15.28")
                .header("Referer", "http://172.16.15.28/W01_Login.asp")
                .data("name", name)
                .data("B1", "提交")
                .data("pwd", id)
                .data("Verifycode", verifyCode)
                .cookies(cookies)
                .timeout(5000)
                .postDataCharset("GB2312")
                .execute();
        String body = response.charset("gb2312").body();
        int index = body.indexOf("已登陆");
        if (index == -1) {
            Log.w(TAG, "登陆失败 " + response.statusCode());
            if (body.contains(CODE_ERROR_STR)) {
                Log.w(TAG, "验证码有误 " + verifyCode);
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "验证码有误,请重新登陆"));
            } else if (body.contains(NAME_ERROR_STR)) {
                Log.w(TAG, "该学生未录入");
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "你的用户名不存在,请重新登陆"));
            } else if (body.contains(PWD_ERROR_STR)) {
                Log.w(TAG, "学号输入错误");
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "学号不正确,请重新登陆"));
            } else {
                Log.e(TAG, "未知错误 " + body);
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "未知错误,请尝试重新登陆"));
            }
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.RE_LOGIN_SCORE));
            return;
        }
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.SCORE_LOGIN_MSG, "登陆成功 " + response.statusCode()));
        Connection.Response response2 = Jsoup.connect("http://172.16.15.28/W33_SearchCj.asp")
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Host", "172.16.15.28")
                .header("Referer", "http://172.16.15.28/W05_Main.asp")
                .cookies(cookies)
                .timeout(5000)
                .postDataCharset("GB2312")
                .execute();
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.SCORE_LOGIN_MSG, "成绩查询 " + response2.statusCode()));
        Document document = response2.charset("gb2312").parse();
        Elements elements = document.getElementsByAttribute("bgcolor");
        ArrayList<Score> scoreList = new ArrayList<>();
        boolean first = true;
        for (Element element : elements) {
            if (first) {
                first = false;
                continue;
            }
            String[] s = element.text().split(" ");
            if (s.length == 5) {
                Score score = new Score();
                score.setId(s[0]);
                score.setSemester(s[1]);
                score.setName(s[2]);
                score.setGrade(s[3]);
                score.setCredit(s[4]);
                scoreList.add(score);
            }
        }
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.SCORE_LOGIN_SUCCESS, "", scoreList));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_login)
    public void onLoginBtnClicked() {
        if (!netStatusOk()) {
            return;
        }
        tlId.setError(null);
        tlName.setError(null);
        if ("".equals(etName.getText().toString())) {
            tlName.setError("请输入姓名");
            return;
        }
        if ("".equals(etId.getText().toString())) {
            tlId.setError("请输入学号");
            return;
        }
        this.name = etName.getText().toString();
        this.id = etId.getText().toString();
        boolean remember = checkBox.isChecked();
        App.sharedPreferences.edit().putBoolean(REMEMBER_STATUS, remember).apply();
        if (remember) {
            App.sharedPreferences.edit()
                    .putString(REMEMBER_ID, this.id)
                    .putString(REMEMBER_NAME, this.name)
                    .apply();
        } else {
            App.sharedPreferences.edit()
                    .remove(REMEMBER_ID)
                    .remove(REMEMBER_NAME)
                    .apply();
        }
        if (progressDialog != null) {
            progressDialog.setTitle("登陆中");
            progressDialog.show();
        }
        EventBus.getDefault().post(3);
    }
}
