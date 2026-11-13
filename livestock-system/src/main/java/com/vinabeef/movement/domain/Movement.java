package com.vinabeef.movement.domain;

import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.GateReadEvent;
import com.vinabeef.shared.contract.ReadClassification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

/**
 * Canonical, append-only movement record ({@code movement} schema). The primary key is the
 * edge-generated event id, so the database itself rejects a second copy of the same read (P6, P7).
 * Registry data is referenced by id and code only; this module never writes it (P8).
 */
@Entity
@Table(name = "movement")
public class Movement {

    @Id
    @Column(length = 36)
    private String eventId;

    @Column(nullable = false, length = 15)
    private String rfidTag;

    private String animalCode;
    private long gateId;
    private String gateCode;
    private long barnId;
    private String barnCode;
    private Long herdSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    private ReadClassification classification;

    private long whitelistVersion;

    protected Movement() {
    }

    public static Movement record(GateReadEvent event, Long herdSessionId, Instant receivedAt) {
        Movement movement = new Movement();
        movement.eventId = event.eventId();
        movement.rfidTag = event.rfidTag();
        movement.animalCode = event.animalCode();
        movement.gateId = event.gate().gateId();
        movement.gateCode = event.gate().gateCode();
        movement.barnId = event.gate().barnId();
        movement.barnCode = event.gate().barnCode();
        movement.herdSessionId = herdSessionId;
        movement.direction = event.direction();
        movement.occurredAt = event.occurredAt();
        movement.receivedAt = receivedAt;
        movement.classification = event.classification();
        movement.whitelistVersion = event.whitelistVersion();
        return movement;
    }

    /** How long the event waited between the gate and the central system (non-zero after an outage). */
    public Duration deliveryDelay() {
        return Duration.between(occurredAt, receivedAt);
    }

    public String getEventId() {
        return eventId;
    }

    public String getRfidTag() {
        return rfidTag;
    }

    public String getAnimalCode() {
        return animalCode;
    }

    public long getGateId() {
        return gateId;
    }

    public String getGateCode() {
        return gateCode;
    }

    public long getBarnId() {
        return barnId;
    }

    public String getBarnCode() {
        return barnCode;
    }

    public Long getHerdSessionId() {
        return herdSessionId;
    }

    public Direction getDirection() {
        return direction;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public ReadClassification getClassification() {
        return classification;
    }

    public long getWhitelistVersion() {
        return whitelistVersion;
    }
}
