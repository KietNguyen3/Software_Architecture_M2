package com.vinabeef.movement.application;

import com.vinabeef.movement.domain.HerdSession;
import com.vinabeef.movement.domain.SessionStatus;
import com.vinabeef.movement.persistence.HerdSessionRepository;
import com.vinabeef.shared.contract.HerdSessionClosed;
import com.vinabeef.shared.error.NotFoundException;
import com.vinabeef.shared.messaging.EventBroker;
import java.time.Clock;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ends a barn's release/return session (when the supervisor finishes the herd return) and publishes
 * the result. Whether the result is an anomaly is decided by the Anomaly Detection Engine.
 */
@Service
public class HerdSessionService {

    private final HerdSessionRepository sessions;
    private final EventBroker broker;
    private final Clock clock;

    public HerdSessionService(HerdSessionRepository sessions, EventBroker broker, Clock clock) {
        this.sessions = sessions;
        this.broker = broker;
        this.clock = clock;
    }

    @Transactional
    public HerdSessionView close(long barnId) {
        HerdSession session = sessions.findByBarnIdAndStatus(barnId, SessionStatus.OPEN)
                .orElseThrow(() -> new NotFoundException("Barn " + barnId + " has no open herd session"));
        Set<String> missing = session.close(clock.instant());

        broker.publish(new HerdSessionClosed(session.getId(), session.getBarnId(), session.getBarnCode(),
                session.getOutCount(), session.getReturnedCount(), missing, session.getClosedAt()));
        return HerdSessionView.of(session);
    }
}
