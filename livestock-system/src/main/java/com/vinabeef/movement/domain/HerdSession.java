package com.vinabeef.movement.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * One release/return cycle of a barn ({@code herd_session}). It owns the counting rule: an animal
 * that leaves is outstanding until it returns through a gate of the same barn. Whatever is still
 * outstanding when the session closes is missing (fewer returned than left).
 */
@Entity
@Table(name = "herd_session")
public class HerdSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long barnId;
    private String barnCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    private Instant openedAt;
    private Instant closedAt;
    private int outCount;
    private int returnedCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "herd_session_outstanding", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "rfid_tag", length = 15)
    private Set<String> outstandingTags = new HashSet<>();

    protected HerdSession() {
    }

    public static HerdSession open(long barnId, String barnCode, Instant openedAt) {
        HerdSession session = new HerdSession();
        session.barnId = barnId;
        session.barnCode = barnCode;
        session.status = SessionStatus.OPEN;
        session.openedAt = openedAt;
        return session;
    }

    public void recordOut(String rfidTag) {
        requireOpen();
        if (outstandingTags.add(rfidTag)) {
            outCount++;
        }
    }

    public void recordIn(String rfidTag) {
        requireOpen();
        if (outstandingTags.remove(rfidTag)) {
            returnedCount++;
        }
    }

    /** Closes the session and returns the tags that left and did not come back. */
    public Set<String> close(Instant at) {
        requireOpen();
        status = SessionStatus.CLOSED;
        closedAt = at;
        return Set.copyOf(outstandingTags);
    }

    public int stillOut() {
        return outstandingTags.size();
    }

    private void requireOpen() {
        if (status != SessionStatus.OPEN) {
            throw new IllegalStateException("Herd session " + id + " is already closed");
        }
    }

    public Long getId() {
        return id;
    }

    public long getBarnId() {
        return barnId;
    }

    public String getBarnCode() {
        return barnCode;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public int getOutCount() {
        return outCount;
    }

    public int getReturnedCount() {
        return returnedCount;
    }

    public Set<String> getOutstandingTags() {
        return Set.copyOf(outstandingTags);
    }
}
