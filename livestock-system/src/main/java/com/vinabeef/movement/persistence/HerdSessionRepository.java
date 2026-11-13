package com.vinabeef.movement.persistence;

import com.vinabeef.movement.domain.HerdSession;
import com.vinabeef.movement.domain.SessionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HerdSessionRepository extends JpaRepository<HerdSession, Long> {

    Optional<HerdSession> findByBarnIdAndStatus(long barnId, SessionStatus status);

    List<HerdSession> findByStatusOrderByBarnCodeAsc(SessionStatus status);
}
