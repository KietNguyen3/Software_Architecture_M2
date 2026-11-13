package com.vinabeef.movement.application;

import com.vinabeef.movement.domain.HerdSession;
import com.vinabeef.movement.domain.SessionStatus;
import java.time.Instant;
import java.util.Set;

/**
 * @param outstandingTags tags still out; for a closed session these are the missing animals
 */
public record HerdSessionView(
        long id,
        long barnId,
        String barnCode,
        SessionStatus status,
        Instant openedAt,
        Instant closedAt,
        int outCount,
        int returnedCount,
        int stillOut,
        Set<String> outstandingTags) {

    static HerdSessionView of(HerdSession s) {
        return new HerdSessionView(s.getId(), s.getBarnId(), s.getBarnCode(), s.getStatus(), s.getOpenedAt(),
                s.getClosedAt(), s.getOutCount(), s.getReturnedCount(), s.stillOut(), s.getOutstandingTags());
    }
}
