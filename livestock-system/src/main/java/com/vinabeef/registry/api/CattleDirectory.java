package com.vinabeef.registry.api;

import java.util.List;
import java.util.Optional;

/**
 * Read-only view of the cattle registry for other components. The registry is the only writer
 * of its data; everyone else reads through this interface (P8, ISP).
 */
public interface CattleDirectory {

    Optional<CattleRecord> findByRfidTag(String rfidTag);

    List<CattleRecord> findAllCattle();
}
