package com.vinabeef.edge.queue;

import com.vinabeef.shared.contract.GateReadEvent;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA implementation of the edge queue (stands in for the node's SQLite file in the demo). */
@Component
class JpaLocalEventQueue implements LocalEventQueue {

    private final QueuedReadEventRepository rows;
    private final Clock clock;

    JpaLocalEventQueue(QueuedReadEventRepository rows, Clock clock) {
        this.rows = rows;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void append(GateReadEvent event) {
        rows.saveAndFlush(QueuedReadEvent.of(event));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> sitesWithPendingEvents() {
        return Set.copyOf(rows.findBarnIdsWithPendingEvents());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GateReadEvent> pending(long barnId, int limit) {
        return rows.findByBarnIdAndForwardedFalseOrderBySeqAsc(barnId, Limit.of(limit)).stream()
                .map(QueuedReadEvent::toEvent)
                .toList();
    }

    @Override
    @Transactional
    public void markForwarded(String eventId) {
        rows.findByEventId(eventId).ifPresent(row -> row.markForwarded(clock.instant()));
    }

    @Override
    @Transactional(readOnly = true)
    public long pendingCount() {
        return rows.countByForwardedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public long pendingCount(long barnId) {
        return rows.countByBarnIdAndForwardedFalse(barnId);
    }
}
