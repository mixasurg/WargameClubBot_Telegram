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
     * Возвращает игры с указанной активностью.
     *
     * @param isActive признак активности
     * @return список игр
     */
    List<GameCatalog> findByIsActive(boolean isActive);

    /**
     * Ищет игру по названию без учета регистра.
     *
     * @param name название игры
     * @return игра, если найдена
     */
    Optional<GameCatalog> findFirstByNameIgnoreCase(String name);
}
