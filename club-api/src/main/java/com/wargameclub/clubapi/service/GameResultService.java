package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.util.List;
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

/**
 * Сервис фиксации результатов игр и обновления статистики игроков.
 */
@Service
public class GameResultService {

    /**
     * Репозиторий бронирований.
     */
    private final BookingRepository bookingRepository;

    /**
     * Репозиторий результатов игр.
     */
    private final BookingResultRepository resultRepository;

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий статистики игроков.
     */
    private final UserGameStatsRepository statsRepository;

    /**
     * Создает сервис результатов игр.
     *
     * @param bookingRepository репозиторий бронирований
     * @param resultRepository репозиторий результатов
     * @param userRepository репозиторий пользователей
     * @param statsRepository репозиторий статистики игроков
     */
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

    /**
     * Фиксирует результат игры по бронированию и обновляет статистику.
     *
     * @param bookingId идентификатор бронирования
     * @param reporterUserId идентификатор пользователя, сообщившего результат
     * @param outcome исход игры
     */
    @Transactional
    public void recordResult(Long bookingId, Long reporterUserId, GameOutcome outcome) {
        if (bookingId == null || reporterUserId == null) {
            throw new BadRequestException("Некорректные параметры результата");
        }
        if (outcome == null) {
            throw new BadRequestException("Не задан результат игры");
        }
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
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

    /**
     * Возвращает статистику игрока (создает пустую при отсутствии).
     *
     * @param userId идентификатор пользователя
     * @return статистика пользователя
     */
    @Transactional(readOnly = true)
    public UserGameStats getStats(Long userId) {
        User user = findUser(userId);
        return statsRepository.findById(userId).orElseGet(() -> new UserGameStats(user));
    }

    /**
     * Возвращает агрегированную статистику результатов пользователя.
     *
     * @param userId идентификатор пользователя
     * @param from включительная нижняя граница по времени фиксации результата (опционально)
     * @return агрегированная статистика результатов
     */
    @Transactional(readOnly = true)
    public ResultSnapshot getResultSnapshot(Long userId, OffsetDateTime from) {
        findUser(userId);
        List<BookingResult> results = from == null
                ? resultRepository.findByUserId(userId)
                : resultRepository.findByUserIdAndRecordedAtFrom(userId, from);
        if (results == null || results.isEmpty()) {
            return new ResultSnapshot(0, 0, 0);
        }
        int wins = 0;
        int losses = 0;
        int draws = 0;
        for (BookingResult result : results) {
            if (result == null || result.getBooking() == null || result.getReporter() == null || result.getOutcome() == null) {
                continue;
            }
            boolean isReporter = userId.equals(result.getReporter().getId());
            switch (result.getOutcome()) {
                case WIN -> {
                    if (isReporter) {
                        wins++;
                    } else {
                        losses++;
                    }
                }
                case LOSS -> {
                    if (isReporter) {
                        losses++;
                    } else {
                        wins++;
                    }
                }
                case DRAW -> draws++;
            }
        }
        return new ResultSnapshot(wins, losses, draws);
    }

    /**
     * Проверяет, является ли пользователь участником бронирования.
     *
     * @param booking бронирование
     * @param user пользователь
     * @return true, если пользователь участвует
     */
    private boolean isParticipant(Booking booking, User user) {
        if (booking == null || user == null) {
            return false;
        }
        if (booking.getUser() != null && booking.getUser().getId().equals(user.getId())) {
            return true;
        }
        return booking.getOpponent() != null && booking.getOpponent().getId().equals(user.getId());
    }

    /**
     * Обновляет статистику по результату игры.
     *
     * @param booking бронирование
     * @param reporter пользователь, сообщивший результат
     * @param outcome исход игры
     */
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

    /**
     * Возвращает статистику пользователя или создает новую запись.
     *
     * @param user пользователь
     * @return статистика пользователя
     */
    private UserGameStats getOrCreateStats(User user) {
        return statsRepository.findById(user.getId()).orElseGet(() -> statsRepository.save(new UserGameStats(user)));
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь
     */
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
    }

    /**
     * Снимок результатов пользователя.
     *
     * @param wins количество побед
     * @param losses количество поражений
     * @param draws количество ничьих
     */
    public record ResultSnapshot(
            int wins,
            int losses,
            int draws
    ) {
        /**
         * Возвращает общее количество игр.
         *
         * @return количество игр
         */
        public int games() {
            return wins + losses + draws;
        }

        /**
         * Возвращает процент побед.
         *
         * @return процент побед в диапазоне 0..100
         */
        public double winRate() {
            if (games() == 0) {
                return 0.0;
            }
            return wins * 100.0 / games();
        }
    }
}
