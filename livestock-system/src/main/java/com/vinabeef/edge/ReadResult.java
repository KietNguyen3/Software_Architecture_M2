package com.vinabeef.edge;

import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.GateReadEvent;
import com.vinabeef.shared.contract.GateRef;
import com.vinabeef.shared.contract.ReadClassification;
import java.time.Instant;

/**
 * What the Gate Reader Node answers to the reader, decided locally.
 *
 * @param processingMillis time spent on the node, measured against the 2-second budget of NFR-01
 */
public record ReadResult(
        String eventId,
        String rfidTag,
        String animalCode,
        GateRef gate,
        Direction direction,
        Instant occurredAt,
        ReadClassification classification,
        long whitelistVersion,
        Delivery delivery,
        double processingMillis) {

    public enum Delivery {
        /** Stored locally; the relay forwards it to the broker within the next cycle. */
        FORWARDING,
        /** Stored locally; the site's WAN link is down, so it waits at the edge. */
        HELD_OFFLINE
    }

    static ReadResult accepted(GateReadEvent event, boolean uplinkOnline, double processingMillis) {
        return new ReadResult(event.eventId(), event.rfidTag(), event.animalCode(), event.gate(), event.direction(),
                event.occurredAt(), event.classification(), event.whitelistVersion(),
                uplinkOnline ? Delivery.FORWARDING : Delivery.HELD_OFFLINE, processingMillis);
    }
}
