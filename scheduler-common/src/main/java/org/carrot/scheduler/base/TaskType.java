package org.carrot.scheduler.base;

import java.io.Serializable;

public enum TaskType implements Serializable{

    OneTimeTask("oneTime","一次性任务"),
    CronTask("CronTask","常驻任务");
    private String value;
    private String desc;

    TaskType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
