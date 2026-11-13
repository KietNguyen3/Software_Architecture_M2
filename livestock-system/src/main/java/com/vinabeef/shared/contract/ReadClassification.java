package com.vinabeef.shared.contract;

/** Result of the local whitelist check done at the gate ({@code movement.classification}). */
public enum ReadClassification {
    /** Registered, active animal at a gate of its own barn. */
    KNOWN,
    /** Tag is not in the whitelist. */
    UNREGISTERED,
    /** Tag belongs to an animal that is no longer active (sold, dead, culled). */
    INACTIVE,
    /** Active animal detected at a gate of a barn it is not assigned to. */
    WRONG_BARN
}
