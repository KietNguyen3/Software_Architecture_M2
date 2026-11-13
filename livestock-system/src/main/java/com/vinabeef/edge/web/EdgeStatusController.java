package com.vinabeef.edge.web;

import com.vinabeef.edge.queue.LocalEventQueue;
import com.vinabeef.edge.uplink.SiteUplink;
import com.vinabeef.edge.whitelist.LocalWhitelist;
import com.vinabeef.shared.contract.GateRef;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Edge state for the dashboard, and the switch that simulates a barn site losing its WAN link. */
@RestController
@RequestMapping("/api/edge")
public class EdgeStatusController {

    public record SiteStatus(long barnId, String barnCode, boolean online, long pendingEvents) {
    }

    public record EdgeStatus(long whitelistVersion, int whitelistSize, long pendingEvents, List<SiteStatus> sites) {
    }

    public record LinkChange(@NotNull Boolean online) {
    }

    private final LocalWhitelist whitelist;
    private final LocalEventQueue queue;
    private final SiteUplink uplink;

    public EdgeStatusController(LocalWhitelist whitelist, LocalEventQueue queue, SiteUplink uplink) {
        this.whitelist = whitelist;
        this.queue = queue;
        this.uplink = uplink;
    }

    @GetMapping("/status")
    public EdgeStatus status() {
        List<SiteStatus> sites = sites().values().stream()
                .sorted(Comparator.comparing(GateRef::barnCode))
                .map(this::siteStatus)
                .toList();
        return new EdgeStatus(whitelist.version(), whitelist.size(), queue.pendingCount(), sites);
    }

    @PutMapping("/sites/{barnId}/link")
    public SiteStatus changeLink(@PathVariable long barnId, @Valid @RequestBody LinkChange change) {
        uplink.setOnline(barnId, change.online());
        GateRef site = sites().get(barnId);
        String barnCode = site == null ? String.valueOf(barnId) : site.barnCode();
        return new SiteStatus(barnId, barnCode, uplink.isOnline(barnId), queue.pendingCount(barnId));
    }

    private SiteStatus siteStatus(GateRef site) {
        return new SiteStatus(site.barnId(), site.barnCode(), uplink.isOnline(site.barnId()), queue.pendingCount(site.barnId()));
    }

    /** One entry per barn site that has at least one configured gate. */
    private Map<Long, GateRef> sites() {
        return whitelist.gates().stream()
                .map(config -> config.gate())
                .collect(Collectors.toMap(GateRef::barnId, Function.identity(), (first, second) -> first));
    }
}
