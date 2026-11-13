package com.vinabeef.edge;

import com.vinabeef.edge.queue.LocalEventQueue;
import com.vinabeef.edge.uplink.SiteUplink;
import com.vinabeef.shared.contract.GateReadEvent;
import com.vinabeef.shared.messaging.EventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Store-and-forward relay of the Gate Reader Node. While a barn site's WAN link is up, it drains
 * that site's queued events to the broker in arrival order. An event is marked forwarded only after
 * the broker accepts it, so delivery is at-least-once and consumers dedupe on {@code eventId}
 * (P5, P7). If the broker rejects an event, the site stops for this cycle so order is kept.
 */
@Component
public class EdgeForwarder {

    private static final Logger log = LoggerFactory.getLogger(EdgeForwarder.class);
    private static final int BATCH_SIZE = 100;

    private final LocalEventQueue queue;
    private final SiteUplink uplink;
    private final EventBroker broker;

    public EdgeForwarder(LocalEventQueue queue, SiteUplink uplink, EventBroker broker) {
        this.queue = queue;
        this.uplink = uplink;
        this.broker = broker;
    }

    /** Runs one forwarding cycle and returns how many events reached the broker. */
    @Scheduled(fixedDelayString = "${vinabeef.edge.forward-interval-ms:500}")
    public synchronized int flush() {
        int forwarded = 0;
        for (long site : queue.sitesWithPendingEvents()) {
            if (uplink.isOnline(site)) {
                forwarded += forwardSite(site);
            }
        }
        return forwarded;
    }

    private int forwardSite(long site) {
        int forwarded = 0;
        for (GateReadEvent event : queue.pending(site, BATCH_SIZE)) {
            try {
                broker.publish(event);
            } catch (RuntimeException ex) {
                log.warn("Event {} from site {} was not accepted and stays queued: {}", event.eventId(), site, ex.toString());
                break;
            }
            queue.markForwarded(event.eventId());
            forwarded++;
        }
        return forwarded;
    }
}
