package com.vinabeef.registry.api;

import com.vinabeef.shared.contract.GateType;

public record GateRecord(long id, String code, String name, GateType type, long barnId, String barnCode) {
}
