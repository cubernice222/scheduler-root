package org.carrot.scheduler.servant.base.manager;

import org.carrot.scheduler.servant.base.shape.TaskShape;

import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {
    private static ConcurrentHashMap<String,TaskShape> shapeKeeper = new ConcurrentHashMap<>();

    public static void addShaper(String jobName, TaskShape shape){
        shapeKeeper.putIfAbsent(jobName,shape);
    }

    public static TaskShape getShaper(String jobName){
        return shapeKeeper.get(jobName);
    }
}
