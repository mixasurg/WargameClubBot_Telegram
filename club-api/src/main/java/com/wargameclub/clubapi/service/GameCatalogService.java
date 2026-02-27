package com.wargameclub.clubapi.service;

import java.util.List;
import com.wargameclub.clubapi.entity.GameCatalog;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.repository.GameCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления каталогом игр.
 */
@Service
public class GameCatalogService {

    /**
     * Репозиторий каталога игр.
     */
    private final GameCatalogRepository repository;

    /**
     * Создает сервис каталога игр.
     *
     * @param repository репозиторий каталога игр
     */
    public GameCatalogService(GameCatalogRepository repository) {
        this.repository = repository;
    }

    /**
     * Возвращает список игр с учетом фильтра активности.
     *
     * @param active фильтр активности (опционально)
     * @return список игр
     */
    @Transactional(readOnly = true)
    public List<GameCatalog> list(Boolean active) {
        if (active == null) {
            return repository.findAll();
        }
        return repository.findByIsActive(active);
    }

    /**
     * Создает игру или возвращает существующую с таким же названием.
     *
     * @param name название игры
     * @param defaultDurationMinutes длительность по умолчанию
     * @param tableUnits требуемое количество единиц стола
     * @return созданная или найденная запись каталога
     */
    @Transactional
    public GameCatalog createOrGet(String name, int defaultDurationMinutes, int tableUnits) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new BadRequestException("Название игры не может быть пустым");
        }
        if (normalized.length() > 120) {
            throw new BadRequestException("Название игры слишком длинное");
        }
        return repository.findFirstByNameIgnoreCase(normalized)
                .orElseGet(() -> repository.save(new GameCatalog(normalized, defaultDurationMinutes, tableUnits)));
    }
}
