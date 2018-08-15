package top.itning.yunshuclassschedule.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 课程实体
 *
 * @author itning
 */
@Entity
public class ClassSchedule {
    /**
     * 课程编号
     */
    @Id
    private String id;
    /**
     * 星期几的课程
     */
    private int week;
    /**
     * 第几节课
     */
    private int section;
    /**
     * 课程名
     */
    private String name;
    /**
     * 地点
     */
    private String location;
    /**
     * 教师
     */
    private String teacher;

    @Generated(hash = 317900149)
    public ClassSchedule(String id, int week, int section, String name,
            String location, String teacher) {
        this.id = id;
        this.week = week;
        this.section = section;
        this.name = name;
        this.location = location;
        this.teacher = teacher;
    }

    @Generated(hash = 1679435099)
    public ClassSchedule() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    @Override
    public String toString() {
        return "ClassSchedule{" +
                "id='" + id + '\'' +
                ", week=" + week +
                ", section=" + section +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", teacher='" + teacher + '\'' +
                '}';
    }
}
