package com.wargameclub.clubapi.controller;

import com.wargameclub.clubapi.dto.LoyaltyDto;
import com.wargameclub.clubapi.service.LoyaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyControllerTest {

    @Mock
    private LoyaltyService loyaltyService;

    private LoyaltyController controller;

    @BeforeEach
    void setUp() {
        controller = new LoyaltyController(loyaltyService);
    }

    @Test
    void getReturnsDto() {
        when(loyaltyService.getPoints(10L)).thenReturn(42);

        LoyaltyDto dto = controller.get(10L);

        assertThat(dto.userId()).isEqualTo(10L);
        assertThat(dto.points()).isEqualTo(42);
    }
}
