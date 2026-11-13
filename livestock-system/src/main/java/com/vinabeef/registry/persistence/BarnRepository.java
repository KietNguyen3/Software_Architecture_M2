package com.vinabeef.registry.persistence;

import com.vinabeef.registry.domain.Barn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarnRepository extends JpaRepository<Barn, Long> {
}
