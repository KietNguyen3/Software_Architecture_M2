package com.vinabeef.edge;

import com.vinabeef.edge.queue.LocalEventQueue;
import com.vinabeef.edge.uplink.SiteUplink;
import com.vinabeef.edge.whitelist.LocalWhitelist;
import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.GateReadEvent;
import com.vinabeef.shared.contract.GateRef;
import com.vinabeef.shared.contract.ReadClassification;
import com.vinabeef.shared.contract.WhitelistSnapshot.CattleEntry;
import com.vinabeef.shared.contract.WhitelistSnapshot.GateConfig;
import com.vinabeef.shared.error.NotFoundException;
import com.vinabeef.shared.rfid.TagFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Gate Reader Service, running on the Gate Reader Node at the barn.
 *
 * <p>Every decision is local: tag format, direction, whitelist classification. The event is stored
 * in the local queue <em>before</em> the reader gets an answer, and forwarding happens separately.
 * The central system is therefore never in the read path (P4): reads stay well under the
 * 2-second budget (NFR-01) and survive WAN outages (NFR-02).
 */
@Service
public class GateReaderService {

    private final TagFormat tagFormat;
    private final LocalWhitelist whitelist;
    private final LocalEventQueue queue;
    private final SiteUplink uplink;
    private final Clock clock;

    public GateReaderService(TagFormat tagFormat, LocalWhitelist whitelist, LocalEventQueue queue,
                             SiteUplink uplink, Clock clock) {
        this.tagFormat = tagFormat;
        this.whitelist = whitelist;
        this.queue = queue;
        this.uplink = uplink;
        this.clock = clock;
    }

    public ReadResult handle(RawRead read) {
        long started = System.nanoTime();

        String tag = tagFormat.normalize(read.rfidTag());
        if (!tagFormat.isValid(tag)) {
            throw new IllegalArgumentException("Malformed RFID read '" + read.rfidTag() + "': expected " + tagFormat.name());
        }
        GateConfig config = whitelist.gate(read.gateId())
                .orElseThrow(() -> new NotFoundException("Gate " + read.gateId() + " is not configured on this Gate Reader Node"));
        GateRef gate = config.gate();
        Direction direction = config.type().resolveDirection(read.antennaDirection());
        Instant occurredAt = read.timestamp() != null ? read.timestamp() : clock.instant();

        CattleEntry cattle = whitelist.cattle(tag).orElse(null);
        GateReadEvent event = new GateReadEvent(
                UUID.randomUUID().toString(),
                tag,
                cattle == null ? null : cattle.animalCode(),
                gate,
                direction,
                occurredAt,
                classify(cattle, gate.barnId()),
                whitelist.version());

        queue.append(event);
        return ReadResult.accepted(event, uplink.isOnline(gate.barnId()), elapsedMillis(started));
    }

    private static ReadClassification classify(CattleEntry cattle, long gateBarnId) {
        if (cattle == null) {
            return ReadClassification.UNREGISTERED;
        }
        if (!cattle.active()) {
            return ReadClassification.INACTIVE;
        }
        return cattle.expectedBarnId() == gateBarnId ? ReadClassification.KNOWN : ReadClassification.WRONG_BARN;
    }

    private static double elapsedMillis(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000.0;
    }
}
