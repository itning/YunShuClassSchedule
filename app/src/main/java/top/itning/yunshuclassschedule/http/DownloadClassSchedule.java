package top.itning.yunshuclassschedule.http;

import retrofit2.Call;
import retrofit2.http.GET;
import top.itning.yunshuclassschedule.entity.ClassSchedule;

/**
 * 下载课程表
 *
 * @author itning
 */
public interface DownloadClassSchedule extends BaseHttp {
    /**
     * 检查课程表版本
     *
     * @return {@link ClassSchedule}
     */
    @GET("classSchedule/download")
    Call<ClassSchedule> download();
}
