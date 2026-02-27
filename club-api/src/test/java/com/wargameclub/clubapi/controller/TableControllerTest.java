package com.wargameclub.clubapi.controller;

import java.util.List;
import com.wargameclub.clubapi.dto.TableDto;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableControllerTest {

    @Mock
    private TableService tableService;

    private TableController controller;

    @BeforeEach
    void setUp() {
        controller = new TableController(tableService);
    }

    @Test
    void listReturnsDtos() {
        ClubTable table = new ClubTable("T1", true, null);
        ReflectionTestUtils.setField(table, "id", 1L);
        when(tableService.findAll()).thenReturn(List.of(table));

        List<TableDto> result = controller.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }
}
