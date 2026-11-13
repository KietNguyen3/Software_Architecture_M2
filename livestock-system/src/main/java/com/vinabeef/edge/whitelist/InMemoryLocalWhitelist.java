package com.vinabeef.edge.whitelist;

import com.vinabeef.shared.contract.WhitelistSnapshot;
import com.vinabeef.shared.contract.WhitelistSnapshot.CattleEntry;
import com.vinabeef.shared.contract.WhitelistSnapshot.GateConfig;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * In-memory whitelist: an immutable state object swapped atomically when a newer snapshot arrives,
 * so lookups never lock and never see a half-applied update.
 */
@Component
public class InMemoryLocalWhitelist implements LocalWhitelist {

    private record State(long version, Map<Long, GateConfig> gates, Map<String, CattleEntry> cattle) {
        static final State EMPTY = new State(0, Map.of(), Map.of());
    }

    private volatile State state = State.EMPTY;

    @EventListener
    public synchronized void apply(WhitelistSnapshot snapshot) {
        if (snapshot.version() <= state.version()) {
            return; // stale or out-of-order snapshot
        }
        state = new State(
                snapshot.version(),
                snapshot.gates().stream().collect(Collectors.toUnmodifiableMap(g -> g.gate().gateId(), Function.identity())),
                snapshot.cattle().stream().collect(Collectors.toUnmodifiableMap(CattleEntry::rfidTag, Function.identity())));
    }

    @Override
    public Optional<GateConfig> gate(long gateId) {
        return Optional.ofNullable(state.gates().get(gateId));
    }

    @Override
    public Collection<GateConfig> gates() {
        return state.gates().values();
    }

    @Override
    public Optional<CattleEntry> cattle(String rfidTag) {
        return Optional.ofNullable(state.cattle().get(rfidTag));
    }

    @Override
    public long version() {
        return state.version();
    }

    @Override
    public int size() {
        return state.cattle().size();
    }
}
