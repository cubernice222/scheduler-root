package org.carrot.scheduler.servant.base.annotation;

import org.carrot.scheduler.base.TaskType;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {
    String jobName();
    String expression();
    TaskType type();
    Class passengerType();
    boolean autoSubmit() default false;
}