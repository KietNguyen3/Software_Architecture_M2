package com.vinabeef.edge.queue;

import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.GateReadEvent;
import com.vinabeef.shared.contract.GateRef;
import com.vinabeef.shared.contract.ReadClassification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A row of the edge queue. The sequence number keeps arrival order per site. */
@Entity
@Table(name = "edge_event_queue")
class QueuedReadEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(nullable = false, length = 15)
    private String rfidTag;

    private String animalCode;
    private long gateId;
    private String gateCode;
    private long barnId;
    private String barnCode;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    private ReadClassification classification;

    private long whitelistVersion;
    private boolean forwarded;
    private Instant forwardedAt;

    protected QueuedReadEvent() {
    }

    static QueuedReadEvent of(GateReadEvent event) {
        QueuedReadEvent row = new QueuedReadEvent();
        row.eventId = event.eventId();
        row.rfidTag = event.rfidTag();
        row.animalCode = event.animalCode();
        row.gateId = event.gate().gateId();
        row.gateCode = event.gate().gateCode();
        row.barnId = event.gate().barnId();
        row.barnCode = event.gate().barnCode();
        row.direction = event.direction();
        row.occurredAt = event.occurredAt();
        row.classification = event.classification();
        row.whitelistVersion = event.whitelistVersion();
        return row;
    }

    GateReadEvent toEvent() {
        return new GateReadEvent(eventId, rfidTag, animalCode, new GateRef(gateId, gateCode, barnId, barnCode),
                direction, occurredAt, classification, whitelistVersion);
    }

    void markForwarded(Instant at) {
        forwarded = true;
        forwardedAt = at;
    }
}
