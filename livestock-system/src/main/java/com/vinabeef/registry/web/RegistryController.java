package com.vinabeef.registry.web;

import com.vinabeef.registry.api.CattleDirectory;
import com.vinabeef.registry.api.CattleRecord;
import com.vinabeef.registry.api.GateDirectory;
import com.vinabeef.registry.api.GateRecord;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only master data for the dashboard (the demo registry is seeded at startup). */
@RestController
@RequestMapping("/api")
public class RegistryController {

    private final GateDirectory gates;
    private final CattleDirectory cattle;

    public RegistryController(GateDirectory gates, CattleDirectory cattle) {
        this.gates = gates;
        this.cattle = cattle;
    }

    @GetMapping("/gates")
    public List<GateRecord> gates() {
        return gates.findAll();
    }

    @GetMapping("/cattle")
    public List<CattleRecord> cattle() {
        return cattle.findAllCattle();
    }
}
