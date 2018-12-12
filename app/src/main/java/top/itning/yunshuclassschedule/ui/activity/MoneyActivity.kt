package top.itning.yunshuclassschedule.ui.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.BaseActivity
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.net.URISyntaxException

/**
 * @author itning
 */
class MoneyActivity : BaseActivity() {
    @BindView(R.id.cv_z)
    lateinit var cvZ: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeChangeUtil.changeTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money)
        ButterKnife.bind(this)
        EventBus.getDefault().register(this)
        initView()
    }

    private fun initView() {
        Log.d(TAG, "init view")
        //设置返回箭头
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.title = "捐赠"
        }
        AlertDialog.Builder(this)
                .setTitle("谢谢")
                .setMessage("您的支持是我最大的动力(*^_^*)")
                .setPositiveButton("立即捐赠") { _, _ -> openPay() }
                .setNegativeButton("晓得啦", null)
                .show()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(eventEntity: EventEntity) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @OnClick(R.id.cv_z)
    fun onQRCodeClicked() {
        openPay()
    }

    private fun openPay() {
        val s = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX04259Z8PDL9XBQG5PDF%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"
        try {
            val intent = Intent.parseUri(s, Intent.URI_INTENT_SCHEME)
            startActivity(intent)
        } catch (e: URISyntaxException) {
            Log.e(TAG, " ", e)
            Toast.makeText(this, "没有找到支付宝APP", Toast.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, " ", e)
            Toast.makeText(this, "没有找到支付宝APP", Toast.LENGTH_SHORT).show()
        }

    }

    companion object {
        private const val TAG = "MoneyActivity"
    }
}
