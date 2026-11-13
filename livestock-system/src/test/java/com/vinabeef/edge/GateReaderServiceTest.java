package com.vinabeef.edge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vinabeef.edge.ReadResult.Delivery;
import com.vinabeef.edge.queue.LocalEventQueue;
import com.vinabeef.edge.uplink.SimulatedSiteUplink;
import com.vinabeef.edge.whitelist.InMemoryLocalWhitelist;
import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.GateReadEvent;
import com.vinabeef.shared.contract.GateRef;
import com.vinabeef.shared.contract.GateType;
import com.vinabeef.shared.contract.ReadClassification;
import com.vinabeef.shared.contract.WhitelistSnapshot;
import com.vinabeef.shared.contract.WhitelistSnapshot.CattleEntry;
import com.vinabeef.shared.contract.WhitelistSnapshot.GateConfig;
import com.vinabeef.shared.error.NotFoundException;
import com.vinabeef.shared.rfid.Iso11784TagFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The gate decides locally: no broker, database or network is involved in these tests. */
class GateReaderServiceTest {

    private static final long BARN_A01 = 10;
    private static final long BARN_A02 = 20;
    private static final GateRef NORTH_GATE = new GateRef(1, "DL-A01-G1", BARN_A01, "DL-A01");
    private static final GateRef SOUTH_EXIT = new GateRef(2, "DL-A01-G2", BARN_A01, "DL-A01");

    private final InMemoryLocalWhitelist whitelist = new InMemoryLocalWhitelist();
    private final InMemoryQueue queue = new InMemoryQueue();
    private final SimulatedSiteUplink uplink = new SimulatedSiteUplink();
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T06:00:00Z"), ZoneOffset.UTC);
    private GateReaderService reader;

    @BeforeEach
    void setUp() {
        whitelist.apply(new WhitelistSnapshot(7, clock.instant(),
                List.of(new GateConfig(NORTH_GATE, GateType.BIDIR), new GateConfig(SOUTH_EXIT, GateType.EXIT)),
                List.of(new CattleEntry("704000000000001", "VN-0001", BARN_A01, true),
                        new CattleEntry("704000000000004", "VN-0004", BARN_A02, true),
                        new CattleEntry("704000000000006", "VN-0006", BARN_A02, false))));
        reader = new GateReaderService(new Iso11784TagFormat(), whitelist, queue, uplink, clock);
    }

    @Test
    void knownAnimalIsClassifiedLocallyAndStoredBeforeTheReaderIsAnswered() {
        ReadResult result = reader.handle(new RawRead("704000000000001", NORTH_GATE.gateId(), Direction.OUT, null));

        assertThat(result.classification()).isEqualTo(ReadClassification.KNOWN);
        assertThat(result.animalCode()).isEqualTo("VN-0001");
        assertThat(result.whitelistVersion()).isEqualTo(7);
        assertThat(result.delivery()).isEqualTo(Delivery.FORWARDING);
        assertThat(result.processingMillis()).isLessThan(2000); // NFR-01 budget
        assertThat(queue.events).singleElement()
                .satisfies(event -> assertThat(event.eventId()).isEqualTo(result.eventId()));
    }

    @Test
    void unexpectedAnimalsAreFlaggedAtTheGate() {
        assertThat(classify("704999000000123")).isEqualTo(ReadClassification.UNREGISTERED);
        assertThat(classify("704000000000006")).isEqualTo(ReadClassification.INACTIVE);
        assertThat(classify("704000000000004")).isEqualTo(ReadClassification.WRONG_BARN);
    }

    @Test
    void readsDuringAWanOutageAreStillAcceptedAndHeldAtTheEdge() {
        uplink.setOnline(BARN_A01, false);

        ReadResult result = reader.handle(new RawRead("704000000000001", NORTH_GATE.gateId(), Direction.IN, null));

        assertThat(result.delivery()).isEqualTo(Delivery.HELD_OFFLINE);
        assertThat(queue.pendingCount(BARN_A01)).isEqualTo(1);
    }

    @Test
    void exitGateDerivesTheDirectionFromItsConfiguration() {
        ReadResult result = reader.handle(new RawRead("704000000000001", SOUTH_EXIT.gateId(), Direction.IN, null));

        assertThat(result.direction()).isEqualTo(Direction.OUT);
    }

    @Test
    void bidirectionalGateNeedsTheAntennaDirection() {
        assertThatThrownBy(() -> reader.handle(new RawRead("704000000000001", NORTH_GATE.gateId(), null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void malformedTagsAndUnknownGatesAreRejected() {
        assertThatThrownBy(() -> reader.handle(new RawRead("E2000017221101441890AB01", NORTH_GATE.gateId(), Direction.OUT, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reader.handle(new RawRead("704000000000001", 99, Direction.OUT, null)))
                .isInstanceOf(NotFoundException.class);
        assertThat(queue.events).isEmpty();
    }

    private ReadClassification classify(String tag) {
        return reader.handle(new RawRead(tag, NORTH_GATE.gateId(), Direction.IN, null)).classification();
    }

    /** Any queue that honours the LocalEventQueue contract can replace the JPA one (LSP). */
    private static final class InMemoryQueue implements LocalEventQueue {

        private final List<GateReadEvent> events = new ArrayList<>();
        private final Set<String> forwarded = new HashSet<>();

        @Override
        public void append(GateReadEvent event) {
            events.add(event);
        }

        @Override
        public Set<Long> sitesWithPendingEvents() {
            return pending().map(e -> e.gate().barnId()).collect(Collectors.toSet());
        }

        @Override
        public List<GateReadEvent> pending(long barnId, int limit) {
            return pending().filter(e -> e.gate().barnId() == barnId).limit(limit).toList();
        }

        @Override
        public void markForwarded(String eventId) {
            forwarded.add(eventId);
        }

        @Override
        public long pendingCount() {
            return pending().count();
        }

        @Override
        public long pendingCount(long barnId) {
            return pending().filter(e -> e.gate().barnId() == barnId).count();
        }

        private Stream<GateReadEvent> pending() {
            return events.stream().filter(e -> !forwarded.contains(e.eventId()));
        }
    }
}
