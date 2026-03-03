package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.dto.BookingCreateRequest;
import com.wargameclub.clubapi.dto.BookingJoinRequest;
import com.wargameclub.clubapi.entity.Army;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.BookingMode;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.ConflictException;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.messaging.BookingCancelledEvent;
import com.wargameclub.clubapi.messaging.BookingCreatedEvent;
import com.wargameclub.clubapi.messaging.KafkaEventPublisher;
import com.wargameclub.clubapi.repository.ArmyRepository;
import com.wargameclub.clubapi.repository.BookingRepository;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import com.wargameclub.clubapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления бронированиями и распределением столов.
 */
@Service
public class BookingService {

    /**
     * Емкость одного стола в условных единицах.
     */
    private static final int TABLE_CAPACITY_UNITS = 2;

    /**
     * Дедлайн join для открытых броней по умолчанию: за 12 часов до старта.
     */
    private static final int OPEN_JOIN_DEADLINE_HOURS = 12;

    /**
     * Код причины автоотмены открытой брони.
     */
    private static final String OPEN_JOIN_TIMEOUT_REASON = "OPEN_JOIN_TIMEOUT";

    /**
     * Регулярное выражение для обновления фракции соперника в notes.
     */
    private static final Pattern OPPONENT_FACTION_NOTES_PATTERN =
            Pattern.compile("(?i)соперник\\s+фракция\\s*:\\s*[^;\\n]+;?\\s*");

    /**
     * Формат даты и времени в пользовательских уведомлениях.
     */
    private static final DateTimeFormatter NOTIFY_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Репозиторий бронирований.
     */
    private final BookingRepository bookingRepository;

    /**
     * Репозиторий столов клуба.
     */
    private final ClubTableRepository tableRepository;

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий армий.
     */
    private final ArmyRepository armyRepository;

    /**
     * Сериализатор JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Сервис автообновления Telegram-расписания.
     */
    private final TelegramAutoRefreshService autoRefreshService;

    /**
     * Публикатор событий в Kafka.
     */
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * Outbox-сервис уведомлений.
     */
    private final NotificationOutboxService outboxService;

    /**
     * Настройки приложения.
     */
    private final AppProperties appProperties;

    /**
     * Создает сервис бронирований.
     *
     * @param bookingRepository репозиторий бронирований
     * @param tableRepository репозиторий столов
     * @param userRepository репозиторий пользователей
     * @param armyRepository репозиторий армий
     * @param objectMapper сериализатор JSON
     * @param autoRefreshService сервис автообновления Telegram
     * @param kafkaEventPublisher публикатор событий Kafka
     * @param outboxService сервис outbox-уведомлений
     * @param appProperties настройки приложения
     */
    public BookingService(
            BookingRepository bookingRepository,
            ClubTableRepository tableRepository,
            UserRepository userRepository,
            ArmyRepository armyRepository,
            ObjectMapper objectMapper,
            TelegramAutoRefreshService autoRefreshService,
            KafkaEventPublisher kafkaEventPublisher,
            NotificationOutboxService outboxService,
            AppProperties appProperties
    ) {
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
        this.armyRepository = armyRepository;
        this.objectMapper = objectMapper;
        this.autoRefreshService = autoRefreshService;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.outboxService = outboxService;
        this.appProperties = appProperties;
    }

