package com.wargameclub.clubapi.service;

import java.util.List;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TableService {
    private final ClubTableRepository tableRepository;

    public TableService(ClubTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @Transactional(readOnly = true)
    public List<ClubTable> findAll() {
        return tableRepository.findAll();
    }
}

