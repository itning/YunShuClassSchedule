package top.itning.yunshuclassschedule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import org.greenrobot.eventbus.EventBus

import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.ClassSchedule
import top.itning.yunshuclassschedule.entity.EventEntity

/**
 * 提醒广播
 *
 * @author itning
 */
class RemindReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "on Receive")
        val type = intent.getStringExtra("type")
        val name = intent.getStringExtra("name")
        val location = intent.getStringExtra("location")
        val section = intent.getIntExtra("section", -1)
        val status = intent.getIntExtra("status", -1)
        val week = intent.getIntExtra("week", -1)
        if (section == -1 || status == -1 || week == -1) {
            Log.e(TAG, "section or status , week error ! section:$section status:$status week:$week")
            return
        }
        if (type == null || name == null || location == null) {
            Log.e(TAG, "null value ! type:$type name:$name location:$location")
            return
        }
        Log.d(TAG, "get data: type->$type name->$name location->$location section->$section status->$status week->$week")
        if (PHONE_MUTE == type) {
            if (status == 0) {
                Log.d(TAG, "PHONE_MUTE_OPEN")
                EventBus.getDefault().post(EventEntity(ConstantPool.Int.PHONE_MUTE_OPEN))
            } else {
                Log.d(TAG, "PHONE_MUTE_CANCEL")
                EventBus.getDefault().post(EventEntity(ConstantPool.Int.PHONE_MUTE_CANCEL))
            }
        }
        val classSchedule = ClassSchedule()
        classSchedule.section = section
        classSchedule.name = name
        classSchedule.location = location
        if (CLASS_REMINDER_UP == type) {
            Log.d(TAG, "CLASS_UP_REMIND")
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.CLASS_UP_REMIND, "", classSchedule))
        }
        if (CLASS_REMINDER_DOWN == type) {
            Log.d(TAG, "CLASS_DOWN_REMIND")
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.CLASS_DOWN_REMIND, "", classSchedule))
        }
    }

    companion object {
        private const val TAG = "RemindReceiver"
        private const val PHONE_MUTE = "phone_mute"
        private const val CLASS_REMINDER_UP = "class_reminder_up"
        private const val CLASS_REMINDER_DOWN = "class_reminder_down"
    }
}
