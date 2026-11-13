package com.vinabeef.shared.contract;

/**
 * Gate configuration from the {@code barn_gate} data model. Each type knows how to derive the
 * movement direction, so callers never switch on the gate type (OCP, polymorphism over conditionals).
 */
public enum GateType {

    /** One-way gate into the barn. */
    ENTRY {
        @Override
        public Direction resolveDirection(Direction antennaDirection) {
            return Direction.IN;
        }
    },

    /** One-way gate out of the barn. */
    EXIT {
        @Override
        public Direction resolveDirection(Direction antennaDirection) {
            return Direction.OUT;
        }
    },

    /** Two-way gate: the reader reports the direction from the inner/outer antenna sequence. */
    BIDIR {
        @Override
        public Direction resolveDirection(Direction antennaDirection) {
            if (antennaDirection == null) {
                throw new IllegalArgumentException(
                        "A bidirectional gate needs the antenna direction (IN or OUT) reported by the reader");
            }
            return antennaDirection;
        }
    };

    public abstract Direction resolveDirection(Direction antennaDirection);
}
