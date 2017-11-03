package org.carrot.scheduler.servant.base.spring.event;

import org.carrot.scheduler.servant.base.annotation.Task;
import org.carrot.scheduler.servant.base.shape.TaskShape;
import org.carrot.scheduler.vector.CenterRegister;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.*;
import org.springframework.core.env.Environment;

import java.util.Map;

public class TaskReadyEvent implements EnvironmentAware,ApplicationContextAware,ApplicationListener<ApplicationReadyEvent> {

    private Environment environment;

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Map<String,Object> tasks = applicationContext.getBeansWithAnnotation(Task.class);
        String appName = environment.getProperty("spring.application.name");
        CenterRegister centerRegister = (CenterRegister) applicationContext.getBean("centerRegister");
        if(tasks != null && tasks.size() > 0){
            tasks.forEach((k,v)->{
                if(v instanceof TaskShape){
                    TaskShape shape = (TaskShape) v;
                    shape.setAppName(appName);
                    shape.setRegister(centerRegister);
                    Task task = v.getClass().getAnnotation(Task.class);
                    if(task.autoSubmit()){
                        shape.shootTask(null,null,null);
                    }else{
                        shape.addShaper();
                    }
                }else{
                    throw new RuntimeException("can't add task annotation not taskShape instance");
                }
            });
        }
    }
}
