package com.vinabeef.shared.contract;

import java.time.Instant;

/** Published by the Movement &amp; Counting Service after a movement is stored ("Count_Updated"). */
public record MovementRecorded(
        String eventId,
        String rfidTag,
        String animalCode,
        GateRef gate,
        Direction direction,
        Instant occurredAt,
        ReadClassification edgeClassification,
        long whitelistVersion,
        Long herdSessionId) {
}
