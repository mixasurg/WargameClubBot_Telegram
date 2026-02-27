package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.ClubTable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для игровых столов клуба.
 */
public interface ClubTableRepository extends JpaRepository<ClubTable, Long> {
}
