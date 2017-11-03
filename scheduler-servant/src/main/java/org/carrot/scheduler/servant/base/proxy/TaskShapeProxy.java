package org.carrot.scheduler.servant.base.proxy;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.carrot.scheduler.passenger.TaskDetails;
import org.carrot.scheduler.servant.base.annotation.Task;
import org.carrot.scheduler.servant.base.manager.TaskManager;
import org.carrot.scheduler.servant.base.shape.TaskShape;
import org.carrot.scheduler.vector.CenterRegister;


public abstract class TaskShapeProxy implements TaskShape{

    private static final Logger logger = LogManager.getLogger(TaskShapeProxy.class);

    private String appName;

    private CenterRegister register;

    protected  Gson gson = new Gson();

    public Gson getGson() {
        return gson;
    }

    public static Logger getLogger() {
        return logger;
    }

    public CenterRegister getRegister() {
        return register;
    }

    public void setRegister(CenterRegister register) {
        this.register = register;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public void shootTask(String physicalMainId, Object t, String rewriteExpression) {
        Task taskAnnotation = this.getClass().getAnnotation(Task.class);
        TaskDetails taskDetails = new TaskDetails();
        taskDetails.setAppName(appName);
        taskDetails.setJobName(taskAnnotation.jobName());
        taskDetails.setMainId(physicalMainId);
        String expression = rewriteExpression;
        if(!(null != rewriteExpression && "".equals(rewriteExpression.trim()))){
            expression = taskAnnotation.expression();
        }
        taskDetails.setExpression(expression);
        taskDetails.setTaskType(taskAnnotation.type());
        if(t != null){
            String passengerStr = gson.toJson(t);
            taskDetails.setPassenger(passengerStr);
        }
        TaskManager.addShaper(taskDetails.getJobName(), this);
        boolean submitResult = register.submitJob(taskDetails);
        if(!submitResult){
            logger.error("submit task failure task details - {}, please check scheduler center or expression",taskDetails);
        }
    }

    @Override
    public void addShaper() {
        Task taskAnnotation = this.getClass().getAnnotation(Task.class);
        TaskManager.addShaper(taskAnnotation.jobName(), this);
    }
}
