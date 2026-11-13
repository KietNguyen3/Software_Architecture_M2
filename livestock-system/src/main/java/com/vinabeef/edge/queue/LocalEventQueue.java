package com.vinabeef.edge.queue;

import com.vinabeef.shared.contract.GateReadEvent;
import java.util.List;
import java.util.Set;

/**
 * Store-and-forward queue of the Gate Reader Node (SQLite in production, AD-2).
 *
 * <p>Contract every implementation must keep (LSP): {@link #append} returns only after the event
 * is stored durably, and {@link #pending} returns a barn site's events in arrival order.
 */
public interface LocalEventQueue {

    void append(GateReadEvent event);

    /** Barn sites that still have events waiting to be forwarded. */
    Set<Long> sitesWithPendingEvents();

    List<GateReadEvent> pending(long barnId, int limit);

    void markForwarded(String eventId);

    long pendingCount();

    long pendingCount(long barnId);
}
