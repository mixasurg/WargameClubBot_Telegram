package com.wargameclub.clubapi.service;

import java.util.Optional;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.entity.UserGameStats;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.enums.GameOutcome;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.ConflictException;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.BookingRepository;
import com.wargameclub.clubapi.repository.BookingResultRepository;
import com.wargameclub.clubapi.repository.UserGameStatsRepository;
import com.wargameclub.clubapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameResultServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingResultRepository resultRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserGameStatsRepository statsRepository;

    private GameResultService service;

    @BeforeEach
    void setUp() {
        service = new GameResultService(bookingRepository, resultRepository, userRepository, statsRepository);
    }

    @Test
    void recordResultUpdatesStatsForWin() {
        User user = new User("Reporter");
        ReflectionTestUtils.setField(user, "id", 1L);
        User opponent = new User("Opponent");
        ReflectionTestUtils.setField(opponent, "id", 2L);
        Booking booking = new Booking(new ClubTable("T1", true, null), user, null, null);
        booking.setOpponent(opponent);
        booking.setStatus(BookingStatus.CREATED);
        ReflectionTestUtils.setField(booking, "id", 10L);

        UserGameStats reporterStats = new UserGameStats(user);
        UserGameStats opponentStats = new UserGameStats(opponent);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(resultRepository.existsById(10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(statsRepository.findById(1L)).thenReturn(Optional.of(reporterStats));
        when(statsRepository.findById(2L)).thenReturn(Optional.of(opponentStats));

        service.recordResult(10L, 1L, GameOutcome.WIN);

        assertThat(reporterStats.getWins()).isEqualTo(1);
        assertThat(opponentStats.getLosses()).isEqualTo(1);
        verify(resultRepository).save(any());
    }

    @Test
    void recordResultRejectsNonParticipant() {
        User user = new User("Owner");
        ReflectionTestUtils.setField(user, "id", 1L);
        User opponent = new User("Opponent");
        ReflectionTestUtils.setField(opponent, "id", 2L);
        Booking booking = new Booking(new ClubTable("T1", true, null), user, null, null);
        booking.setOpponent(opponent);
        booking.setStatus(BookingStatus.CREATED);
        ReflectionTestUtils.setField(booking, "id", 10L);

        User outsider = new User("Outsider");
        ReflectionTestUtils.setField(outsider, "id", 3L);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(resultRepository.existsById(10L)).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> service.recordResult(10L, 3L, GameOutcome.DRAW))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void recordResultRejectsDuplicate() {
        User owner = new User("Owner");
        ReflectionTestUtils.setField(owner, "id", 1L);
        User opponent = new User("Opponent");
        ReflectionTestUtils.setField(opponent, "id", 2L);
        Booking booking = new Booking(new ClubTable("T1", true, null), owner, null, null);
        booking.setOpponent(opponent);
        booking.setStatus(BookingStatus.CREATED);
        when(resultRepository.existsById(10L)).thenReturn(true);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.recordResult(10L, 1L, GameOutcome.WIN))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getStatsCreatesEmptyWhenMissing() {
        User user = new User("Player");
        ReflectionTestUtils.setField(user, "id", 5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(statsRepository.findById(5L)).thenReturn(Optional.empty());

        UserGameStats stats = service.getStats(5L);

        assertThat(stats.getUser()).isSameAs(user);
    }

    @Test
    void getStatsThrowsWhenUserMissing() {
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStats(7L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getResultSnapshotUsesUnfilteredQueryWhenFromIsNull() {
        User user = new User("Player");
        ReflectionTestUtils.setField(user, "id", 11L);
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));
        when(resultRepository.findByUserId(11L)).thenReturn(java.util.List.of());

        GameResultService.ResultSnapshot snapshot = service.getResultSnapshot(11L, null);

        assertThat(snapshot.games()).isEqualTo(0);
        verify(resultRepository).findByUserId(11L);
    }

    @Test
    void getResultSnapshotUsesFilteredQueryWhenFromIsProvided() {
        User user = new User("Player");
        ReflectionTestUtils.setField(user, "id", 12L);
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        java.time.OffsetDateTime from = java.time.OffsetDateTime.now().minusDays(30);
        when(resultRepository.findByUserIdAndRecordedAtFrom(eq(12L), eq(from))).thenReturn(java.util.List.of());

        GameResultService.ResultSnapshot snapshot = service.getResultSnapshot(12L, from);

        assertThat(snapshot.games()).isEqualTo(0);
        verify(resultRepository).findByUserIdAndRecordedAtFrom(12L, from);
    }

    @Test
    void getResultSnapshotReturnsZeroWhenRepositoryReturnsNull() {
        User user = new User("Player");
        ReflectionTestUtils.setField(user, "id", 13L);
        when(userRepository.findById(13L)).thenReturn(Optional.of(user));
        when(resultRepository.findByUserId(13L)).thenReturn(null);

        GameResultService.ResultSnapshot snapshot = service.getResultSnapshot(13L, null);

        assertThat(snapshot.games()).isEqualTo(0);
        assertThat(snapshot.winRate()).isEqualTo(0.0);
        verify(resultRepository).findByUserId(13L);
    }
}
