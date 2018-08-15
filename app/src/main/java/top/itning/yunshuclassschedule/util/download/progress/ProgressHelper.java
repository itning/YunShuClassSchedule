package top.itning.yunshuclassschedule.util.download.progress;

import android.util.Log;

import okhttp3.OkHttpClient;

/**
 * Created by ljd on 4/12/16.
 */
public class ProgressHelper {

    private static ProgressBean progressBean = new ProgressBean();
    private static ProgressHandler mProgressHandler;

    public static OkHttpClient.Builder addProgress(OkHttpClient.Builder builder) {

        if (builder == null) {
            builder = new OkHttpClient.Builder();
        }

        //该方法在子线程中运行
        final ProgressListener progressListener = (progress, total, done) -> {
            Log.d("progress:", String.format("%d%% done\n", (100 * progress) / total));
            if (mProgressHandler == null) {
                return;
            }

            progressBean.setBytesRead(progress);
            progressBean.setContentLength(total);
            progressBean.setDone(done);
            mProgressHandler.sendMessage(progressBean);

        };

        //添加拦截器，自定义ResponseBody，添加下载进度
        builder.networkInterceptors().add(chain -> {
            okhttp3.Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder().body(
                    new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();

        });

        return builder;
    }

    public static void setProgressHandler(ProgressHandler progressHandler) {
        mProgressHandler = progressHandler;
    }
}
