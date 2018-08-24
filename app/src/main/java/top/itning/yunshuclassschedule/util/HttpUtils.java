package top.itning.yunshuclassschedule.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import top.itning.yunshuclassschedule.http.BaseHttp;

/**
 * 网络工具
 *
 * @author itning
 */
@SuppressWarnings("unused")
public class HttpUtils {
    private static final Retrofit.Builder BUILDER = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create());
    private static final OkHttpClient CLIENT = new OkHttpClient().newBuilder().connectTimeout(2, TimeUnit.SECONDS).build();

    private HttpUtils() {

    }

    public static Retrofit getRetrofit() {
        return BUILDER
                .baseUrl(BaseHttp.BASE_URL)
                .client(CLIENT)
                .build();
    }

    public static Retrofit getRetrofit(OkHttpClient client) {
        return BUILDER
                .client(client)
                .build();
    }

    public static Retrofit getRetrofit(String baseUrl) {
        return BUILDER
                .baseUrl(baseUrl)
                .client(CLIENT)
                .build();
    }
}
