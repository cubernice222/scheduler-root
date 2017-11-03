package org.carrot.scheduler.center.manager.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.carrot.scheduler.center.manager.JobManager;
import org.carrot.scheduler.center.quartz.SymbolsEnum;
import org.carrot.scheduler.center.quartz.constant.ContextConstant;
import org.carrot.scheduler.center.quartz.job.RemoteJobProxy;
import org.carrot.scheduler.center.quartz.listener.MonitorJobListener;
import org.carrot.scheduler.center.quartz.trigger.BulletTrigger;
import org.carrot.scheduler.center.vo.FrontTask;
import org.carrot.scheduler.passenger.TaskDetails;
import org.carrot.scheduler.vector.CenterRegister;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.AbstractTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.quartz.JobBuilder.newJob;

@Service("centerRegister")
public class JobManagerImpl implements CenterRegister, JobManager,EnvironmentAware{

    private static final Logger logger = LogManager.getLogger(JobManagerImpl.class);

    private volatile boolean leader;

    private volatile Scheduler scheduler;

    private CuratorFramework client;

    private LeaderSelector leaderSelector;

    private Environment environment;
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Autowired
    private MonitorJobListener jobListener;

    Pattern pattern = Pattern.compile("^[1-9]+\\d*[A-Za-z]$");

    @Value("${zookeeper.address}")
    public String connectString;

    public Scheduler getScheduler(){
        if(scheduler != null){
            return scheduler;
        }else{
            return regetScheduler();
        }
    }

    private Scheduler regetScheduler(){
        ConfigurableEnvironment environmentConfig = (ConfigurableEnvironment)environment;
        MutablePropertySources mapPropertySource  = environmentConfig.getPropertySources();
        Properties properties = new Properties();
        mapPropertySource.forEach(propertySource ->{
            Object obj = propertySource.getSource();
            if(obj instanceof Map){
                properties.putAll((Map)propertySource.getSource());
            }

        });

        /**
         * properties 可以作为StdSchedulerFactory的构造参数，这样一来，就不需要quartz.propertis
         * 但是有一个bug, 就是spring读到的全局参数，  如果是数据的， 保持在map变成了interger,
         * 所以还需要做一个将interger做一个改变
         */
        Set<Object> keyset = properties.keySet();
        for(Object key : keyset){
            Object value = properties.get(key);
            properties.put(key, String.valueOf(value));
        }

        try{
            SchedulerFactory schedFact = new StdSchedulerFactory(properties);
            //SchedulerFactory schedFact = new StdSchedulerFactory();
            scheduler =  schedFact.getScheduler();
            scheduler.getListenerManager().addJobListener(jobListener);
            return scheduler;
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean submitJob(TaskDetails details) {
        boolean submitResult = false;
        try{
            String groupName = details.getAppName()  + "." + details.getAppName();
            String jobName = details.getJobName() + (Strings.isBlank(details.getMainId())? "regularJob" : details.getMainId());
            JobDetail jobDetail = newJob(RemoteJobProxy.class)
                    .withIdentity(jobName, groupName)
                    .build();
            JobKey jobKey = jobDetail.getKey();
            if(scheduler.checkExists(jobKey)){
                logger.warn("{}.{} job already exist", groupName, jobName);
            }else{
                Trigger trigger = null;
                switch (details.getTaskType()){
                    case OneTimeTask:
                        trigger = constructBulletTrigger(details,jobName,groupName);
                        break;
                    case CronTask:
                        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(details.getExpression());
                        trigger = TriggerBuilder.newTrigger().withIdentity(jobName, groupName)
                                .withSchedule(cronScheduleBuilder).build();
                        break;
                }
                jobDetail.getJobDataMap().put(ContextConstant.TASK_DETAILS,details);
                scheduler.scheduleJob(jobDetail, trigger);

                submitResult = true;
            }
        }catch (Exception e){
            logger.error("submit task error", e);
        }
        return submitResult;
    }

    private Trigger constructBulletTrigger(TaskDetails details, String jobName, String groupName) throws Exception{
        BulletTrigger bulletTrigger = new BulletTrigger(jobName,groupName);
        java.util.Calendar calendar = new GregorianCalendar();
        bulletTrigger.setStartTime(calendar.getTime());
        List<String> expressions  =  Splitter.on(",").trimResults().splitToList(details.getExpression());
        if(expressions != null && expressions.size() > 0){
            for (String express : expressions) {
                Matcher matcher = pattern.matcher(express);
                if(!matcher.matches()){
                    throw new Exception("expression is not legal");
                }else{
                    String symbol = express.substring(express.length() -  1);
                    int addNumber = Integer.parseInt(express.substring(0, express.length() -  1));
                    calendar.add(SymbolsEnum.getBySymbols(symbol).getField(),addNumber);
                }
            }
            calendar.add(Calendar.MINUTE, 60);
            bulletTrigger.setEndTime(calendar.getTime());
            bulletTrigger.setBulletExpressions(expressions);
        }else{
            throw new Exception("expression is not legal");
        }
        return bulletTrigger;
    }

    @PostConstruct
    public void construct(){
        client = getClient();
        getScheduler();
        leaderSelector = new LeaderSelector(client, ContextConstant.ZK_LEAD_PATH, new LeaderSelectorListener() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                leader = true;
                scheduler.start();//启动schedule
                while (true){//hold lead
                    TimeUnit.SECONDS.sleep(60);
                }
            }
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {

            }
        });
        leaderSelector.autoRequeue();
        leaderSelector.start();
    }

