package org.carrot.scheduler.center.quartz.trigger;

import org.carrot.scheduler.center.quartz.SymbolsEnum;
import org.quartz.Calendar;
import org.quartz.ScheduleBuilder;
import org.quartz.impl.triggers.AbstractTrigger;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class BulletTrigger extends AbstractTrigger {
    private int timesTriggered;

    private Date previousFireTime, nextFireTime,startTime,endTime;

    private List<String> bulletExpressions;

    public List<String> getBulletExpressions() {
        return bulletExpressions;
    }

    public void setBulletExpressions(List<String> bulletExpressions) {
        this.bulletExpressions = bulletExpressions;
    }

    public static int getYearToGiveupSchedulingAt() {
        return YEAR_TO_GIVEUP_SCHEDULING_AT;
    }

    private static final int YEAR_TO_GIVEUP_SCHEDULING_AT = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 100;

    public int getTimesTriggered() {
        return timesTriggered;
    }

    public void setTimesTriggered(int timesTriggered) {
        this.timesTriggered = timesTriggered;
    }

    public BulletTrigger() {
    }

    public BulletTrigger(String name) {
        super(name);
    }

    public BulletTrigger(String name, String group) {
        super(name, group);
    }

    public BulletTrigger(String name, String group, String jobName, String jobGroup) {
        super(name, group, jobName, jobGroup);
    }

    @Override
    public void triggered(Calendar calendar) {
        timesTriggered++;
        previousFireTime = nextFireTime;
        nextFireTime = getFireTimeAfter(nextFireTime);
        nextFireTime = considerCalendar(calendar,nextFireTime);
    }


    private Date considerCalendar(Calendar calendar,Date shouldFireTime){
        while (shouldFireTime != null && calendar != null
                && !calendar.isTimeIncluded(shouldFireTime.getTime())) {

            shouldFireTime = getFireTimeAfter(shouldFireTime);

            if(shouldFireTime == null)
                break;

            //avoid infinite loop
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(shouldFireTime);
            if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
                shouldFireTime = null;
            }
        }
        return shouldFireTime;
    }
    @Override
    public Date computeFirstFireTime(Calendar calendar) {
        Date firstFireTime = getFireTimeAfter(null);
        nextFireTime = considerCalendar(calendar, firstFireTime);
        return firstFireTime;
    }

    @Override
    public boolean mayFireAgain() {
        return (getNextFireTime() != null);
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public Date getNextFireTime() {
        return nextFireTime;
    }

    @Override
    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    @Override
    public Date getFireTimeAfter(Date afterTime) {
        if (afterTime == null) {
            afterTime = new Date();
        }
        if(bulletExpressions != null && bulletExpressions.size() > 0
                && timesTriggered < bulletExpressions.size()){
            String expression = bulletExpressions.get(timesTriggered);
            String symbol = expression.substring(expression.length() -  1);
            int addNumber = Integer.parseInt(expression.substring(0, expression.length() -  1));
            java.util.Calendar nextCalendar= new GregorianCalendar();
            nextCalendar.setTime(afterTime);
            nextCalendar.add(SymbolsEnum.getBySymbols(symbol).getField(),addNumber);
            Date nextFire = nextCalendar.getTime();
            if(nextFire.after(endTime)){
                return null;
            }
            return nextCalendar.getTime();
        }
        return null;
    }

    @Override
    public Date getFinalFireTime() {
        System.out.println("getFinalFireTime()//不知道什么时候调用,暂时不实现");
        return null;
    }

    @Override
    protected boolean validateMisfireInstruction(int candidateMisfireInstruction) {
        System.out.println("validateMisfireInstruction()//不知道什么时候调用,暂时不实现, +" + candidateMisfireInstruction);
        return false;
    }

    @Override
    public void updateAfterMisfire(Calendar cal) {
        if(timesTriggered >= bulletExpressions.size()){
            nextFireTime = null;
        }
        Date now = new Date();
        if(endTime.before(now)){
            nextFireTime = null;
        }else{
            nextFireTime = getFireTimeAfter(now);
        }
    }

    @Override
    public void updateWithNewCalendar(Calendar calendar, long misfireThreshold) {
        nextFireTime = getFireTimeAfter(previousFireTime);

        if (nextFireTime == null || calendar == null) {
            return;
        }

        Date now = new Date();
        while (nextFireTime != null && !calendar.isTimeIncluded(nextFireTime.getTime())) {

            nextFireTime = getFireTimeAfter(nextFireTime);

            if(nextFireTime == null)
                break;

            //avoid infinite loop
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(nextFireTime);
            if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
                nextFireTime = null;
            }

            if(nextFireTime != null && nextFireTime.before(now)) {
                long diff = now.getTime() - nextFireTime.getTime();
                if(diff >= misfireThreshold) {
                    nextFireTime = getFireTimeAfter(nextFireTime);
                }
            }
        }
    }

    @Override
    public ScheduleBuilder getScheduleBuilder() {
        return null;
    }

    @Override
    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    @Override
    public void setPreviousFireTime(Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }
}
