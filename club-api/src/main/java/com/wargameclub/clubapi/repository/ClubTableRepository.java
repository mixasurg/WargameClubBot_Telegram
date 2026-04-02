package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.ClubTable;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

/**
 * JPA-репозиторий для игровых столов клуба.
 */
public interface ClubTableRepository extends JpaRepository<ClubTable, Long> {

    /**
     * Возвращает и блокирует все активные столы.
     * Используется при расчете распределения столов, чтобы избежать гонок.
     *
     * @return список активных столов
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ClubTable t where t.isActive = true order by t.id")
    List<ClubTable> findActiveForUpdate();
}
