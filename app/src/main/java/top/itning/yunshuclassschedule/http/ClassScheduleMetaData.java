package top.itning.yunshuclassschedule.http;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import top.itning.yunshuclassschedule.entity.ClassEntity;
import top.itning.yunshuclassschedule.entity.Profession;

public interface ClassScheduleMetaData {
    @GET("classSchedule/profession")
    Call<List<Profession>> getProfession();

    @GET("classSchedule/class")
    Call<List<ClassEntity>> getClassInfo(@Query("professionId") String professionId);
}
