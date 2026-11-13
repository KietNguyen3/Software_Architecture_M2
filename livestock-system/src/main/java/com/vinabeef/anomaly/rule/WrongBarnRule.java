package com.vinabeef.anomaly.rule;

import com.vinabeef.registry.api.CattleRecord;
import com.vinabeef.shared.contract.AlertType;
import com.vinabeef.shared.contract.MovementRecorded;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Unexpected animal: an active animal passed a gate of a barn it is not assigned to. */
@Component
public class WrongBarnRule implements MovementRule {

    @Override
    public Optional<AnomalyFinding> evaluate(MovementContext context) {
        MovementRecorded m = context.movement();
        return context.registeredCattle()
                .filter(CattleRecord::active)
                .filter(cattle -> cattle.expectedBarnId() != m.gate().barnId())
                .map(cattle -> new AnomalyFinding(AlertType.WRONG_BARN,
                        "%s belongs to %s but passed gate %s of %s (%s)"
                                .formatted(cattle.animalCode(), cattle.expectedBarnCode(), m.gate().gateCode(),
                                        m.gate().barnCode(), m.direction())));
    }
}
