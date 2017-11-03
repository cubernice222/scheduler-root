package org.carrot.scheduler.vector;

import org.carrot.scheduler.passenger.TaskDetails;

public interface CenterRegister {
    boolean submitJob(TaskDetails details);
}
