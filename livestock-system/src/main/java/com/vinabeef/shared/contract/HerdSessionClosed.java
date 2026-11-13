package com.vinabeef.shared.contract;

import java.time.Instant;
import java.util.Set;

/**
 * Published when a barn's release/return session ends.
 *
 * @param missingTags tags that left the barn in this session and did not come back
 */
public record HerdSessionClosed(
        long sessionId,
        long barnId,
        String barnCode,
        int outCount,
        int returnedCount,
        Set<String> missingTags,
        Instant closedAt) {
}
