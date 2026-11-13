package com.vinabeef.movement.persistence;

import com.vinabeef.movement.domain.Movement;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementRepository extends JpaRepository<Movement, String> {

    List<Movement> findAllByOrderByReceivedAtDesc(Limit limit);
}
