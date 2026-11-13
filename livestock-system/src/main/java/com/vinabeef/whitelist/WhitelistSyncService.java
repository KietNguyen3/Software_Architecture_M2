package com.vinabeef.whitelist;

import com.vinabeef.registry.api.CattleDirectory;
import com.vinabeef.registry.api.GateDirectory;
import com.vinabeef.shared.contract.GateRef;
import com.vinabeef.shared.contract.WhitelistSnapshot;
import com.vinabeef.shared.contract.WhitelistSnapshot.CattleEntry;
import com.vinabeef.shared.contract.WhitelistSnapshot.GateConfig;
import com.vinabeef.shared.messaging.EventBroker;
import java.time.Clock;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Whitelist Sync Service: builds the whitelist (tag to animal, expected barn, status, plus gate
 * configuration) from the registry and pushes it to the Gate Reader Nodes through the broker (AD-9).
 * The gates then classify every read locally, without calling the central system.
 *
 * <p>The demo registry is fixed seed data, so the snapshot is published once at startup. In the full
 * design it is republished after every registry change.
 */
@Service
public class WhitelistSyncService {

    private final CattleDirectory cattle;
    private final GateDirectory gates;
    private final EventBroker broker;
    private final Clock clock;
    private long version;

    public WhitelistSyncService(CattleDirectory cattle, GateDirectory gates, EventBroker broker, Clock clock) {
        this.cattle = cattle;
        this.gates = gates;
        this.broker = broker;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public synchronized void publishSnapshot() {
        List<GateConfig> gateConfigs = gates.findAll().stream()
                .map(g -> new GateConfig(new GateRef(g.id(), g.code(), g.barnId(), g.barnCode()), g.type()))
                .toList();
        List<CattleEntry> entries = cattle.findAllCattle().stream()
                .map(c -> new CattleEntry(c.rfidTag(), c.animalCode(), c.expectedBarnId(), c.active()))
                .toList();

        broker.publish(new WhitelistSnapshot(++version, clock.instant(), gateConfigs, entries));
    }
}
