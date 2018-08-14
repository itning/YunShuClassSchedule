package top.itning.yunshuclassschedule.entity;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 课程实体
 *
 * @author itning
 */
public class ClassSchedule implements Serializable {
    /**
     * 班级唯一编号
     */
    private String id;
    /**
     * 课程名,地点,教师,使用@进行分隔<br/>
     * 例:<code>软件工程@A301@舒露</code><br/>
     * 必须是[5][7]的数组
     */
    private String[][] classArray;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[][] getClassArray() {
        return classArray;
    }

    public void setClassArray(String[][] classArray) {
        this.classArray = classArray;
    }

    @Override
    public String toString() {
        return "ClassSchedule{" +
                "id='" + id + '\'' +
                ", classArray=" + Arrays.toString(classArray) +
                '}';
    }
}
