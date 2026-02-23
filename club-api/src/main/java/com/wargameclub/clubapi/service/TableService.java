package com.wargameclub.clubapi.service;

import java.util.List;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с столами.
 */
@Service
public class TableService {

    /**
     * Репозиторий стола клуба.
     */
    private final ClubTableRepository tableRepository;

    /**
     * Конструктор TableService.
     */
    public TableService(ClubTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    /**
     * Возвращает All.
     */
    @Transactional(readOnly = true)
    public List<ClubTable> findAll() {
        return tableRepository.findAll();
    }
}

