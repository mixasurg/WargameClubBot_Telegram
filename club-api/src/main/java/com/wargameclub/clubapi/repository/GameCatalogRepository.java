package com.wargameclub.clubapi.repository;

import java.util.List;
import java.util.Optional;
import com.wargameclub.clubapi.entity.GameCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameCatalogRepository extends JpaRepository<GameCatalog, Long> {
    List<GameCatalog> findByIsActive(boolean isActive);

    Optional<GameCatalog> findFirstByNameIgnoreCase(String name);
}

