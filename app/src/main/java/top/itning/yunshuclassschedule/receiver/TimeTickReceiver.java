package top.itning.yunshuclassschedule.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;

/**
 * 时间改变广播,包括用户手动设置时间,时区
 *
 * @author itning
 */
public class TimeTickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.TIME_TICK_CHANGE));
    }
}
