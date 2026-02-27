package com.wargameclub.clubapi.service;

import java.util.List;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с игровыми столами.
 */
@Service
public class TableService {

    /**
     * Репозиторий столов клуба.
     */
    private final ClubTableRepository tableRepository;

    /**
     * Создает сервис столов.
     *
     * @param tableRepository репозиторий столов
     */
    public TableService(ClubTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    /**
     * Возвращает список всех столов.
     *
     * @return список столов
     */
    @Transactional(readOnly = true)
    public List<ClubTable> findAll() {
        return tableRepository.findAll();
    }
}
