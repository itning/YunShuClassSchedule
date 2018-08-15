package top.itning.yunshuclassschedule.entity;

import top.itning.yunshuclassschedule.ConstantPool;

public class EventEntity {
    private ConstantPool.Int id;
    private String msg;
    private Object data;

    public EventEntity() {
    }

    public EventEntity(ConstantPool.Int id) {
        this.id = id;
    }

    public EventEntity(ConstantPool.Int id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public EventEntity(ConstantPool.Int id, String msg, Object data) {
        this.id = id;
        this.msg = msg;
        this.data = data;
    }

    public ConstantPool.Int getId() {
        return id;
    }

    public void setId(ConstantPool.Int id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
