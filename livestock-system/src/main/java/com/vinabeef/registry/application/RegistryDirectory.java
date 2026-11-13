package com.vinabeef.registry.application;

import com.vinabeef.registry.api.CattleDirectory;
import com.vinabeef.registry.api.CattleRecord;
import com.vinabeef.registry.api.GateDirectory;
import com.vinabeef.registry.api.GateRecord;
import com.vinabeef.registry.persistence.CattleRepository;
import com.vinabeef.registry.persistence.GateRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The registry's read-only face: other modules see records, never entities or repositories. */
@Service
@Transactional(readOnly = true)
class RegistryDirectory implements CattleDirectory, GateDirectory {

    private final GateRepository gates;
    private final CattleRepository cattle;

    RegistryDirectory(GateRepository gates, CattleRepository cattle) {
        this.gates = gates;
        this.cattle = cattle;
    }

    @Override
    public Optional<CattleRecord> findByRfidTag(String rfidTag) {
        return cattle.findByRfidTag(rfidTag).map(RegistryMapper::toRecord);
    }

    @Override
    public List<CattleRecord> findAllCattle() {
        return cattle.findAll().stream().map(RegistryMapper::toRecord).toList();
    }

    @Override
    public List<GateRecord> findAll() {
        return gates.findAll().stream().map(RegistryMapper::toRecord).toList();
    }
}
