package com.vinabeef.movement.application;

import com.vinabeef.movement.domain.SessionStatus;
import com.vinabeef.movement.persistence.HerdSessionRepository;
import com.vinabeef.movement.persistence.MovementRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read side of the movement module, kept apart from the consumers that write (SRP). */
@Service
@Transactional(readOnly = true)
public class MovementQueryService {

    private final MovementRepository movements;
    private final HerdSessionRepository sessions;

    public MovementQueryService(MovementRepository movements, HerdSessionRepository sessions) {
        this.movements = movements;
        this.sessions = sessions;
    }

    public List<MovementView> recent(int limit) {
        return movements.findAllByOrderByReceivedAtDesc(Limit.of(limit)).stream().map(MovementView::of).toList();
    }

    public Optional<MovementView> find(String eventId) {
        return movements.findById(eventId).map(MovementView::of);
    }

    public long count() {
        return movements.count();
    }

    public List<HerdSessionView> openSessions() {
        return sessions.findByStatusOrderByBarnCodeAsc(SessionStatus.OPEN).stream().map(HerdSessionView::of).toList();
    }
}
