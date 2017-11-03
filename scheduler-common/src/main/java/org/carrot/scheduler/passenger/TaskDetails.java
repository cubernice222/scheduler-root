package org.carrot.scheduler.passenger;

import org.carrot.scheduler.base.TaskType;

import java.io.Serializable;

public class TaskDetails implements Serializable{

    private static final long serialVersionUID = -1825284788565997602L;

    private TaskType taskType;

    private String appName;

    private String jobName;

    private String mainId;

    public String getMainId() {
        return mainId;
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
    }

    private Object passenger;

    private String expression;


    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }


    public Object getPassenger() {
        return passenger;
    }

    public void setPassenger(Object passenger) {
        this.passenger = passenger;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }


    @Override
    public String toString() {
        return "TaskDetails{" +
                "taskType=" + taskType +
                ", appName='" + appName + '\'' +
                ", jobName='" + jobName + '\'' +
                ", passenger=" + passenger +
                ", expression='" + expression + '\'' +
                '}';
    }
}
