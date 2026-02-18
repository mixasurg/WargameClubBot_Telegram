package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.BookingResult;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameResultService {
    private final BookingRepository bookingRepository;
    private final BookingResultRepository resultRepository;
    private final UserRepository userRepository;
    private final UserGameStatsRepository statsRepository;

    public GameResultService(
            BookingRepository bookingRepository,
            BookingResultRepository resultRepository,
            UserRepository userRepository,
            UserGameStatsRepository statsRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.statsRepository = statsRepository;
    }

    @Transactional
    public void recordResult(Long bookingId, Long reporterUserId, GameOutcome outcome) {
        if (bookingId == null || reporterUserId == null) {
            throw new BadRequestException("Некорректные параметры результата");
        }
        if (outcome == null) {
            throw new BadRequestException("Не задан результат игры");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));
        if (booking.getStatus() != BookingStatus.CREATED) {
            throw new BadRequestException("Нельзя указать результат для отмененного бронирования");
        }
        if (booking.getOpponent() == null) {
            throw new BadRequestException("Для бронирования нет соперника");
        }
        if (resultRepository.existsById(bookingId)) {
            throw new ConflictException("Результат уже зафиксирован");
        }
        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + reporterUserId));
        if (!isParticipant(booking, reporter)) {
            throw new BadRequestException("Пользователь не участвует в бронировании");
        }
        BookingResult result = new BookingResult(booking, reporter, outcome);
        resultRepository.save(result);
        updateStats(booking, reporter, outcome);
    }

    @Transactional(readOnly = true)
    public UserGameStats getStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        return statsRepository.findById(userId).orElseGet(() -> new UserGameStats(user));
    }

    private boolean isParticipant(Booking booking, User user) {
        if (booking == null || user == null) {
            return false;
        }
        if (booking.getUser() != null && booking.getUser().getId().equals(user.getId())) {
            return true;
        }
        return booking.getOpponent() != null && booking.getOpponent().getId().equals(user.getId());
    }

    private void updateStats(Booking booking, User reporter, GameOutcome outcome) {
        User opponent = booking.getUser().getId().equals(reporter.getId())
                ? booking.getOpponent()
                : booking.getUser();
        if (opponent == null) {
            return;
        }
        UserGameStats reporterStats = getOrCreateStats(reporter);
        UserGameStats opponentStats = getOrCreateStats(opponent);
        switch (outcome) {
            case WIN -> {
                reporterStats.setWins(reporterStats.getWins() + 1);
                opponentStats.setLosses(opponentStats.getLosses() + 1);
            }
            case LOSS -> {
                reporterStats.setLosses(reporterStats.getLosses() + 1);
                opponentStats.setWins(opponentStats.getWins() + 1);
            }
            case DRAW -> {
                reporterStats.setDraws(reporterStats.getDraws() + 1);
                opponentStats.setDraws(opponentStats.getDraws() + 1);
            }
        }
        OffsetDateTime now = OffsetDateTime.now();
        reporterStats.setUpdatedAt(now);
        opponentStats.setUpdatedAt(now);
    }

    private UserGameStats getOrCreateStats(User user) {
        return statsRepository.findById(user.getId()).orElseGet(() -> statsRepository.save(new UserGameStats(user)));
    }
}
