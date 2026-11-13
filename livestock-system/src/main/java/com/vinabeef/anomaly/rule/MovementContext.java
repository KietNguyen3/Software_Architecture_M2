package com.vinabeef.anomaly.rule;

import com.vinabeef.registry.api.CattleRecord;
import com.vinabeef.shared.contract.MovementRecorded;
import java.util.Optional;

/**
 * What a rule sees: the recorded movement and the authoritative registry entry for its tag
 * (looked up once by the engine, not by every rule).
 */
public record MovementContext(MovementRecorded movement, CattleRecord cattle) {

    public Optional<CattleRecord> registeredCattle() {
        return Optional.ofNullable(cattle);
    }
}
