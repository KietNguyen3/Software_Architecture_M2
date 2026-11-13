package com.vinabeef.registry.domain;

/** Lifecycle status of an animal. Records are never deleted, only their status changes (AD-6). */
public enum CattleStatus {
    ACTIVE,
    SOLD,
    DEAD,
    CULLED
}
