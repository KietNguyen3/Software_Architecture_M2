package com.vinabeef.alert;

import com.vinabeef.shared.contract.AlertType;

/**
 * An alert to raise.
 *
 * @param sourceRef what caused the alert (event id, session, gate outage); the same type and source
 *                  never produce two alerts, so a replayed event cannot alert twice
 */
public record AlertDraft(AlertType type, String message, Long barnId, Long gateId, String rfidTag, String sourceRef) {
}
