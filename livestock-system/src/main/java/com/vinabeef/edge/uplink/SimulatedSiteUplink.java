package com.vinabeef.edge.uplink;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Demo uplink whose state is switched from the dashboard to simulate a barn losing connectivity. */
@Component
public class SimulatedSiteUplink implements SiteUplink {

    private final Set<Long> offlineSites = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isOnline(long barnId) {
        return !offlineSites.contains(barnId);
    }

    @Override
    public void setOnline(long barnId, boolean online) {
        if (online) {
            offlineSites.remove(barnId);
        } else {
            offlineSites.add(barnId);
        }
    }
}
