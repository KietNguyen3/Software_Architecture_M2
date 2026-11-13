package com.vinabeef.movement.web;

import com.vinabeef.movement.application.HerdSessionService;
import com.vinabeef.movement.application.HerdSessionView;
import com.vinabeef.movement.application.MovementQueryService;
import com.vinabeef.movement.application.MovementView;
import com.vinabeef.shared.error.NotFoundException;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MovementController {

    private final MovementQueryService queries;
    private final HerdSessionService sessions;

    public MovementController(MovementQueryService queries, HerdSessionService sessions) {
        this.queries = queries;
        this.sessions = sessions;
    }

    @GetMapping("/movements")
    public List<MovementView> recent(@RequestParam(defaultValue = "15") int limit) {
        return queries.recent(Math.clamp(limit, 1, 200));
    }

    @GetMapping("/movements/count")
    public Map<String, Long> count() {
        return Map.of("total", queries.count());
    }

    @GetMapping("/movements/{eventId}")
    public MovementView find(@PathVariable String eventId) {
        return queries.find(eventId).orElseThrow(() -> new NotFoundException("Movement " + eventId + " not recorded yet"));
    }

    @GetMapping("/herd-sessions")
    public List<HerdSessionView> openSessions() {
        return queries.openSessions();
    }

    @PostMapping("/barns/{barnId}/herd-session/close")
    public HerdSessionView closeSession(@PathVariable long barnId) {
        return sessions.close(barnId);
    }
}
