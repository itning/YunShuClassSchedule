package top.itning.yunshuclassschedule.http;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import top.itning.yunshuclassschedule.entity.ClassEntity;
import top.itning.yunshuclassschedule.entity.Profession;

/**
 * 课程元数据
 *
 * @author itning
 */
public interface ClassScheduleMetaData {
    /**
     * 获取专业数据
     *
     * @return 专业数据集合
     */
    @GET("classSchedule/profession")
    Call<List<Profession>> getProfession();

    /**
     * 获取班级信息
     *
     * @param professionId 专业ID
     * @return 班级信息集合
     */
    @GET("classSchedule/class")
    Call<List<ClassEntity>> getClassInfo(@Query("professionId") String professionId);
}
