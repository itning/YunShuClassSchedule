package top.itning.yunshuclassschedule.http;

import retrofit2.Call;
import retrofit2.http.GET;
import top.itning.yunshuclassschedule.entity.AppUpdate;

/**
 * 检查应用版本
 *
 * @author itning
 */
public interface CheckAppUpdate extends BaseHttp{
    /**
     * 检查APP版本
     *
     * @return {@link AppUpdate}
     */
    @GET("app/checkUpdate")
    Call<AppUpdate> checkUpdate();
}
