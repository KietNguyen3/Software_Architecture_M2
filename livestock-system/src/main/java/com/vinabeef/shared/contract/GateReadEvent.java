package com.vinabeef.shared.contract;

import java.time.Instant;

/**
 * Movement event produced by a Gate Reader Node, stored in its local queue and forwarded to the broker.
 *
 * @param eventId          UUID generated at the edge; the idempotency key of the whole pipeline (P7)
 * @param animalCode       animal code from the local whitelist, or {@code null} for an unknown tag
 * @param classification   result of the local whitelist check
 * @param whitelistVersion whitelist version the edge used, so stale decisions can be reconciled
 */
public record GateReadEvent(
        String eventId,
        String rfidTag,
        String animalCode,
        GateRef gate,
        Direction direction,
        Instant occurredAt,
        ReadClassification classification,
        long whitelistVersion) {
}
