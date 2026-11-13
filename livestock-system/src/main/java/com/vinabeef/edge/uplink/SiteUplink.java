package com.vinabeef.edge.uplink;

/** WAN link between a barn site's Gate Reader Nodes and the central data center. */
public interface SiteUplink {

    boolean isOnline(long barnId);

    void setOnline(long barnId, boolean online);
}
