package com.wargameclub.clubapi.service;

import java.util.List;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private ClubTableRepository repository;

    private TableService service;

    @BeforeEach
    void setUp() {
        service = new TableService(repository);
    }

    @Test
    void findAllReturnsRepositoryData() {
        ClubTable table = new ClubTable("T1", true, null);
        when(repository.findAll()).thenReturn(List.of(table));

        List<ClubTable> result = service.findAll();

        assertThat(result).containsExactly(table);
    }
}
