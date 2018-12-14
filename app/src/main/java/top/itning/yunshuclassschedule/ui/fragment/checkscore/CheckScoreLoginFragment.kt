package top.itning.yunshuclassschedule.ui.fragment.checkscore

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jsoup.Connection
import org.jsoup.Jsoup
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.entity.Hash
import top.itning.yunshuclassschedule.entity.Score
import top.itning.yunshuclassschedule.util.ImageHash
import top.itning.yunshuclassschedule.util.NetWorkUtils
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.io.IOException
import java.util.*

/**
 * 登陆
 *
 * @author itning
 */
class CheckScoreLoginFragment : Fragment() {

    @BindView(R.id.et_name)
    lateinit var etName: TextInputEditText
    @BindView(R.id.tl_name)
    lateinit var tlName: TextInputLayout
    @BindView(R.id.et_id)
    lateinit var etId: TextInputEditText
    @BindView(R.id.tl_id)
    lateinit var tlId: TextInputLayout
    @BindView(R.id.btn_login)
    lateinit var btnLogin: AppCompatButton
    @BindView(R.id.checkBox)
    lateinit var checkBox: AppCompatCheckBox
    private var unbinder: Unbinder? = null
    private var cookies: MutableMap<String, String>? = null
    private var name: String? = null
    private var id: String? = null
    private var progressDialog: ProgressDialog? = null
    private var code: String? = null
    private var bitmap: Bitmap? = null
    private var wifiMgr: WifiManager? = null
    private var alertDialog: AlertDialog? = null
    private var gpsAndWifiStatusReceiver: GpsAndWifiStatusReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiMgr = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        gpsAndWifiStatusReceiver = GpsAndWifiStatusReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        requireActivity().registerReceiver(gpsAndWifiStatusReceiver, intentFilter)
        EventBus.getDefault().register(this)
        EventBus.getDefault().post(1)
    }

    override fun onDestroy() {
        Log.d(TAG, "on Destroy")
        if (cookies != null) {
            cookies!!.clear()
            cookies = null
        }
        if (bitmap != null) {
            bitmap!!.recycle()
            bitmap = null
        }
        if (alertDialog != null) {
            alertDialog!!.cancel()
            alertDialog = null
        }
        if (progressDialog != null) {
            progressDialog!!.cancel()
            progressDialog = null
        }
        if (wifiMgr != null) {
            wifiMgr = null
        }
        requireActivity().unregisterReceiver(gpsAndWifiStatusReceiver)
        if (gpsAndWifiStatusReceiver != null) {
            gpsAndWifiStatusReceiver = null
        }
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(type: Int?) {
        when (type) {
            1 -> {
                initLibData()
            }
            2 -> {
                getCookieAndImg()
            }
            3 -> {
                if (code != null && cookies != null) {
                    EventBus.getDefault().post(EventEntity(ConstantPool.Int.SCORE_LOGIN_MSG, "进行登陆..."))
                    try {
                        startLogin(code!!, name, id)
                    } catch (e: IOException) {
                        EventBus.getDefault().post(EventEntity(ConstantPool.Int.HTTP_ERROR, "登陆失败:" + e.message))
                        Log.e(TAG, " ", e)
                    } finally {
                        progressDialog!!.cancel()
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.GET_COOKIE_AND_IMAGE_OK -> {
                Toast.makeText(requireContext(), "服务器连接成功", Toast.LENGTH_SHORT).show()
                val startTime = System.currentTimeMillis()
                code = startCompared(bitmap!!)
                val endTime = System.currentTimeMillis()
                Toast.makeText(requireContext(), "验证码识别用时:" + (endTime - startTime) + "ms", Toast.LENGTH_LONG).show()
                progressDialog!!.cancel()
            }
            ConstantPool.Int.GET_COOKIE_AND_IMAGE_FAILED -> {
                showAlert("服务器连接失败", "请检查HXGNET网络正常连接,并且已经登陆", "重新连接", "确定",
                        DialogInterface.OnClickListener { _, _ ->
                            EventBus.getDefault().post(2)
                            progressDialog!!.show()
                        })
            }
            ConstantPool.Int.RE_LOGIN_SCORE -> {
                progressDialog!!.setTitle("连接成绩查询服务器")
                progressDialog!!.setMessage("获取验证码和Cookie信息")
                progressDialog!!.show()
                if (cookies != null) {
                    cookies!!.clear()
                    cookies = null
                }
                if (bitmap != null) {
                    bitmap!!.recycle()
                    bitmap = null
                }
                EventBus.getDefault().post(2)
            }
            ConstantPool.Int.SCORE_LOGIN_MSG -> {
                progressDialog!!.setMessage(eventEntity.msg)
            }
            ConstantPool.Int.APP_COLOR_CHANGE -> {
                btnLogin.backgroundTintList = ColorStateList.valueOf(ThemeChangeUtil.getNowThemeColorAccent(requireContext()))
            }
            else -> {
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_check_score_login, container, false)
        unbinder = ButterKnife.bind(this, view)
        btnLogin.backgroundTintList = ColorStateList.valueOf(ThemeChangeUtil.getNowThemeColorAccent(requireContext()))
        etId.setText(App.sharedPreferences.getString(REMEMBER_ID, ""))
        etName.setText(App.sharedPreferences.getString(REMEMBER_NAME, ""))
        checkBox.isChecked = App.sharedPreferences.getBoolean(REMEMBER_STATUS, false)
        return view
    }

    /**
     * 开始连接服务器
     */
    private fun startConnectionServer() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(requireContext())
        }
        progressDialog!!.setTitle("连接成绩查询服务器")
        progressDialog!!.setMessage("获取验证码和Cookie信息")
        // 能够返回
        progressDialog!!.setCancelable(false)
        // 点击外部返回
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.show()
        EventBus.getDefault().post(2)
    }

    /**
     * 检查所需网络环境是否满足
     *
     * @return 满足返回真
     */
    @CheckResult
    private fun netStatusOk(): Boolean {
        if (NetWorkUtils.getConnectedType(requireContext()) != ConnectivityManager.TYPE_WIFI) {
            showAlert("需要连接WIFI", "需要连接名为HXGNET的WIFI并且登陆后才能进行成绩查询",
                    "打开设置页面", "不用了", DialogInterface.OnClickListener { _, _ -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) })
            return false
        }
        if (wifiMgr != null) {
            val wifiState = wifiMgr!!.wifiState
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                val info = wifiMgr!!.connectionInfo
                val ssid = info.ssid
                Log.d(TAG, "get Wifi SSID:$ssid")
                if (UNKNOWN_SSID == ssid) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        showAlert("权限说明", "由于Android系统限制,您必须授予定位权限才可以进行成绩查询",
                                "授予权限", "不用了", DialogInterface.OnClickListener { _, _ -> requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1) })
                    } else {
                        if (NetWorkUtils.isGPSEnabled(requireContext())) {
                            return true
                        } else {
                            showAlert("权限说明", "由于Android系统限制,您必须打开GPS才可以进行成绩查询",
                                    "打开GPS", "不用了", DialogInterface.OnClickListener { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
                        }
                    }
                    return false
                }
                return if (WIFI_NAME == ssid) {
                    true
                } else {
                    showAlert("需要连接WIFI", "需要连接名为HXGNET的WIFI并且登陆后才能进行成绩查询",
                            "打开设置页面", "不用了", DialogInterface.OnClickListener { _, _ -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) })
                    false
                }
            } else {
                showAlert("需要连接WIFI", "需要连接名为HXGNET的WIFI并且登陆后才能进行成绩查询",
                        "打开设置页面", "不用了", DialogInterface.OnClickListener { _, _ -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) })
                return false
            }
        }
        return false
    }

    /**
     * 显示Dialog
     */
    private fun showAlert(title: String, message: String, positiveButtonStr: String, negativeButtonStr: String, listener: DialogInterface.OnClickListener) {
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        alertDialog = AlertDialog.Builder(requireContext()).setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonStr, listener)
                .setNegativeButton(negativeButtonStr, null)
                .show()
    }

    /**
     * 开始裁剪图片并进行比对
     *
     * @param sourceBitmap [Bitmap]
     * @return 对比出来的字符
     */
    @CheckResult
    private fun startCompared(@NonNull sourceBitmap: Bitmap): String {
        val stringBuilder = StringBuilder()
        val sWidth = sourceBitmap.width / 6
        for (i in 0 until NUMBER_OF_COPIES) {
            val bitmap = Bitmap.createBitmap(sourceBitmap, sWidth * i, 0, sourceBitmap.width / 6, sourceBitmap.height)
            val hash1 = ImageHash.calculateFingerPrint(bitmap)
            val daoSession = (requireActivity().application as App).daoSession
            val hash = daoSession.hashDao.load(hash1)
            if (hash != null) {
                stringBuilder.append(hash.name)
            }
            bitmap.recycle()
        }
        sourceBitmap.recycle()
        return stringBuilder.toString()
    }

    /**
     * 初始化数据库数据
     */
    private fun initLibData() {
        val daoSession = (requireActivity().application as App).daoSession
        if (daoSession.hashDao.count() == 0L) {
            val assets = requireActivity().assets
            insert(assets)
        }
    }

    /**
     * 插入字符
     *
     * @param assets [AssetManager]
     */
    private fun insert(assets: AssetManager) {
        try {
            for (i in START_ASCII..END_ASCII) {
                val c = i.toChar()
                val hash1 = ImageHash.calculateFingerPrint(BitmapFactory.decodeStream(assets.open("$c.bmp")))
                doInsert(hash1, c + "")
            }
            for (i in 0..END_NUMBER) {
                val hash2 = ImageHash.calculateFingerPrint(BitmapFactory.decodeStream(assets.open(i.toString() + ".bmp")))
                doInsert(hash2, i.toString() + "")
            }
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.HTTP_ERROR, "数据初始化完成"))
        } catch (e: IOException) {
            Log.e(TAG, " ", e)
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.HTTP_ERROR, "初始化失败:" + e.message))
        }

    }

    /**
     * 将数据插入到数据库
     *
     * @param hash 指纹
     * @param name 对应字符
     */
    private fun doInsert(@NonNull hash: String, @NonNull name: String) {
        val daoSession = (requireActivity().application as App).daoSession
        daoSession.hashDao.insert(Hash(hash, name))
    }

    /**
     * 获取Cookie和验证码
     */
    private fun getCookieAndImg() {
        try {
            val responseCode = Jsoup.connect("http://172.16.15.28/include/Code.asp")
                    .method(Connection.Method.GET)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                    .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                    .header("Host", "172.16.15.28")
                    .header("Referer", "http://172.16.15.28/W01_Login.asp")
                    .ignoreContentType(true)
                    .execute()
            cookies = responseCode.cookies()
            bitmap = BitmapFactory.decodeStream(responseCode.bodyStream())
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.GET_COOKIE_AND_IMAGE_OK))
        } catch (e: IOException) {
            progressDialog!!.cancel()
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.HTTP_ERROR, "获取验证码失败:" + e.message))
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.GET_COOKIE_AND_IMAGE_FAILED))
            Log.e(TAG, "error : ", e)
        }

    }

    override fun onResume() {
        Log.d(TAG, "on Resume")
        super.onResume()
        if (bitmap == null || code == null) {
            if (netStatusOk()) {
                startConnectionServer()
            }
        }
    }

    override fun onPause() {
        Log.d(TAG, "on Pause")
        if (progressDialog != null) {
            progressDialog!!.cancel()
        }
        if (alertDialog != null) {
            alertDialog!!.cancel()
        }
        super.onPause()
    }

    /**
     * 开始登陆
     *
     * @param verifyCode 验证码
     * @param name       姓名
     * @param id         学号
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    private fun startLogin(@NonNull verifyCode: String, @NonNull name: String?, @NonNull id: String?) {
        val response = Jsoup.connect("http://172.16.15.28/F02_Check.asp")
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
                .execute()
        val body = response.charset("gb2312").body()
        val index = body.indexOf("已登陆")
        if (index == -1) {
            Log.w(TAG, "登陆失败 " + response.statusCode())
            when {
                body.contains(CODE_ERROR_STR) -> {
                    Log.w(TAG, "验证码有误 $verifyCode")
                    EventBus.getDefault().post(EventEntity(ConstantPool.Int.HTTP_ERROR, "验证码有误,请重新登陆"))
                }
                body.contains(NAME_ERROR_STR) -> {
                    Log.w(TAG, "该学生未录入")
                    EventBus.getDefault().post(EventEntity(ConstantPool.Int.HTTP_ERROR, "你的用户名不存在,请重新登陆"))
                }
                body.contains(PWD_ERROR_STR) -> {
                    Log.w(TAG, "学号输入错误")
                    EventBus.getDefault().post(EventEntity(ConstantPool.Int.HTTP_ERROR, "学号不正确,请重新登陆"))
                }
                else -> {
                    Log.e(TAG, "未知错误 $body")
                    EventBus.getDefault().post(EventEntity(ConstantPool.Int.HTTP_ERROR, "未知错误,请尝试重新登陆"))
                }
            }
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.RE_LOGIN_SCORE))
            return
        }
        EventBus.getDefault().post(EventEntity(ConstantPool.Int.SCORE_LOGIN_MSG, "登陆成功 " + response.statusCode()))
        val response2 = Jsoup.connect("http://172.16.15.28/W33_SearchCj.asp")
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Host", "172.16.15.28")
                .header("Referer", "http://172.16.15.28/W05_Main.asp")
                .cookies(cookies)
                .timeout(5000)
                .postDataCharset("GB2312")
                .execute()
        EventBus.getDefault().post(EventEntity(ConstantPool.Int.SCORE_LOGIN_MSG, "成绩查询 " + response2.statusCode()))
        val document = response2.charset("gb2312").parse()
        val elements = document.getElementsByAttribute("bgcolor")
        val scoreList = ArrayList<Score>()
        var first = true
        for (element in elements) {
            if (first) {
                first = false
                continue
            }
            val s = element.text().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (s.size == 5) {
                val score = Score()
                score.id = s[0]
                score.semester = s[1]
                score.name = s[2]
                score.grade = s[3]
                score.credit = s[4]
                scoreList.add(score)
            }
        }
        EventBus.getDefault().post(EventEntity(ConstantPool.Int.SCORE_LOGIN_SUCCESS, "", scoreList))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }

    @OnClick(R.id.btn_login)
    fun onLoginBtnClicked() {
        if (!netStatusOk()) {
            return
        }
        tlId.error = null
        tlName.error = null
        if ("" == etName.text.toString()) {
            tlName.error = "请输入姓名"
            return
        }
        if ("" == etId.text.toString()) {
            tlId.error = "请输入学号"
            return
        }
        this.name = etName.text.toString()
        this.id = etId.text.toString()
        val remember = checkBox.isChecked
        App.sharedPreferences.edit().putBoolean(REMEMBER_STATUS, remember).apply()
        if (remember) {
            App.sharedPreferences.edit()
                    .putString(REMEMBER_ID, this.id)
                    .putString(REMEMBER_NAME, this.name)
                    .apply()
        } else {
            App.sharedPreferences.edit()
                    .remove(REMEMBER_ID)
                    .remove(REMEMBER_NAME)
                    .apply()
        }
        if (progressDialog != null) {
            progressDialog!!.setTitle("登陆中")
            progressDialog!!.show()
        }
        EventBus.getDefault().post(3)
    }

    /**
     * GPS and Wifi Status Change Receiver
     *
     * @author itning
     */
    private inner class GpsAndWifiStatusReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (alertDialog != null) {
                alertDialog!!.cancel()
                if (netStatusOk()) {
                    startConnectionServer()
                }
            }
        }
    }

    companion object {
        private const val TAG = "CheckScoreLoginFragment"
        private const val NUMBER_OF_COPIES = 6
        private const val END_NUMBER = 9
        private const val START_ASCII = 97
        private const val END_ASCII = 122
        private const val WIFI_NAME = "\"HXGNET\""
        private const val UNKNOWN_SSID = "<unknown ssid>"
        private const val CODE_ERROR_STR = "验证码有误"
        private const val NAME_ERROR_STR = "你的用户名不存在"
        private const val PWD_ERROR_STR = "密码不正确"
        private const val REMEMBER_ID = "remember_id"
        private const val REMEMBER_NAME = "remember_name"
        private const val REMEMBER_STATUS = "remember_id_name"
    }
}
