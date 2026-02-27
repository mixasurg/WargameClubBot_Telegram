package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.dto.WeekDigestDto;
import com.wargameclub.clubapi.service.DigestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigestControllerTest {

    @Mock
    private DigestService digestService;

    private DigestController controller;

    @BeforeEach
    void setUp() {
        controller = new DigestController(digestService);
    }

    @Test
    void weekDelegatesToService() {
        OffsetDateTime start = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-01-07T23:59:59Z");
        WeekDigestDto dto = new WeekDigestDto(start, end, "UTC", List.of(), List.of());
        when(digestService.getWeekDigest(1)).thenReturn(dto);

        WeekDigestDto result = controller.week(1);

        assertThat(result).isSameAs(dto);
        verify(digestService).getWeekDigest(1);
    }
}
