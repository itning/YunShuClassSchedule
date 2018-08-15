package top.itning.yunshuclassschedule.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.UUID;

import top.itning.yunshuclassschedule.entity.EventEntity;

/**
 * 公共服务
 *
 * @author itning
 */
public class CommonService extends Service {
    private static final String TAG = "CommonService";

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 消息事件
     *
     * @param eventEntity what
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case HTTP_ERROR: {
                Toast.makeText(this, eventEntity.getMsg(), Toast.LENGTH_LONG).show();
                break;
            }
            default:
        }
    }
}
