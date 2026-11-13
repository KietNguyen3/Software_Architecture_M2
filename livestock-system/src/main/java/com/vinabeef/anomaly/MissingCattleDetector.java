package com.vinabeef.anomaly;

import com.vinabeef.alert.AlertDraft;
import com.vinabeef.alert.AlertRaiser;
import com.vinabeef.registry.api.CattleDirectory;
import com.vinabeef.registry.api.CattleRecord;
import com.vinabeef.shared.contract.AlertType;
import com.vinabeef.shared.contract.HerdSessionClosed;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/** Missing animals: fewer returned than left when a barn's herd session closes (FR-04). */
@Component
public class MissingCattleDetector {

    private final CattleDirectory cattle;
    private final AlertRaiser alerts;

    public MissingCattleDetector(CattleDirectory cattle, AlertRaiser alerts) {
        this.cattle = cattle;
        this.alerts = alerts;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHerdSessionClosed(HerdSessionClosed session) {
        if (session.missingTags().isEmpty()) {
            return;
        }
        String missing = session.missingTags().stream()
                .sorted()
                .map(this::describe)
                .collect(Collectors.joining(", "));
        String message = "%d of %d cattle did not return to %s: %s"
                .formatted(session.missingTags().size(), session.outCount(), session.barnCode(), missing);

        alerts.raise(new AlertDraft(AlertType.MISSING_CATTLE, message, session.barnId(), null, null,
                "herd-session:" + session.sessionId()));
    }

    private String describe(String rfidTag) {
        return cattle.findByRfidTag(rfidTag)
                .map(CattleRecord::animalCode)
                .orElse("unregistered " + rfidTag);
    }
}
