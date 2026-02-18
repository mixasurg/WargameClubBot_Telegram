package com.wargameclub.clubapi.service;

import java.util.List;
import com.wargameclub.clubapi.entity.GameCatalog;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.repository.GameCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameCatalogService {
    private final GameCatalogRepository repository;

    public GameCatalogService(GameCatalogRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<GameCatalog> list(Boolean active) {
        if (active == null) {
            return repository.findAll();
        }
        return repository.findByIsActive(active);
    }

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

