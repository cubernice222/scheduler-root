package org.carrot.scheduler.vector;

import org.carrot.scheduler.passenger.TaskDetails;

public interface CenterTransmitter {
    boolean execJob(TaskDetails details);

    void deathStruggle(TaskDetails taskDetails);
}
