package org.carrot.scheduler.center.quartz.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.carrot.scheduler.center.manager.dubbo.ServantManagerDubboImpl;
import org.carrot.scheduler.center.quartz.constant.ContextConstant;
import org.carrot.scheduler.passenger.TaskDetails;
import org.carrot.scheduler.vector.CenterTransmitter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class RemoteJobProxy implements Job{

    private static final Logger logger = LogManager.getLogger(RemoteJobProxy.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        TaskDetails details = (TaskDetails)dataMap.get(ContextConstant.TASK_DETAILS);
        CenterTransmitter centerTransmitter = null;
        logger.info("appName:" + details.getAppName());
        try{
            centerTransmitter = ServantManagerDubboImpl.getByAppName(details.getAppName());
        }catch (Exception e){
            logger.error("获取 CenterTransmitter 异常", e);
        }
        if(centerTransmitter!=null){
            boolean result = centerTransmitter.execJob(details);
            dataMap.put(ContextConstant.EXEC_RESULT,result);
        }else{
            logger.info("centerTransmitter is null");
            dataMap.put(ContextConstant.EXEC_RESULT, false);
        }
    }
}
