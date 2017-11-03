package org.carrot.scheduler.center.manager;

import org.carrot.scheduler.center.vo.FrontTask;
import org.quartz.JobDetail;

import java.util.List;

public interface JobManager {

    List<FrontTask> getAll();

    List<FrontTask> getByGroup(String group);

    FrontTask getByGroupAndJobName(String group, String jobName);

    boolean stopJob(String group);

    boolean stopJob(String group, String jobName);

    boolean modifyJob(String group,String jobName,String cronExpression);

    boolean restartJob(String group);

    boolean restartJob(String group, String jobName);

    boolean deleteJob(String group);
    boolean deleteJob(String group,String jobName);

    boolean isLeader();
}
