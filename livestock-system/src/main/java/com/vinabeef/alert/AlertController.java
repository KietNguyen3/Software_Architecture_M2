package com.vinabeef.alert;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alerts;

    public AlertController(AlertService alerts) {
        this.alerts = alerts;
    }

    @GetMapping
    public List<AlertView> recent(@RequestParam(defaultValue = "20") int limit) {
        return alerts.recent(Math.clamp(limit, 1, 200));
    }

    @GetMapping("/open-count")
    public Map<String, Long> openCount() {
        return Map.of("open", alerts.openCount());
    }

    @PostMapping("/{id}/acknowledge")
    public AlertView acknowledge(@PathVariable long id) {
        return alerts.acknowledge(id);
    }
}
