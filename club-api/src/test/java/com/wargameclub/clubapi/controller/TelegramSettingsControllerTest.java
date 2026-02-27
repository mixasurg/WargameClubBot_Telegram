package com.wargameclub.clubapi.controller;

import java.util.Optional;
import com.wargameclub.clubapi.dto.TelegramSettingsDto;
import com.wargameclub.clubapi.dto.TelegramSettingsUpdateRequest;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.service.TelegramSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramSettingsControllerTest {

    @Mock
    private TelegramSettingsService settingsService;

    private TelegramSettingsController controller;

    @BeforeEach
    void setUp() {
        controller = new TelegramSettingsController(settingsService);
    }

    @Test
    void getReturnsDtoWhenPresent() {
        ClubTelegramSettings settings = new ClubTelegramSettings(100L);
        settings.setScheduleThreadId(5);
        when(settingsService.getAny()).thenReturn(Optional.of(settings));

        TelegramSettingsDto dto = controller.get();

        assertThat(dto.chatId()).isEqualTo(100L);
        assertThat(dto.scheduleThreadId()).isEqualTo(5);
    }

    @Test
    void getThrowsWhenMissing() {
        when(settingsService.getAny()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.get())
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateDelegatesToService() {
        ClubTelegramSettings settings = new ClubTelegramSettings(100L);
        settings.setScheduleThreadId(5);
        when(settingsService.upsert(100L, 5, null, null, null, null, "UTC"))
                .thenReturn(settings);

        TelegramSettingsDto dto = controller.update(new TelegramSettingsUpdateRequest(
                100L,
                5,
                null,
                null,
                null,
                null,
                "UTC"
        ));

        assertThat(dto.chatId()).isEqualTo(100L);
        verify(settingsService).upsert(100L, 5, null, null, null, null, "UTC");
    }
}
