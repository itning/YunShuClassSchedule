package top.itning.yunshuclassschedule.common;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.service.CommonService;
import top.itning.yunshuclassschedule.service.RemindService;

/**
 * Base App Activity
 *
 * @author itning
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, CommonService.class));
        startService(new Intent(this, RemindService.class));
    }

    /**
     * 消息事件
     *
     * @param eventEntity what
     */
    public abstract void onMessageEvent(EventEntity eventEntity);
}
