package com.vinabeef.edge;

import com.vinabeef.shared.contract.Direction;
import java.time.Instant;

/**
 * A raw read reported by the RFID hardware.
 *
 * @param antennaDirection direction from the inner/outer antenna sequence; only needed for bidirectional gates
 * @param timestamp        reader timestamp, or {@code null} to use the node's clock
 */
public record RawRead(String rfidTag, long gateId, Direction antennaDirection, Instant timestamp) {
}
