package com.vinabeef.alert;

import com.vinabeef.shared.contract.AlertType;
import com.vinabeef.shared.contract.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/** An alert; one per type and cause, enforced by the database (P6). */
@Entity
@Table(name = "alert", uniqueConstraints = @UniqueConstraint(columnNames = {"type", "source_ref"}))
class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false, length = 500)
    private String message;

    private Long barnId;
    private Long gateId;
    private String rfidTag;

    @Column(name = "source_ref")
    private String sourceRef;

    private Instant raisedAt;
    private Instant acknowledgedAt;

    protected Alert() {
    }

    static Alert open(AlertDraft draft, Instant at) {
        Alert alert = new Alert();
        alert.type = draft.type();
        alert.severity = draft.type().severity();
        alert.status = AlertStatus.OPEN;
        alert.message = draft.message();
        alert.barnId = draft.barnId();
        alert.gateId = draft.gateId();
        alert.rfidTag = draft.rfidTag();
        alert.sourceRef = draft.sourceRef();
        alert.raisedAt = at;
        return alert;
    }

    void acknowledge(Instant at) {
        if (status == AlertStatus.OPEN) {
            status = AlertStatus.ACKNOWLEDGED;
            acknowledgedAt = at;
        }
    }

    Long getId() {
        return id;
    }

    AlertType getType() {
        return type;
    }

    Severity getSeverity() {
        return severity;
    }

    AlertStatus getStatus() {
        return status;
    }

    String getMessage() {
        return message;
    }

    Long getBarnId() {
        return barnId;
    }

    Long getGateId() {
        return gateId;
    }

    String getRfidTag() {
        return rfidTag;
    }

    String getSourceRef() {
        return sourceRef;
    }

    Instant getRaisedAt() {
        return raisedAt;
    }
}
