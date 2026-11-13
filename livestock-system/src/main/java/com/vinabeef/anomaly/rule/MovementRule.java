package com.vinabeef.anomaly.rule;

import java.util.Optional;

/**
 * One anomaly rule of the Anomaly Detection Engine (FR-04).
 *
 * <p>Contract (LSP): a rule is a pure check. It has no side effects and returns a finding or empty,
 * so the engine can run any rule the same way. Adding a rule means adding a bean; the engine
 * does not change (OCP).
 */
public interface MovementRule {

    Optional<AnomalyFinding> evaluate(MovementContext context);
}
