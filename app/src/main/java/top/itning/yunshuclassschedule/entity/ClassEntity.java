package top.itning.yunshuclassschedule.entity;

/**
 * 班级信息
 *
 * @author itning
 */
public class ClassEntity {
    /**
     * ID
     */
    private String id;
    /**
     * 名
     */
    private String name;
    /**
     * 版本
     */
    private String version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
