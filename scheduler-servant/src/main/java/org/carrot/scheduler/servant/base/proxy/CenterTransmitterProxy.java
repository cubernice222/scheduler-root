package org.carrot.scheduler.servant.base.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.carrot.scheduler.passenger.TaskDetails;
import org.carrot.scheduler.servant.base.annotation.Task;
import org.carrot.scheduler.servant.base.manager.TaskManager;
import org.carrot.scheduler.servant.base.shape.TaskShape;
import org.carrot.scheduler.vector.CenterTransmitter;

import java.lang.reflect.Type;

public class CenterTransmitterProxy implements CenterTransmitter {

    private static final Logger logger = LogManager.getLogger(CenterTransmitterProxy.class);
    @Override
    public boolean execJob(TaskDetails details) {
        TaskShape taskShape = TaskManager.getShaper(details.getJobName());
        if(null != taskShape){
            Task taskAnnotation = taskShape.getClass().getAnnotation(Task.class);
            Class passengerType = taskAnnotation.passengerType();
            Object passenger = null;
            if(passengerType != null){
                passenger = ((TaskShapeProxy)taskShape).getGson().fromJson(String.valueOf(details.getPassenger()),(Type)passengerType);
            }
            return taskShape.fireTask(passenger);
        }else{
            logger.error("can't find executor of task details {}", details);
        }
        return false;
    }


    @Override
    public void deathStruggle(TaskDetails taskDetails) {
        TaskShape taskShape = TaskManager.getShaper(taskDetails.getJobName());
        if(null != taskShape){
            Task taskAnnotation = taskShape.getClass().getAnnotation(Task.class);
            Class passengerType = taskAnnotation.passengerType();
            Object passenger = null;
            if(passengerType != null){
                passenger = ((TaskShapeProxy)taskShape).getGson().fromJson(String.valueOf(taskDetails.getPassenger()),(Type)passengerType);
            }
            taskShape.deathStruggle(passenger);
        }else{
            logger.error("can't find executor of task details {}", taskDetails);
        }
    }
}
