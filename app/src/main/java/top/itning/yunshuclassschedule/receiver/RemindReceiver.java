package top.itning.yunshuclassschedule.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.EventEntity;

/**
 * 提醒广播
 *
 * @author itning
 */
public class RemindReceiver extends BroadcastReceiver {
    private static final String TAG = "RemindReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "on Receive");
        String type = intent.getStringExtra("type");
        String name = intent.getStringExtra("name");
        String location = intent.getStringExtra("location");
        int section = intent.getIntExtra("section", -1);
        int status = intent.getIntExtra("status", -1);
        if (section == -1 || status == -1) {
            Log.e(TAG, "section or status error ! section:" + section + " status:" + status);
            return;
        }
        if (type == null || name == null || location == null) {
            Log.e(TAG, "null value ! type:" + type + " name:" + name + " location:" + location);
            return;
        }
        if ("phone_mute".equals(type)) {
            if (status == 0) {
                Log.d(TAG, "PHONE_MUTE_OPEN");
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.PHONE_MUTE_OPEN));
            } else {
                Log.d(TAG, "PHONE_MUTE_CANCEL");
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.PHONE_MUTE_CANCEL));
            }
        }
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setSection(section);
        classSchedule.setName(name);
        classSchedule.setLocation(location);
        if ("class_reminder_up".equals(type)) {
            Log.d(TAG, "CLASS_UP_REMIND");
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.CLASS_UP_REMIND, "", classSchedule));
        }
        if ("class_reminder_down".equals(type)) {
            Log.d(TAG, "CLASS_DOWN_REMIND");
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.CLASS_DOWN_REMIND, "", classSchedule));
        }
    }
}
