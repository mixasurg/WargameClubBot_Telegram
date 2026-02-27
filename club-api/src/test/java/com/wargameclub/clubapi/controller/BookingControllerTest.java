package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.dto.BookingCreateRequest;
import com.wargameclub.clubapi.dto.BookingDto;
import com.wargameclub.clubapi.dto.BookingResultRequest;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.GameOutcome;
import com.wargameclub.clubapi.service.BookingService;
import com.wargameclub.clubapi.service.GameResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;
    @Mock
    private GameResultService resultService;

    private BookingController controller;

    @BeforeEach
    void setUp() {
        controller = new BookingController(bookingService, resultService);
    }

    @Test
    void createReturnsDto() {
        ClubTable table = new ClubTable("T1", true, null);
        ReflectionTestUtils.setField(table, "id", 1L);
        User user = new User("Alice");
        ReflectionTestUtils.setField(user, "id", 2L);
        Booking booking = new Booking(table, user, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));
        ReflectionTestUtils.setField(booking, "id", 10L);

        when(bookingService.create(any(BookingCreateRequest.class))).thenReturn(booking);

        BookingDto dto = controller.create(new BookingCreateRequest(
                table.getId(),
                user.getId(),
                booking.getStartAt(),
                booking.getEndAt(),
                "Game",
                2,
                null,
                null,
                null
        ));

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.userId()).isEqualTo(2L);
    }

    @Test
    void listDelegatesToService() {
        Booking booking = new Booking(new ClubTable("T1", true, null), new User("Alice"),
                OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));
        ReflectionTestUtils.setField(booking, "id", 1L);
        when(bookingService.findOverlapping(any(), any(), any())).thenReturn(List.of(booking));

        List<BookingDto> result = controller.list(OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), null);

        assertThat(result).hasSize(1);
    }

    @Test
    void resultDelegatesToService() {
        controller.result(10L, new BookingResultRequest(2L, GameOutcome.WIN));

        verify(resultService).recordResult(10L, 2L, GameOutcome.WIN);
    }
}
