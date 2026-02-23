package com.wargameclub.clubapi.repository;

import java.util.List;
import java.util.Optional;
import com.wargameclub.clubapi.entity.GameCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для каталога игр.
 */
public interface GameCatalogRepository extends JpaRepository<GameCatalog, Long> {

    /**
     * Возвращает каталог игр.
     */
    List<GameCatalog> findByIsActive(boolean isActive);

    /**
     * Возвращает FirstByNameIgnoreCase.
     */
    Optional<GameCatalog> findFirstByNameIgnoreCase(String name);
}

