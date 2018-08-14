package top.itning.yunshuclassschedule;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

/**
 * 应用基类
 *
 * @author itning
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //EventBus add Index
        EventBus.builder().addIndex(new AppActivityIndex()).installDefaultEventBus();
    }
}
