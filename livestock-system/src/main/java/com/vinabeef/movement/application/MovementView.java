package com.vinabeef.movement.application;

import com.vinabeef.movement.domain.Movement;
import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.ReadClassification;
import java.time.Instant;

public record MovementView(
        String eventId,
        String rfidTag,
        String animalCode,
        long gateId,
        String gateCode,
        long barnId,
        String barnCode,
        Direction direction,
        Instant occurredAt,
        Instant receivedAt,
        long deliveryDelayMillis,
        ReadClassification classification,
        long whitelistVersion) {

    static MovementView of(Movement m) {
        return new MovementView(m.getEventId(), m.getRfidTag(), m.getAnimalCode(), m.getGateId(), m.getGateCode(),
                m.getBarnId(), m.getBarnCode(), m.getDirection(), m.getOccurredAt(), m.getReceivedAt(),
                m.deliveryDelay().toMillis(), m.getClassification(), m.getWhitelistVersion());
    }
}
