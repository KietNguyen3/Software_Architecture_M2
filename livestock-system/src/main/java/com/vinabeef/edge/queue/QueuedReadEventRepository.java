package com.vinabeef.edge.queue;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface QueuedReadEventRepository extends JpaRepository<QueuedReadEvent, Long> {

    List<QueuedReadEvent> findByBarnIdAndForwardedFalseOrderBySeqAsc(long barnId, Limit limit);

    @Query("select distinct q.barnId from QueuedReadEvent q where q.forwarded = false")
    List<Long> findBarnIdsWithPendingEvents();

    Optional<QueuedReadEvent> findByEventId(String eventId);

    long countByForwardedFalse();

    long countByBarnIdAndForwardedFalse(long barnId);
}
