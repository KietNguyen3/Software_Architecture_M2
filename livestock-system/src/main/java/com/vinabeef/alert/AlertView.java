package com.vinabeef.alert;

import com.vinabeef.shared.contract.AlertType;
import com.vinabeef.shared.contract.Severity;
import java.time.Instant;

public record AlertView(
        long id,
        AlertType type,
        Severity severity,
        AlertStatus status,
        String message,
        Long barnId,
        Long gateId,
        String rfidTag,
        String sourceRef,
        Instant raisedAt) {

    static AlertView of(Alert a) {
        return new AlertView(a.getId(), a.getType(), a.getSeverity(), a.getStatus(), a.getMessage(), a.getBarnId(),
                a.getGateId(), a.getRfidTag(), a.getSourceRef(), a.getRaisedAt());
    }
}
