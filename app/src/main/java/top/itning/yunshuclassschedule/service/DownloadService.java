package top.itning.yunshuclassschedule.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.http.DownloadApk;
import top.itning.yunshuclassschedule.util.HttpUtils;
import top.itning.yunshuclassschedule.util.download.progress.AbstractProgressHandler;
import top.itning.yunshuclassschedule.util.download.progress.ProgressHelper;

/**
 * 下载服务
 *
 * @author itning
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    private String apkName;

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

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case START_DOWNLOAD_UPDATE_APK: {
                Log.d(TAG, "start download");
                apkName = eventEntity.getData() + ".apk";
                startDownload(eventEntity.getMsg(), apkName);
                break;
            }
            default:
        }
    }

    private void startDownload(@NonNull String url, @NonNull String fileName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "download")
                .setContentTitle("正在下载更新文件")
                .setContentText("连接服务器")
                .setSmallIcon(this.getApplicationInfo().icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
                .setProgress(100, 0, true);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        assert notificationManager != null;
        notificationManager.notify(1, notification);

        ProgressHelper.setProgressHandler(new AbstractProgressHandler() {
            @Override
            protected void onProgress(long bytesRead, long contentLength, boolean done) {
                updateProgress(bytesRead, contentLength, done, builder, notificationManager);
            }
        });
        HttpUtils.getRetrofit(ProgressHelper.addProgress(null).build()).create(DownloadApk.class).download(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.code() == ConstantPool.Int.RESPONSE_SUCCESS_CODE.get()) {
                    try {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            InputStream is = responseBody.byteStream();
                            File file = new File(Environment.getExternalStorageDirectory(), fileName);
                            FileOutputStream fos = new FileOutputStream(file);
                            BufferedInputStream bis = new BufferedInputStream(is);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = bis.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                                fos.flush();
                            }
                            fos.close();
                            bis.close();
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Notification build = builder
                            .setContentTitle("下载失败")
                            .setContentText("请稍后再试")
                            .setProgress(100, 0, false)
                            .build();
                    build.flags = Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(1, build);
                    EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "下载失败:" + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Notification build = builder
                        .setContentTitle("下载失败")
                        .setContentText("请稍后再试")
                        .setProgress(100, 0, false)
                        .build();
                build.flags = Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(1, build);
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "下载失败:" + t.toString()));
            }
        });
    }


    private void updateProgress(long bytesRead, long contentLength, boolean done, NotificationCompat.Builder builder, NotificationManager notificationManager) {
        int now = ((int) (bytesRead / 1024));
        int all = ((int) (contentLength / 1024));
        Notification build;
        if (done) {
            //休眠 否则 不显示
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            build = builder.setContentTitle("下载完成")
                    .setContentText(now + "/" + all)
                    .build();
            build.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(1, build);
            //To MainActivity EventBus
            EventBus.getDefault().postSticky(new EventEntity(ConstantPool.Int.INSTALL_APK, apkName));
        } else {
            build = builder
                    .setContentTitle("正在下载更新文件 " + (100 * bytesRead) / contentLength + "%")
                    .setContentText(now + "/" + all)
                    .setProgress(all, now, false)
                    .build();
            build.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(1, build);
        }
    }
}
