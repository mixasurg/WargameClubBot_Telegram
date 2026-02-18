package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.Army;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ArmyRepository extends JpaRepository<Army, Long>, JpaSpecificationExecutor<Army> {
}

