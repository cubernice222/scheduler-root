package org.carrot.scheduler.center.vo;

import org.quartz.Trigger;

import java.util.Date;

public class FrontTask {

    private String appName;

    private String taskJobName;

    private String group;

    private String jobName;

    private String expression;
    //执行次数
    private int times;

    private Date startTime;

    private Date nextFireTime;

    private Trigger.TriggerState state;

    private String stateDesc;

    public String getStateDesc() {
        return stateDesc;
    }

    public void setStateDesc(String stateDesc) {
        this.stateDesc = stateDesc;
    }

    public Trigger.TriggerState getState() {
        return state;
    }

    public void setState(Trigger.TriggerState state) {
        this.state = state;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTaskJobName() {
        return taskJobName;
    }

    public void setTaskJobName(String taskJobName) {
        this.taskJobName = taskJobName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }
}
