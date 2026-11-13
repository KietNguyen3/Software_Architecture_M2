package com.vinabeef.shared.contract;

/** Identifies a gate and its barn inside events, so every event is self-describing. */
public record GateRef(long gateId, String gateCode, long barnId, String barnCode) {
}
