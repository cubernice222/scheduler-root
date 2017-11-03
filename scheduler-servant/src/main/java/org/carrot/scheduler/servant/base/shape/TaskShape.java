package org.carrot.scheduler.servant.base.shape;

import org.carrot.scheduler.vector.CenterRegister;

public interface TaskShape {
    void shootTask(String physicalMainId, Object t, String rewriteExpression);

    boolean fireTask(Object t);

    void deathStruggle(Object t);

    void setAppName(String appName);

    void setRegister(CenterRegister register);

    void addShaper();
}