    @PreDestroy
    public void destroy() throws Exception{
        if(scheduler != null && !scheduler.isShutdown()){
            scheduler.shutdown();
        }
        CloseableUtils.closeQuietly(client);
        CloseableUtils.closeQuietly(leaderSelector);
    }
    private CuratorFramework getClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(6000)
                .connectionTimeoutMs(3000)
                .build();
        client.start();
        return client;
    }


    @Override
    public List<FrontTask> getAll() {
        try{
            GroupMatcher<JobKey> matcher = GroupMatcher.anyGroup();
            return getByMatcher(matcher);
        }catch (Exception e){
            logger.error(e);
        }
        return null;
    }

    private List<FrontTask> getByMatcher(GroupMatcher groupMatcher) throws Exception{
        Set<JobKey> jobKeys = scheduler.getJobKeys(groupMatcher);
        if(jobKeys != null && jobKeys.size() > 0){
            List<FrontTask> frontTasks = new ArrayList<>();
            jobKeys.stream().forEach(jobKey -> {
                frontTasks.add(getByGroupAndJobName(jobKey.getGroup(),jobKey.getName()));
            });
            return frontTasks;
        }
        return null;
    }

    @Override
    public List<FrontTask> getByGroup(String group) {
        try{
            GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(group);
            return getByMatcher(matcher);
        }catch (Exception e){
            logger.error(e);
        }
        return null;
    }

    @Override
    public FrontTask getByGroupAndJobName(String group, String jobName) {
        try{
            JobKey jobKey = JobKey.jobKey(jobName,group);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName,group);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            AbstractTrigger trigger = (AbstractTrigger) scheduler.getTrigger(triggerKey);
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            FrontTask frontTask = constructFronttask(jobDetail,trigger,triggerState);
            return frontTask;
        }catch (Exception e){
            logger.error(e);
        }
        return null;
    }

    private FrontTask constructFronttask(JobDetail jobDetail, AbstractTrigger trigger, Trigger.TriggerState triggerState){
        FrontTask frontTask = new FrontTask();
        TaskDetails taskDetails = (TaskDetails)jobDetail.getJobDataMap().get(ContextConstant.TASK_DETAILS);
        frontTask.setAppName(taskDetails.getAppName());
        if(trigger instanceof CronTrigger){
            CronTrigger cronTrigger = (CronTrigger)trigger;
            frontTask.setExpression(cronTrigger.getCronExpression());
        }else{
            frontTask.setExpression(taskDetails.getExpression());
        }
        frontTask.setGroup(jobDetail.getKey().getGroup());
        frontTask.setNextFireTime(trigger.getNextFireTime());
        frontTask.setStartTime(trigger.getStartTime());
        frontTask.setJobName(jobDetail.getKey().getName());
        frontTask.setTaskJobName(taskDetails.getJobName());
        frontTask.setState(triggerState);
        if(trigger instanceof BulletTrigger){
            frontTask.setTimes(((BulletTrigger) trigger).getTimesTriggered());
        }
        return frontTask;
    }

    @Override
    public boolean stopJob(String group) {
        GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(group);
        try{
            scheduler.pauseJobs(matcher);
            return true;
        }catch (Exception e){
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean stopJob(String group, String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName,group);
        try{
            if(scheduler.checkExists(jobKey)){
                scheduler.pauseJob(jobKey);
                return true;
            }
        }catch (Exception e){
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean modifyJob(String group, String jobName, String cronExpression) {
        JobKey jobKey = JobKey.jobKey(jobName,group);
        try{
            if(scheduler.checkExists(jobKey)){
                Trigger trigger = TriggerBuilder.newTrigger().
                        withIdentity(jobName,group).
                        startNow().
                        withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).
                        build();
                TriggerKey triggerKey = TriggerKey.triggerKey(jobName,group);
                scheduler.rescheduleJob(triggerKey,trigger);
                return true;
            }
        }catch (Exception e){
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean restartJob(String group) {
        GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(group);
        try{
            scheduler.resumeJobs(matcher);
            return true;
        }catch (Exception e){
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean restartJob(String group, String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName,group);
        try{
            if(scheduler.checkExists(jobKey)){
                scheduler.resumeJob(jobKey);
                return true;
            }
        }catch (Exception e){
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean deleteJob(String group) {
        GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(group);
        try{
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            List<JobKey> stopKeys = Lists.newArrayList(jobKeys);
            scheduler.deleteJobs(stopKeys);
            return true;
        }catch (Exception e){
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean deleteJob(String group, String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName,group);
        try{
            if(scheduler.checkExists(jobKey)){
                scheduler.deleteJob(jobKey);
                return true;
            }
        }catch (Exception e){
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean isLeader() {
        return leader;
    }
}
