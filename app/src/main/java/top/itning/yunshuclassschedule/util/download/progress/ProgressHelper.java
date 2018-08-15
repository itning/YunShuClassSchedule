package top.itning.yunshuclassschedule.util.download.progress;

import okhttp3.OkHttpClient;

/**
 * @author ljd
 * @date 4/12/16
 */
public class ProgressHelper {

    private static AbstractProgressHandler mProgressHandler;

    public static OkHttpClient.Builder addProgress(OkHttpClient.Builder builder) {

        if (builder == null) {
            builder = new OkHttpClient.Builder();
        }

        //该方法在子线程中运行
        final ProgressListener progressListener = (progress, total, done) -> {
            if (mProgressHandler == null) {
                return;
            }

            mProgressHandler.onProgress(progress, total, done);
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

    public static void setProgressHandler(AbstractProgressHandler progressHandler) {
        mProgressHandler = progressHandler;
    }
}
