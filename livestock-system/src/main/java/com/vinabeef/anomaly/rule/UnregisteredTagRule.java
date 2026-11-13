package com.vinabeef.anomaly.rule;

import com.vinabeef.shared.contract.AlertType;
import com.vinabeef.shared.contract.MovementRecorded;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Unexpected animal: the tag is not in the registry. */
@Component
public class UnregisteredTagRule implements MovementRule {

    @Override
    public Optional<AnomalyFinding> evaluate(MovementContext context) {
        if (context.registeredCattle().isPresent()) {
            return Optional.empty();
        }
        MovementRecorded m = context.movement();
        return Optional.of(new AnomalyFinding(AlertType.UNREGISTERED_TAG,
                "Unregistered tag %s passed gate %s (%s, %s)"
                        .formatted(m.rfidTag(), m.gate().gateCode(), m.gate().barnCode(), m.direction())));
    }
}
