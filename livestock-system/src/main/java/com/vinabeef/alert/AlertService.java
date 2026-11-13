package com.vinabeef.alert;

import com.vinabeef.shared.error.NotFoundException;
import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Stores alerts raised by the detectors and lets supervisors acknowledge them. */
@Service
public class AlertService implements AlertRaiser {

    private final AlertRepository alerts;
    private final Clock clock;

    public AlertService(AlertRepository alerts, Clock clock) {
        this.alerts = alerts;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void raise(AlertDraft draft) {
        if (draft.sourceRef() != null && alerts.existsByTypeAndSourceRef(draft.type(), draft.sourceRef())) {
            return; // already raised for this cause
        }
        alerts.save(Alert.open(draft, clock.instant()));
    }

    @Transactional
    public AlertView acknowledge(long alertId) {
        Alert alert = alerts.findById(alertId)
                .orElseThrow(() -> new NotFoundException("Alert " + alertId + " not found"));
        alert.acknowledge(clock.instant());
        return AlertView.of(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertView> recent(int limit) {
        return alerts.findAllByOrderByRaisedAtDesc(Limit.of(limit)).stream().map(AlertView::of).toList();
    }

    @Transactional(readOnly = true)
    public long openCount() {
        return alerts.countByStatus(AlertStatus.OPEN);
    }
}