    /**
     * Создает новое бронирование, распределяет столы и публикует событие.
     *
     * @param request запрос на бронирование
     * @return созданное бронирование
     */
    @Transactional
    public Booking create(BookingCreateRequest request) {
        validateRange(request.startAt(), request.endAt());
        if (request.tableUnits() == null) {
            throw new BadRequestException("Поле tableUnits обязательно");
        }
        if (request.tableUnits() < 1 || request.tableUnits() > 6) {
            throw new BadRequestException("tableUnits должен быть в диапазоне от 1 до 6");
        }
        BookingMode mode = request.bookingMode() != null ? request.bookingMode() : BookingMode.FIXED;
        if (mode == BookingMode.OPEN && request.opponentUserId() != null) {
            throw new BadRequestException("Для OPEN-бронирования соперник не указывается при создании");
        }
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + request.userId()));
        User opponent = null;
        if (request.opponentUserId() != null) {
            if (request.opponentUserId().equals(request.userId())) {
                throw new BadRequestException("Соперник не может совпадать с пользователем");
            }
            opponent = userRepository.findById(request.opponentUserId())
                    .orElseThrow(() -> new NotFoundException("Соперник не найден: " + request.opponentUserId()));
        }
        Army army = resolveBookingArmy(request.armyId());
        List<Booking> overlapping = bookingRepository.findOverlappingWithDetails(
                BookingStatus.CREATED, request.startAt(), request.endAt());
        if (army != null && army.isClubShared() && hasArmyConflict(overlapping, army.getId())) {
            throw new ConflictException("Армия уже забронирована на это время");
        }

        List<TableAllocation> allocations = allocateTables(
                overlapping,
                request.tableUnits(),
                request.tableId()
        );

        String allocationsJson = serializeAllocations(allocations);
        ClubTable primaryTable = findTableById(allocations.get(0).tableId());
        OffsetDateTime joinDeadline = resolveJoinDeadline(request, mode);

        Booking booking = new Booking(primaryTable, user, request.startAt(), request.endAt());
        booking.setGame(request.game());
        booking.setTableUnits(request.tableUnits());
        booking.setOpponent(opponent);
        booking.setArmy(army);
        booking.setNotes(request.notes());
        booking.setTableAssignments(allocationsJson);
        booking.setBookingMode(mode);
        booking.setJoinDeadlineAt(joinDeadline);
        booking.setCancelReason(null);

        Booking saved = bookingRepository.save(booking);
        autoRefreshService.refreshTwoweeksIfWithinRange(saved.getStartAt());
        kafkaEventPublisher.publishBookingCreated(new BookingCreatedEvent(saved.getId()));
        return saved;
    }

    /**
     * Возвращает бронирования, пересекающие интервал времени, с опциональной фильтрацией по столу.
     *
     * @param from начало интервала
     * @param to конец интервала
     * @param tableId идентификатор стола (опционально)
     * @return список бронирований
     */
    @Transactional(readOnly = true)
    public List<Booking> findOverlapping(OffsetDateTime from, OffsetDateTime to, Long tableId) {
        validateRange(from, to);
        List<Booking> bookings = bookingRepository.findOverlappingWithDetails(BookingStatus.CREATED, from, to);
        if (tableId == null) {
            return bookings;
        }
        return bookings.stream()
                .filter(booking -> bookingHasTable(booking, tableId))
                .toList();
    }

    /**
     * Возвращает открытые бронирования, к которым можно присоединиться.
     *
     * @param from начало интервала
     * @param to конец интервала
     * @param game фильтр по игре (опционально)
     * @return список открытых бронирований
     */
    @Transactional(readOnly = true)
    public List<Booking> findOpen(OffsetDateTime from, OffsetDateTime to, String game) {
        validateRange(from, to);
        String gameFilter = game == null || game.isBlank() ? null : game.trim();
        OffsetDateTime now = OffsetDateTime.now();
        List<Booking> openBookings;
        if (gameFilter == null) {
            openBookings = bookingRepository.findOpenWithDetails(
                    BookingStatus.CREATED,
                    BookingMode.OPEN,
                    from,
                    to
            );
        } else {
            openBookings = bookingRepository.findOpenWithDetailsByGame(
                    BookingStatus.CREATED,
                    BookingMode.OPEN,
                    from,
                    to,
                    gameFilter
            );
        }
        return openBookings.stream()
                .filter(booking -> isOpenJoinAvailable(booking, now))
                .sorted(Comparator.comparing(Booking::getStartAt))
                .toList();
    }

    /**
     * Присоединяет пользователя к открытой игре.
     *
     * @param bookingId идентификатор бронирования
     * @param request данные присоединения
     * @return обновленное бронирование
     */
    @Transactional
    public Booking join(Long bookingId, BookingJoinRequest request) {
        if (request == null || request.userId() == null) {
            throw new BadRequestException("Поле userId обязательно");
        }
        OffsetDateTime now = OffsetDateTime.now();
        User joiner = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + request.userId()));
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));
        validateJoinAllowed(booking, joiner, now);
        Army opponentArmy = resolveJoinArmy(request.armyId(), joiner);
        if (opponentArmy != null && opponentArmy.isClubShared()) {
            List<Booking> overlapping = bookingRepository.findOverlappingWithDetails(
                    BookingStatus.CREATED,
                    booking.getStartAt(),
                    booking.getEndAt()
            );
            if (hasArmyConflict(overlapping, opponentArmy.getId())) {
                throw new ConflictException("Армия уже забронирована на это время");
            }
        }

        booking.setOpponent(joiner);
        booking.setBookingMode(BookingMode.FIXED);
        booking.setJoinDeadlineAt(null);
        booking.setCancelReason(null);
        String opponentFaction = request.faction();
        if ((opponentFaction == null || opponentFaction.isBlank()) && opponentArmy != null) {
            opponentFaction = opponentArmy.getFaction();
        }
        if (opponentFaction != null && !opponentFaction.isBlank()) {
            booking.setNotes(upsertOpponentFactionInNotes(booking.getNotes(), opponentFaction));
        }

        autoRefreshService.refreshTwoweeksIfWithinRange(booking.getStartAt());
        kafkaEventPublisher.publishBookingCreated(new BookingCreatedEvent(booking.getId()));
        enqueueJoinNotifications(booking, joiner);
        return booking;
    }

    /**
     * Отменяет бронирование и публикует событие отмены.
     *
     * @param bookingId идентификатор бронирования
     * @return отмененное бронирование
     */
    @Transactional
    public Booking cancel(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(null);
        autoRefreshService.refreshTwoweeksIfWithinRange(booking.getStartAt());
        kafkaEventPublisher.publishBookingCancelled(new BookingCancelledEvent(booking.getId()));
        return booking;
    }

    /**
     * Автоматически снимает открытые бронирования с истекшим дедлайном.
     *
     * @return число отмененных бронирований
     */
    @Transactional
    public int cancelExpiredOpenBookings() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Booking> expired = bookingRepository.findExpiredOpenForUpdate(
                BookingStatus.CREATED,
                BookingMode.OPEN,
                now
        );
        int cancelled = 0;
        for (Booking booking : expired) {
            if (booking.getOpponent() != null || booking.getStatus() != BookingStatus.CREATED) {
                continue;
            }
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelReason(OPEN_JOIN_TIMEOUT_REASON);
            autoRefreshService.refreshTwoweeksIfWithinRange(booking.getStartAt());
            kafkaEventPublisher.publishBookingCancelled(new BookingCancelledEvent(booking.getId()));
            enqueueOpenAutoCancelNotification(booking);
            cancelled++;
        }
        return cancelled;
    }

    /**
     * Проверяет наличие пересечения по клубной армии в списке бронирований.
     *
     * @param bookings список бронирований
     * @param armyId идентификатор армии
     * @return true, если армия уже используется в пересекающемся бронировании
     */
    private boolean hasArmyConflict(List<Booking> bookings, Long armyId) {
        return bookings.stream()
                .anyMatch(booking -> booking.getArmy() != null && armyId.equals(booking.getArmy().getId()));
    }

    /**
     * Разрешает армию для создания бронирования.
     *
     * @param armyId идентификатор армии
     * @return армия или null
     */
    private Army resolveBookingArmy(Long armyId) {
        if (armyId == null) {
            return null;
        }
        Army army = armyRepository.findById(armyId)
                .orElseThrow(() -> new NotFoundException("Армия не найдена: " + armyId));
        if (!army.isActive()) {
            throw new BadRequestException("Армия неактивна");
        }
        return army;
    }

    /**
     * Вычисляет дедлайн join для бронирования.
     *
     * @param request запрос на создание
     * @param mode режим бронирования
     * @return дедлайн join или null
     */
    private OffsetDateTime resolveJoinDeadline(BookingCreateRequest request, BookingMode mode) {
        if (mode != BookingMode.OPEN) {
            return null;
        }
        OffsetDateTime deadline = request.joinDeadlineAt() != null
                ? request.joinDeadlineAt()
                : request.startAt().minusHours(OPEN_JOIN_DEADLINE_HOURS);
        if (!deadline.isBefore(request.startAt())) {
            throw new BadRequestException("joinDeadlineAt должен быть раньше startAt");
        }
        if (!deadline.isAfter(OffsetDateTime.now())) {
            throw new BadRequestException("Срок присоединения уже истек");
        }
        return deadline;
    }

    /**
     * Валидирует возможность присоединения к открытой игре.
     *
     * @param booking бронирование
     * @param joiner пользователь, который присоединяется
     * @param now текущий момент
     */
    private void validateJoinAllowed(Booking booking, User joiner, OffsetDateTime now) {
        if (booking.getStatus() != BookingStatus.CREATED) {
            throw new ConflictException("Бронирование неактивно");
        }
        if (booking.getBookingMode() != BookingMode.OPEN) {
            throw new ConflictException("Бронирование не находится в режиме OPEN");
        }
        if (booking.getOpponent() != null) {
            throw new ConflictException("К бронированию уже присоединились");
        }
        if (booking.getUser() != null && booking.getUser().getId().equals(joiner.getId())) {
            throw new BadRequestException("Нельзя присоединиться к собственной игре");
        }
        if (!isOpenJoinAvailable(booking, now)) {
            throw new ConflictException("Время присоединения к бронированию истекло");
        }
    }

    /**
     * Разрешает армию для присоединения к открытой игре.
     *
     * @param armyId идентификатор армии
     * @param joiner пользователь, который присоединяется
     * @return армия или null
     */
    private Army resolveJoinArmy(Long armyId, User joiner) {
        if (armyId == null) {
            return null;
        }
        Army army = armyRepository.findById(armyId)
                .orElseThrow(() -> new NotFoundException("Армия не найдена: " + armyId));
        if (!army.isActive()) {
            throw new BadRequestException("Армия неактивна");
        }
        if (!army.isClubShared()) {
            if (army.getOwner() == null || army.getOwner().getId() == null
                    || !army.getOwner().getId().equals(joiner.getId())) {
                throw new BadRequestException("Нельзя выбрать чужую личную армию");
            }
        }
        return army;
    }

    /**
     * Проверяет доступность join для открытой брони.
     *
     * @param booking бронирование
     * @param now текущий момент
     * @return true, если присоединение допустимо
     */
    private boolean isOpenJoinAvailable(Booking booking, OffsetDateTime now) {
        if (booking == null || booking.getStartAt() == null) {
            return false;
        }
        if (!booking.getStartAt().isAfter(now)) {
            return false;
        }
        if (booking.getJoinDeadlineAt() != null && !booking.getJoinDeadlineAt().isAfter(now)) {
            return false;
        }
        return true;
    }

    /**
     * Встраивает или обновляет фракцию соперника в поле notes.
     *
     * @param notes исходный текст заметок
     * @param faction фракция соперника
     * @return обновленные заметки
     */
    private String upsertOpponentFactionInNotes(String notes, String faction) {
        String normalizedFaction = faction == null ? null : faction.trim();
        if (normalizedFaction == null || normalizedFaction.isBlank()) {
            return notes;
        }
        String existing = notes == null ? "" : notes.trim();
        String cleaned = OPPONENT_FACTION_NOTES_PATTERN.matcher(existing).replaceAll("").trim();
        String suffix = "Соперник фракция: " + normalizedFaction + ";";
        if (cleaned.isBlank()) {
            return suffix;
        }
        if (!cleaned.endsWith(";")) {
            cleaned = cleaned + ";";
        }
        return (cleaned + " " + suffix).trim();
    }

    /**
     * Ставит личное уведомление о присоединении к игре.
     *
     * @param booking обновленное бронирование
     * @param joiner присоединившийся пользователь
     */
    private void enqueueJoinNotifications(Booking booking, User joiner) {
        User owner = booking.getUser();
        String game = booking.getGame() == null || booking.getGame().isBlank() ? "-" : booking.getGame();
        String when = formatBookingStart(booking);
        enqueuePrivateTelegram(owner, "К вашей открытой игре присоединился "
                + joiner.getName() + ".\nИгра: " + game + "\nКогда: " + when + "\nБронь #" + booking.getId());
        enqueuePrivateTelegram(joiner, "Вы присоединились к игре #" + booking.getId()
                + ".\nАвтор: " + owner.getName() + "\nИгра: " + game + "\nКогда: " + when);
    }

    /**
     * Ставит личное уведомление об автоотмене открытой игры.
     *
     * @param booking отмененное бронирование
     */
    private void enqueueOpenAutoCancelNotification(Booking booking) {
        User owner = booking.getUser();
        String game = booking.getGame() == null || booking.getGame().isBlank() ? "-" : booking.getGame();
        String when = formatBookingStart(booking);
        enqueuePrivateTelegram(owner,
                "Открытая заявка #" + booking.getId() + " автоматически снята: соперник не найден до дедлайна.\n"
                        + "Игра: " + game + "\nКогда: " + when);
    }

    /**
     * Ставит личное Telegram-уведомление пользователю.
     *
     * @param user пользователь
     * @param text текст уведомления
     */
    private void enqueuePrivateTelegram(User user, String text) {
        if (user == null || user.getTelegramId() == null || text == null || text.isBlank()) {
            return;
        }
        outboxService.enqueue(
                NotificationTarget.TELEGRAM,
                new ChatRouting(user.getTelegramId(), null),
                text
        );
    }

    /**
     * Форматирует старт бронирования для уведомления.
     *
     * @param booking бронирование
     * @return форматированная дата и время
     */
    private String formatBookingStart(Booking booking) {
        if (booking == null || booking.getStartAt() == null) {
            return "-";
        }
        ZoneId zoneId = appProperties.getTimezone() != null ? appProperties.getTimezone() : ZoneId.of("Europe/Moscow");
        return booking.getStartAt().atZoneSameInstant(zoneId).format(NOTIFY_TIME_FORMAT);
    }

    /**
     * Распределяет требуемые единицы столов с учетом текущих бронирований.
     *
     * @param overlapping пересекающиеся бронирования
     * @param requestedUnits требуемые единицы столов
     * @param preferredTableId предпочитаемый стол (опционально)
     * @return список распределений по столам
     */
    private List<TableAllocation> allocateTables(List<Booking> overlapping, int requestedUnits, Long preferredTableId) {
        List<ClubTable> tables = tableRepository.findAll().stream()
                .filter(ClubTable::isActive)
                .toList();
        if (tables.isEmpty()) {
            throw new NotFoundException("Столы не настроены");
        }
        if (preferredTableId != null && tables.stream().noneMatch(table -> table.getId().equals(preferredTableId))) {
            throw new BadRequestException("Предпочтительный стол неактивен или отсутствует");
        }

        Map<Long, Integer> usedUnits = new HashMap<>();
        for (ClubTable table : tables) {
            usedUnits.put(table.getId(), 0);
        }

        for (Booking booking : overlapping) {
            for (TableAllocation allocation : parseAllocations(booking)) {
                if (usedUnits.containsKey(allocation.tableId())) {
                    usedUnits.merge(allocation.tableId(), allocation.units(), Integer::sum);
                }
            }
        }

        List<Integer> requiredAllocations = new ArrayList<>(expandAllocations(requestedUnits));
        List<TableAllocation> allocations = new ArrayList<>();

        if (preferredTableId != null && !requiredAllocations.isEmpty()) {
            int preferredUnits = requiredAllocations.contains(TABLE_CAPACITY_UNITS) ? TABLE_CAPACITY_UNITS : 1;
            if (!canAllocate(preferredTableId, usedUnits, preferredUnits)) {
                throw new ConflictException("Предпочтительный стол недоступен");
            }
            allocations.add(new TableAllocation(preferredTableId, preferredUnits));
            usedUnits.merge(preferredTableId, preferredUnits, Integer::sum);
            requiredAllocations.remove(Integer.valueOf(preferredUnits));
        }

        for (int units : requiredAllocations) {
            Long tableId = findBestTable(usedUnits, units);
            if (tableId == null) {
                throw new ConflictException("Недостаточно свободных столов на это время");
            }
            allocations.add(new TableAllocation(tableId, units));
            usedUnits.merge(tableId, units, Integer::sum);
        }
        return allocations;
    }

    /**
     * Проверяет, можно ли разместить указанное количество единиц на столе.
     *
     * @param tableId идентификатор стола
     * @param usedUnits текущая занятость по столам
     * @param units требуемые единицы
     * @return true, если размещение возможно
     */
    private boolean canAllocate(Long tableId, Map<Long, Integer> usedUnits, int units) {
        int used = usedUnits.getOrDefault(tableId, 0);
        return used + units <= TABLE_CAPACITY_UNITS;
    }

    /**
     * Подбирает наиболее подходящий стол с учетом требуемых единиц.
     *
     * @param usedUnits текущая занятость по столам
     * @param units требуемые единицы
     * @return идентификатор стола или null, если нет доступных
     */
    private Long findBestTable(Map<Long, Integer> usedUnits, int units) {
        if (units == TABLE_CAPACITY_UNITS) {
            return usedUnits.entrySet().stream()
                    .filter(entry -> entry.getValue() + units <= TABLE_CAPACITY_UNITS)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
        }
        return usedUnits.entrySet().stream()
                .filter(entry -> entry.getValue() + units <= TABLE_CAPACITY_UNITS)
                .sorted(Comparator.comparingInt(entry -> entry.getValue() == 1 ? 0 : 1))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Разворачивает запрошенное количество единиц в список распределений по столам.
     *
     * @param requestedUnits запрошенные единицы столов
     * @return список единиц для распределения
     */
    private List<Integer> expandAllocations(int requestedUnits) {
        List<Integer> allocations = new ArrayList<>();
        if (requestedUnits == 1) {
            allocations.add(1);
        } else if (requestedUnits == 2) {
            allocations.add(2);
        } else if (requestedUnits == 3) {
            allocations.add(2);
            allocations.add(2);
        } else if (requestedUnits % 2 == 0) {
            int tables = requestedUnits / 2;
            for (int i = 0; i < tables; i++) {
                allocations.add(2);
            }
        } else {
            throw new BadRequestException("Неподдерживаемое значение tableUnits");
        }
        return allocations;
    }

    /**
     * Возвращает стол по идентификатору.
     *
     * @param tableId идентификатор стола
     * @return стол
     */
    private ClubTable findTableById(Long tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Стол не найден: " + tableId));
    }

    /**
     * Разбирает сохраненные назначения столов из JSON.
     *
     * @param booking бронирование
     * @return список назначений столов
     */
    private List<TableAllocation> parseAllocations(Booking booking) {
        if (booking.getTableAssignments() == null || booking.getTableAssignments().isBlank()) {
            if (booking.getTable() == null) {
                return List.of();
            }
            int units = booking.getTableUnits() >= 2 ? 2 : 1;
            return List.of(new TableAllocation(booking.getTable().getId(), units));
        }
        try {
            return objectMapper.readValue(
                    booking.getTableAssignments(),
                    new TypeReference<List<TableAllocation>>() {
                    }
            );
        } catch (JsonProcessingException ex) {
            if (booking.getTable() == null) {
                return List.of();
            }
            int units = booking.getTableUnits() >= 2 ? 2 : 1;
            return List.of(new TableAllocation(booking.getTable().getId(), units));
        }
    }

    /**
     * Сериализует назначения столов в JSON.
     *
     * @param allocations список назначений
     * @return JSON-строка
     */
    private String serializeAllocations(List<TableAllocation> allocations) {
        try {
            return objectMapper.writeValueAsString(allocations);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Не удалось сериализовать назначения столов");
        }
    }

    /**
     * Проверяет, содержит ли бронирование указанный стол.
     *
     * @param booking бронирование
     * @param tableId идентификатор стола
     * @return true, если стол присутствует в назначениях
     */
    private boolean bookingHasTable(Booking booking, Long tableId) {
        return parseAllocations(booking).stream()
                .anyMatch(allocation -> allocation.tableId().equals(tableId));
    }

    /**
     * Проверяет корректность временного интервала.
     *
     * @param startAt начало интервала
     * @param endAt конец интервала
     */
    private void validateRange(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new BadRequestException("Некорректный диапазон времени");
        }
    }

    /**
     * Назначение части бронирования на конкретный стол.
     *
     * @param tableId идентификатор стола
     * @param units количество единиц стола
     */
    private record TableAllocation(Long tableId, int units) {
    }
}
