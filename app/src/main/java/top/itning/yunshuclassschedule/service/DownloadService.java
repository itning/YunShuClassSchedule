package top.itning.yunshuclassschedule.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.itning.yunshuclassschedule.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.http.DownloadApk;
import top.itning.yunshuclassschedule.util.HttpUtils;
import top.itning.yunshuclassschedule.util.download.progress.DownloadProgressHandler;
import top.itning.yunshuclassschedule.util.download.progress.ProgressHelper;

/**
 * 下载服务
 *
 * @author itning
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

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
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case START_DOWNLOAD_UPDATE_APK: {
                Log.d(TAG, "开始下载");
                startDownload(eventEntity.getMsg(), eventEntity.getData() + ".apk");
                break;
            }
            default:
        }
    }

    private void startDownload(@NonNull String url, @NonNull String fileName) {
        OkHttpClient.Builder builder = ProgressHelper.addProgress(null);
        ProgressHelper.setProgressHandler(new DownloadProgressHandler() {
            @Override
            protected void onProgress(long bytesRead, long contentLength, boolean done) {
                Log.e("是否在主线程中运行", String.valueOf(Looper.getMainLooper() == Looper.myLooper()));
                Log.e("onProgress", String.format("%d%% done\n", (100 * bytesRead) / contentLength));
                Log.e("done", "--->" + String.valueOf(done));
                Log.d(TAG, (int) (contentLength / 1024) + "");
                Log.d(TAG, ((int) (bytesRead / 1024)) + "");
            }
        });
        HttpUtils.getRetrofit(builder.build()).create(DownloadApk.class).download(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
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
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.HTTP_ERROR, "下载失败:" + t.toString()));
            }
        });
    }
}
