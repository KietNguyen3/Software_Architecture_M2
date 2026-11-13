package com.vinabeef.anomaly.rule;

import com.vinabeef.registry.api.CattleRecord;
import com.vinabeef.shared.contract.AlertType;
import com.vinabeef.shared.contract.MovementRecorded;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Unexpected animal: the tag belongs to an animal recorded as sold, dead or culled. */
@Component
public class InactiveCattleRule implements MovementRule {

    @Override
    public Optional<AnomalyFinding> evaluate(MovementContext context) {
        return context.registeredCattle()
                .filter(cattle -> !cattle.active())
                .map(cattle -> finding(cattle, context.movement()));
    }

    private static AnomalyFinding finding(CattleRecord cattle, MovementRecorded m) {
        return new AnomalyFinding(AlertType.INACTIVE_CATTLE,
                "%s is registered as %s but passed gate %s (%s, %s)"
                        .formatted(cattle.animalCode(), cattle.status(), m.gate().gateCode(), m.gate().barnCode(), m.direction()));
    }
}
