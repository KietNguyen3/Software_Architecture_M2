package com.vinabeef.edge.whitelist;

import com.vinabeef.shared.contract.WhitelistSnapshot.CattleEntry;
import com.vinabeef.shared.contract.WhitelistSnapshot.GateConfig;
import java.util.Collection;
import java.util.Optional;

/**
 * The Gate Reader Node's local copy of the whitelist and gate configuration
 * (SQLite + RAM in production). Reads never leave the node (P4).
 */
public interface LocalWhitelist {

    Optional<GateConfig> gate(long gateId);

    Collection<GateConfig> gates();

    Optional<CattleEntry> cattle(String rfidTag);

    long version();

    int size();
}
