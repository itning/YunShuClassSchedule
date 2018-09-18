package top.itning.yunshuclassschedule.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 图片哈希
 *
 * @author itning
 */
@Entity
public class Hash {
    @Id
    private String id;
    @NotNull
    private String name;

    @Generated(hash = 1070788195)
    public Hash(String id, @NotNull String name) {
        this.id = id;
        this.name = name;
    }

    @Generated(hash = 1112031932)
    public Hash() {
    }

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
}
