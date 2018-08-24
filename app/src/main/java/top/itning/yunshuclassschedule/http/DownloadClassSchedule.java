package top.itning.yunshuclassschedule.http;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import top.itning.yunshuclassschedule.entity.ClassSchedule;

/**
 * 下载课程表
 *
 * @author itning
 */
public interface DownloadClassSchedule {
    /**
     * 检查课程表版本
     *
     * @param classId 课程ID
     * @return {@link ClassSchedule}
     */
    @GET("classSchedule/download")
    Call<List<ClassSchedule>> download(@Query("classId") String classId);
}
