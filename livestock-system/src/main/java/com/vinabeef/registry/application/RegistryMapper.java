package com.vinabeef.registry.application;

import com.vinabeef.registry.api.CattleRecord;
import com.vinabeef.registry.api.GateRecord;
import com.vinabeef.registry.domain.Barn;
import com.vinabeef.registry.domain.Cattle;
import com.vinabeef.registry.domain.Gate;

/** Converts registry entities to the records exposed outside the module; entities never leave it. */
final class RegistryMapper {

    private RegistryMapper() {
    }

    static GateRecord toRecord(Gate gate) {
        Barn barn = gate.getBarn();
        return new GateRecord(gate.getId(), gate.getCode(), gate.getName(), gate.getType(), barn.getId(), barn.getCode());
    }

    static CattleRecord toRecord(Cattle cattle) {
        Barn barn = cattle.getExpectedBarn();
        return new CattleRecord(
                cattle.getId(),
                cattle.getAnimalCode(),
                cattle.getRfidTag(),
                cattle.getBreed(),
                barn.getId(),
                barn.getCode(),
                cattle.getStatus().name(),
                cattle.isActive());
    }
}
