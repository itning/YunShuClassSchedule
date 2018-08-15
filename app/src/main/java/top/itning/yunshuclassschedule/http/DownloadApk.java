package top.itning.yunshuclassschedule.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * 下载APK
 *
 * @author itning
 */
public interface DownloadApk {
    /**
     * 下载
     *
     * @param url URL
     * @return {@link ResponseBody}
     */
    @GET
    Call<ResponseBody> download(@Url String url);
}
