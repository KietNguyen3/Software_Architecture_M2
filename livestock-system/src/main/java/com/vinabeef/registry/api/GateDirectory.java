package com.vinabeef.registry.api;

import java.util.List;

/** Read-only view of the configured gates for other components (P8, ISP). */
public interface GateDirectory {

    List<GateRecord> findAll();
}
