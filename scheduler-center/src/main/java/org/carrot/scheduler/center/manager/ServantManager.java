package org.carrot.scheduler.center.manager;

import org.carrot.scheduler.vector.CenterTransmitter;

public interface ServantManager {
    CenterTransmitter getByAppName(String appName);
}
