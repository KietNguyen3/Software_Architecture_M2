package com.vinabeef.anomaly.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.vinabeef.registry.api.CattleRecord;
import com.vinabeef.shared.contract.AlertType;
import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.GateRef;
import com.vinabeef.shared.contract.MovementRecorded;
import com.vinabeef.shared.contract.ReadClassification;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MovementRulesTest {

    private static final GateRef A01_GATE = new GateRef(1, "DL-A01-G1", 10, "DL-A01");
    private static final CattleRecord ACTIVE_IN_A01 = cattle("VN-0001", 10, "DL-A01", "ACTIVE");
    private static final CattleRecord ACTIVE_IN_A02 = cattle("VN-0004", 20, "DL-A02", "ACTIVE");
    private static final CattleRecord SOLD_FROM_A02 = cattle("VN-0006", 20, "DL-A02", "SOLD");

    private final MovementRule unregistered = new UnregisteredTagRule();
    private final MovementRule inactive = new InactiveCattleRule();
    private final MovementRule wrongBarn = new WrongBarnRule();

    @Test
    void unregisteredTagRuleFiresOnlyForTagsMissingFromTheRegistry() {
        assertThat(type(unregistered, null)).contains(AlertType.UNREGISTERED_TAG);
        assertThat(type(unregistered, ACTIVE_IN_A01)).isEmpty();
    }

    @Test
    void inactiveCattleRuleFiresForSoldDeadOrCulledAnimals() {
        assertThat(type(inactive, SOLD_FROM_A02)).contains(AlertType.INACTIVE_CATTLE);
        assertThat(type(inactive, ACTIVE_IN_A01)).isEmpty();
        assertThat(type(inactive, null)).isEmpty();
    }

    @Test
    void wrongBarnRuleFiresForActiveAnimalsAtAnotherBarnsGate() {
        assertThat(type(wrongBarn, ACTIVE_IN_A02)).contains(AlertType.WRONG_BARN);
        assertThat(type(wrongBarn, ACTIVE_IN_A01)).isEmpty();
        assertThat(type(wrongBarn, SOLD_FROM_A02)).isEmpty(); // reported once, by the inactive rule
    }

    private static Optional<AlertType> type(MovementRule rule, CattleRecord cattle) {
        MovementRecorded movement = new MovementRecorded("e-1", "704000000000001", null, A01_GATE, Direction.IN,
                Instant.parse("2026-11-08T17:00:00Z"), ReadClassification.KNOWN, 1, null);
        return rule.evaluate(new MovementContext(movement, cattle)).map(AnomalyFinding::type);
    }

    private static CattleRecord cattle(String code, long barnId, String barnCode, String status) {
        return new CattleRecord(1, code, "704000000000001", "Brahman", barnId, barnCode, status, "ACTIVE".equals(status));
    }
}
