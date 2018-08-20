package top.itning.yunshuclassschedule.entity;

/**
 * 专业
 *
 * @author itning
 */
public class Profession {
    /**
     * ID
     */
    private String id;
    /**
     * 专业名
     */
    private String name;

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

    @Override
    public String toString() {
        return "Profession{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
