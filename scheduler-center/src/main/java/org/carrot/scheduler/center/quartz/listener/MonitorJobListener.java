package org.carrot.scheduler.center.quartz.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.carrot.scheduler.base.TaskType;
import org.carrot.scheduler.center.manager.dubbo.ServantManagerDubboImpl;
import org.carrot.scheduler.center.quartz.constant.ContextConstant;
import org.carrot.scheduler.center.quartz.trigger.BulletTrigger;
import org.carrot.scheduler.passenger.TaskDetails;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MonitorJobListener implements JobListener{

    private static final Logger logger = LogManager.getLogger(MonitorJobListener.class);

    @Override
    public String getName() {
        return "Terminal Job monitor";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        long nowNanoTime = System.nanoTime();
        dataMap.put(ContextConstant.EXEC_START_TIME,nowNanoTime);
        dataMap.putIfAbsent(ContextConstant.EXEC_BEGIN_TIME,nowNanoTime);
        Trigger trigger = context.getTrigger();
        JobKey key = context.getJobDetail().getKey();
        if(trigger instanceof BulletTrigger){
            BulletTrigger bulletTrigger = (BulletTrigger)trigger;
            logger.debug("{}.{} job will start [{}] times",key.getGroup(),key.getName(),bulletTrigger.getTimesTriggered() +  1);
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        TaskDetails details = (TaskDetails)dataMap.get(ContextConstant.TASK_DETAILS);
        long nowNanoTime = System.nanoTime();
        long oneDuringTime = nowNanoTime - dataMap.getLong(ContextConstant.EXEC_START_TIME);
        logger.debug("{}.{} exec last job take [{}] ns",details.getAppName(),details.getJobName(),oneDuringTime);
        long lifeTime = nowNanoTime -  dataMap.getLong(ContextConstant.EXEC_BEGIN_TIME);
        if(details != null && !TaskType.CronTask.equals(details.getTaskType())){//不是常驻任务
            Date nextFireTime = context.getNextFireTime();
            boolean result = dataMap.getBoolean(ContextConstant.EXEC_RESULT);
            JobKey key = context.getJobDetail().getKey();
            if((!result) && nextFireTime == null){//最后一次失败
                ServantManagerDubboImpl.getByAppName(details.getAppName()).deathStruggle(details);
                try{
                    context.getScheduler().deleteJob(key);//成功删除任务
                    logger.debug("{}.{} done job take [{}] ns",details.getAppName(),details.getJobName(),lifeTime);
                }catch (Exception e){
                    logger.error("deal OneTime Job success terminal was error", e);
                }
            }else if(result){
                try{
                    context.getScheduler().deleteJob(key);//成功删除任务
                    logger.debug("{}.{} done job take [{}] ns",details.getAppName(),details.getJobName(),lifeTime);
                }catch (Exception e){
                    logger.error("deal OneTime Job success terminal was error", e);
                }
            }
        }
    }
}
