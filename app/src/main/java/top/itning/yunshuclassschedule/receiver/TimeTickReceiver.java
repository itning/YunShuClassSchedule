package top.itning.yunshuclassschedule.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;

/**
 * 时间改变广播,包括用户手动设置时间,时区
 *
 * @author itning
 */
public class TimeTickReceiver extends BroadcastReceiver {
    private static final String TAG = "TimeTickReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "send time change event");
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.TIME_TICK_CHANGE));
    }
}
