package com.vinabeef.anomaly;

import com.vinabeef.alert.AlertDraft;
import com.vinabeef.alert.AlertRaiser;
import com.vinabeef.anomaly.rule.AnomalyFinding;
import com.vinabeef.anomaly.rule.MovementContext;
import com.vinabeef.anomaly.rule.MovementRule;
import com.vinabeef.registry.api.CattleDirectory;
import com.vinabeef.shared.contract.MovementRecorded;
import com.vinabeef.shared.contract.ReadClassification;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Anomaly Detection Engine (FR-04). After a movement is committed, it checks the movement against
 * the authoritative registry, not the gate's possibly stale whitelist copy, using every
 * {@link MovementRule} bean, and raises one alert per finding.
 *
 * <p>It runs after the movement transaction commits, like an independent broker consumer: a
 * failure here can never roll back or block the movement record (NFR-02, P14).
 */
@Service
public class AnomalyDetectionEngine {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionEngine.class);

    private final List<MovementRule> rules;
    private final CattleDirectory cattle;
    private final AlertRaiser alerts;

    public AnomalyDetectionEngine(List<MovementRule> rules, CattleDirectory cattle, AlertRaiser alerts) {
        this.rules = List.copyOf(rules);
        this.cattle = cattle;
        this.alerts = alerts;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMovementRecorded(MovementRecorded movement) {
        MovementContext context = new MovementContext(movement, cattle.findByRfidTag(movement.rfidTag()).orElse(null));
        List<AnomalyFinding> findings = rules.stream()
                .map(rule -> rule.evaluate(context))
                .flatMap(Optional::stream)
                .toList();

        if (findings.isEmpty() && movement.edgeClassification() != ReadClassification.KNOWN) {
            // The gate flagged it with an older whitelist, but the registry says it is fine: no alert.
            log.info("Stale edge decision reconciled: event {} was {} at whitelist v{}, registry confirms it",
                    movement.eventId(), movement.edgeClassification(), movement.whitelistVersion());
        }
        findings.forEach(finding -> alerts.raise(new AlertDraft(finding.type(), finding.message(),
                movement.gate().barnId(), movement.gate().gateId(), movement.rfidTag(), movement.eventId())));
    }
}
