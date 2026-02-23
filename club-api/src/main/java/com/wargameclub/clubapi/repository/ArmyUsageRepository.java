package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.ArmyUsage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для использования армии.
 */
public interface ArmyUsageRepository extends JpaRepository<ArmyUsage, Long> {
}

