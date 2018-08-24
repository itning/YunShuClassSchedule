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
    private static final String PHONE_MUTE = "phone_mute";
    private static final String CLASS_REMINDER_UP = "class_reminder_up";
    private static final String CLASS_REMINDER_DOWN = "class_reminder_down";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "on Receive");
        String type = intent.getStringExtra("type");
        String name = intent.getStringExtra("name");
        String location = intent.getStringExtra("location");
        int section = intent.getIntExtra("section", -1);
        int status = intent.getIntExtra("status", -1);
        int week = intent.getIntExtra("week", -1);
        if (section == -1 || status == -1 || week == -1) {
            Log.e(TAG, "section or status , week error ! section:" + section + " status:" + status + " week:" + week);
            return;
        }
        if (type == null || name == null || location == null) {
            Log.e(TAG, "null value ! type:" + type + " name:" + name + " location:" + location);
            return;
        }
        Log.d(TAG, "get data: type->" + type + " name->" + name + " location->" + location + " section->" + section + " status->" + status + " week->" + week);
        if (PHONE_MUTE.equals(type)) {
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
        if (CLASS_REMINDER_UP.equals(type)) {
            Log.d(TAG, "CLASS_UP_REMIND");
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.CLASS_UP_REMIND, "", classSchedule));
        }
        if (CLASS_REMINDER_DOWN.equals(type)) {
            Log.d(TAG, "CLASS_DOWN_REMIND");
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.CLASS_DOWN_REMIND, "", classSchedule));
        }
    }
}
