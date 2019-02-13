package top.itning.yunshuclassschedule.common

import android.content.Intent
import android.os.Bundle

import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.service.CommonService
import top.itning.yunshuclassschedule.service.RemindService
import top.itning.yunshuclassschedule.service.TodayWidgetService

/**
 * Base App Activity
 *
 * @author itning
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, CommonService::class.java))
        startService(Intent(this, RemindService::class.java))
        startService(Intent(this, TodayWidgetService::class.java))
    }

    /**
     * 消息事件
     *
     * @param eventEntity what
     */
    abstract fun onMessageEvent(eventEntity: EventEntity)
}
