package com.vinabeef.registry.persistence;

import com.vinabeef.registry.domain.Gate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GateRepository extends JpaRepository<Gate, Long> {
}
