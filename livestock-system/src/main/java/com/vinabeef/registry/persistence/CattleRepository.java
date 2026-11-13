package com.vinabeef.registry.persistence;

import com.vinabeef.registry.domain.Cattle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CattleRepository extends JpaRepository<Cattle, Long> {

    Optional<Cattle> findByRfidTag(String rfidTag);
}
