package com.vinabeef.movement.application;

import com.vinabeef.movement.domain.HerdSession;
import com.vinabeef.movement.domain.Movement;
import com.vinabeef.movement.domain.SessionStatus;
import com.vinabeef.movement.persistence.HerdSessionRepository;
import com.vinabeef.movement.persistence.MovementRepository;
import com.vinabeef.shared.contract.Direction;
import com.vinabeef.shared.contract.GateReadEvent;
import com.vinabeef.shared.contract.MovementRecorded;
import com.vinabeef.shared.messaging.EventBroker;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Movement &amp; Counting Service (FR-03). It consumes gate read events from the broker, stores the
 * canonical movement and updates the barn's herd-session counts in one transaction, then publishes
 * {@link MovementRecorded}.
 *
 * <p>It only records and counts; deciding whether a movement is abnormal is the Anomaly Detection
 * Engine's job (SRP). Processing is idempotent on {@code eventId}, so a redelivered event after an
 * outage never creates a second record (P7, NFR-02).
 */
@Service
public class MovementCountingService {

    private static final Logger log = LoggerFactory.getLogger(MovementCountingService.class);

    private final MovementRepository movements;
    private final HerdSessionRepository sessions;
    private final EventBroker broker;
    private final Clock clock;

    public MovementCountingService(MovementRepository movements, HerdSessionRepository sessions,
                                   EventBroker broker, Clock clock) {
        this.movements = movements;
        this.sessions = sessions;
        this.broker = broker;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void onGateRead(GateReadEvent event) {
        if (movements.existsById(event.eventId())) {
            log.debug("Duplicate delivery of event {} ignored", event.eventId());
            return;
        }
        HerdSession session = updateHerdSession(event);
        Long sessionId = session == null ? null : session.getId();
        movements.save(Movement.record(event, sessionId, clock.instant()));

        broker.publish(new MovementRecorded(event.eventId(), event.rfidTag(), event.animalCode(), event.gate(),
                event.direction(), event.occurredAt(), event.classification(), event.whitelistVersion(), sessionId));
    }

    /** An OUT opens the barn's session if needed; an IN counts only against an open session. */
    private HerdSession updateHerdSession(GateReadEvent event) {
        long barnId = event.gate().barnId();
        Optional<HerdSession> open = sessions.findByBarnIdAndStatus(barnId, SessionStatus.OPEN);

        if (event.direction() == Direction.OUT) {
            HerdSession session = open.orElseGet(
                    () -> sessions.save(HerdSession.open(barnId, event.gate().barnCode(), event.occurredAt())));
            session.recordOut(event.rfidTag());
            return session;
        }
        open.ifPresent(session -> session.recordIn(event.rfidTag()));
        return open.orElse(null);
    }
}
