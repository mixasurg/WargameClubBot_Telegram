package com.wargameclub.clubapi;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.dto.BookingCreateRequest;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.exception.ConflictException;
import com.wargameclub.clubapi.messaging.KafkaEventPublisher;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import com.wargameclub.clubapi.repository.UserRepository;
import com.wargameclub.clubapi.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Класс модуля club-api.
 */
@SpringBootTest
@ActiveProfiles("test")
public class BookingServiceTest {

    /**
     * Сервис бронирования.
     */
    @Autowired
    private BookingService bookingService;

    /**
     * Репозиторий стола клуба.
     */
    @Autowired
    private ClubTableRepository tableRepository;

    /**
     * Репозиторий пользователя.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Поле состояния.
     */
    @MockBean
    private KafkaEventPublisher kafkaEventPublisher;

    /**
     * Выполняет операцию.
     */
    @Test
    void bookingOverlapIsRejected() {
        ClubTable table = tableRepository.save(new ClubTable("Table-1", true, null));
        User user = userRepository.save(new User("Alice"));

        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(2);

        /**
         * Выполняет операцию.
         */
        BookingCreateRequest request = new BookingCreateRequest(
                table.getId(),
                user.getId(),
                start,
                end,
                "Test game",
                2,
                null,
                null,
                null
        );
        bookingService.create(request);

        /**
         * Выполняет операцию.
         */
        BookingCreateRequest overlap = new BookingCreateRequest(
                table.getId(),
                user.getId(),
                start.plusMinutes(30),
                end.plusHours(1),
                "Test game",
                2,
                null,
                null,
                null
        );

        assertThrows(ConflictException.class, () -> bookingService.create(overlap));
    }
}

