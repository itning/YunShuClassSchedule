package top.itning.yunshuclassschedule.http;

import retrofit2.Call;
import retrofit2.http.GET;
import top.itning.yunshuclassschedule.entity.AppUpdate;

/**
 * 检查应用版本
 *
 * @author itning
 */
public interface CheckAppUpdate {
    /**
     * 检查APP版本
     *
     * @return {@link AppUpdate}
     */
    @GET("update.json")
    Call<AppUpdate> checkUpdate();
}
