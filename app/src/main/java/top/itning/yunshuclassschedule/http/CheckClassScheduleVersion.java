package top.itning.yunshuclassschedule.http;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * 课程表信息版本检查
 *
 * @author itning
 */
public interface CheckClassScheduleVersion{
    /**
     * 检查课程表版本
     *
     * @return String
     */
    @GET("classSchedule/checkVersion")
    Call<String> checkVersion();
}
