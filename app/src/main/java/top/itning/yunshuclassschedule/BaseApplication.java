package top.itning.yunshuclassschedule;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.database.Database;

import top.itning.yunshuclassschedule.entity.DaoMaster;
import top.itning.yunshuclassschedule.entity.DaoSession;

/**
 * 应用基类
 *
 * @author itning
 */
public class BaseApplication extends Application {

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        //EventBus add Index
        EventBus.builder().addIndex(new AppActivityIndex()).installDefaultEventBus();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ConstantPool.Str.DB_NAME.get());
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
