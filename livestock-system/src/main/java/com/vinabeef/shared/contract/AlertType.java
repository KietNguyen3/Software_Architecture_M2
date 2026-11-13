package com.vinabeef.shared.contract;

/** Kinds of abnormal situations the gate subsystem reports (FR-04), each with its default severity. */
public enum AlertType {
    UNREGISTERED_TAG(Severity.HIGH),
    INACTIVE_CATTLE(Severity.MEDIUM),
    WRONG_BARN(Severity.MEDIUM),
    MISSING_CATTLE(Severity.CRITICAL);

    private final Severity severity;

    AlertType(Severity severity) {
        this.severity = severity;
    }

    public Severity severity() {
        return severity;
    }
}
