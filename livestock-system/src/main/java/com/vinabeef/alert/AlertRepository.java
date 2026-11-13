package com.vinabeef.alert;

import com.vinabeef.shared.contract.AlertType;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

interface AlertRepository extends JpaRepository<Alert, Long> {

    boolean existsByTypeAndSourceRef(AlertType type, String sourceRef);

    List<Alert> findAllByOrderByRaisedAtDesc(Limit limit);

    long countByStatus(AlertStatus status);
}
