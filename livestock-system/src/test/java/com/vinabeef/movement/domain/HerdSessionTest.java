package com.vinabeef.movement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class HerdSessionTest {

    private static final Instant RELEASE = Instant.parse("2026-11-08T06:00:00Z");
    private static final Instant RETURN = Instant.parse("2026-11-08T17:00:00Z");

    @Test
    void animalsThatLeftAndDidNotReturnAreMissingWhenTheSessionCloses() {
        HerdSession session = HerdSession.open(1, "DL-A01", RELEASE);
        session.recordOut("A");
        session.recordOut("B");
        session.recordOut("C");
        session.recordIn("A");
        session.recordIn("C");

        assertThat(session.getOutCount()).isEqualTo(3);
        assertThat(session.getReturnedCount()).isEqualTo(2);
        assertThat(session.stillOut()).isEqualTo(1);
        assertThat(session.close(RETURN)).containsExactly("B");
        assertThat(session.getStatus()).isEqualTo(SessionStatus.CLOSED);
    }

    @Test
    void repeatedOrUnmatchedReadsDoNotDistortTheCounts() {
        HerdSession session = HerdSession.open(1, "DL-A01", RELEASE);
        session.recordOut("A");
        session.recordOut("A");
        session.recordIn("STRANGER");

        assertThat(session.getOutCount()).isEqualTo(1);
        assertThat(session.getReturnedCount()).isZero();
    }

    @Test
    void closedSessionAcceptsNoMoreReads() {
        HerdSession session = HerdSession.open(1, "DL-A01", RELEASE);
        session.close(RETURN);

        assertThatThrownBy(() -> session.recordOut("A")).isInstanceOf(IllegalStateException.class);
    }
}
