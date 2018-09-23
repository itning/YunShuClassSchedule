package top.itning.yunshuclassschedule.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.database.Database;

import top.itning.yunshuclassschedule.AppActivityIndex;
import top.itning.yunshuclassschedule.entity.DaoMaster;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.util.GlideApp;

/**
 * 应用基类
 *
 * @author itning
 */
public class App extends Application {

    private static final String TAG = "App";

    private DaoSession daoSession;
    public static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        // 程序创建的时候执行
        //EventBus add Index
        EventBus.builder().addIndex(new AppActivityIndex()).installDefaultEventBus();
        //bugly
        CrashReport.initCrashReport(getApplicationContext(), "439037c8de", false);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ConstantPool.Str.DB_NAME.get());
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        sharedPreferences = getSharedPreferences(ConstantPool.Str.SHARED_PREFERENCES_FILENAME.get(), Context.MODE_PRIVATE);

        ZXingLibrary.initDisplayOpinion(this);
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        Log.d(TAG, "onTerminate");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        Log.d(TAG, "onLowMemory");
        GlideApp.get(this).clearMemory();
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        Log.d(TAG, "onTrimMemory:" + level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            GlideApp.get(this).clearMemory();
        }
        GlideApp.get(this).trimMemory(level);
        super.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }


    public DaoSession getDaoSession() {
        return daoSession;
    }
}
