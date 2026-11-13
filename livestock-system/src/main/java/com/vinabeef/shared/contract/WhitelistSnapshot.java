package com.vinabeef.shared.contract;

import java.time.Instant;
import java.util.List;

/**
 * Versioned whitelist pushed to every Gate Reader Node: tag to animal, expected barn and status,
 * plus the gate configuration. Gates classify reads against it without a WAN round trip (AD-9).
 */
public record WhitelistSnapshot(long version, Instant generatedAt, List<GateConfig> gates, List<CattleEntry> cattle) {

    public WhitelistSnapshot {
        gates = List.copyOf(gates);
        cattle = List.copyOf(cattle);
    }

    public record GateConfig(GateRef gate, GateType type) {
    }

    public record CattleEntry(String rfidTag, String animalCode, long expectedBarnId, boolean active) {
    }
}
