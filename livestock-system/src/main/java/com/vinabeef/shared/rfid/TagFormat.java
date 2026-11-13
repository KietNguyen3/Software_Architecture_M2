package com.vinabeef.shared.rfid;

/**
 * Strategy for an RFID tag numbering scheme. Tag formats are interchangeable (P13):
 * supporting another scheme means adding an implementation, not editing the callers.
 */
public interface TagFormat {

    /** Human-readable name of the scheme, used in error messages. */
    String name();

    boolean isValid(String tag);

    /** Removes formatting a reader or a person may add (spaces, separators). */
    default String normalize(String raw) {
        return raw == null ? "" : raw.replaceAll("[\\s-]", "");
    }
}
