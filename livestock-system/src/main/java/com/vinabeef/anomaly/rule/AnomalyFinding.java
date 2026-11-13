package com.vinabeef.anomaly.rule;

import com.vinabeef.shared.contract.AlertType;

public record AnomalyFinding(AlertType type, String message) {
}
