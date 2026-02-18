package com.wargameclub.clubbot.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.wargameclub.clubbot.client.ClubApiClient;
import com.wargameclub.clubbot.config.BotProperties;
import com.wargameclub.clubbot.dto.ArmyDto;
import com.wargameclub.clubbot.dto.ArmyCreateRequest;
import com.wargameclub.clubbot.dto.BookingCreateRequest;
import com.wargameclub.clubbot.dto.BookingResultRequest;
import com.wargameclub.clubbot.dto.EventCreateRequest;
import com.wargameclub.clubbot.dto.EventDto;
import com.wargameclub.clubbot.dto.GameDto;
import com.wargameclub.clubbot.dto.GameCreateRequest;
import com.wargameclub.clubbot.dto.TelegramSettingsDto;
import com.wargameclub.clubbot.dto.TelegramSettingsUpdateRequest;
import com.wargameclub.clubbot.dto.UserDto;
import com.wargameclub.clubbot.dto.WeekDigestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Component
public class TelegramClubBot extends TelegramLongPollingBot implements NotificationDispatcher {
    private static final Logger log = LoggerFactory.getLogger(TelegramClubBot.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("ru", "RU"));
    private static final String CALLBACK_NOOP = "noop";
    private static final String DATE_TARGET_BOOKING = "b";
    private static final String DATE_TARGET_EVENT = "e";
    private static final String TIME_TARGET_BOOKING_START = "bs";
    private static final String TIME_TARGET_EVENT_START = "es";
    private static final String TIME_TARGET_EVENT_END = "ee";
    private static final String EVENT_TITLE_PREFIX = "etitle";
    private static final String BTN_BOOK = "Записаться на игру";
    private static final String BTN_EVENT = "Создать мероприятие";
    private static final String BTN_CANCEL = "Отменить";
    private static final String BTN_HELP = "Помощь";
    private static final String CMD_REFRESH_TWOWEEKS = "__cmd:refresh_twoweeks__";
    private static final String CMD_REFRESH_EVENTS = "__cmd:refresh_events__";
    private static final String CMD_RESULT_PROMPT_PREFIX = "__cmd:result_prompt__";

    private final BotProperties botProperties;
    private final ClubApiClient apiClient;
    private final DigestFormatter digestFormatter;
    private final EventsFormatter eventsFormatter;
    private final Map<Long, ConversationState> conversations = new ConcurrentHashMap<>();

    public TelegramClubBot(
            BotProperties botProperties,
            ClubApiClient apiClient,
            DigestFormatter digestFormatter,
            EventsFormatter eventsFormatter
    ) {
        this.botProperties = botProperties;
        this.apiClient = apiClient;
        this.digestFormatter = digestFormatter;
        this.eventsFormatter = eventsFormatter;
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) {
            return;
        }
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Message message = update.getMessage();
        if (message.getChat() != null && message.getChat().isUserChat()) {
            handlePrivateMessage(message);
            return;
        }
        handleGroupMessage(message);
    }

    @Override
    public void dispatch(NotificationMessage message) throws TelegramApiException {
        if (message == null || message.text() == null) {
            return;
        }
        if (CMD_REFRESH_TWOWEEKS.equals(message.text())) {
            refreshTwoWeeksFromOutbox(message);
            return;
        }
        if (CMD_REFRESH_EVENTS.equals(message.text())) {
            refreshEventsFromOutbox(message);
            return;
        }
        if (message.text().startsWith(CMD_RESULT_PROMPT_PREFIX)) {
            sendResultPrompt(message);
            return;
        }
        SendMessage send = new SendMessage();
        send.setChatId(message.chatId().toString());
        send.setText(message.text());
        if (message.threadId() != null) {
            send.setMessageThreadId(message.threadId());
        }
        try {
            execute(send);
        } catch (TelegramApiRequestException ex) {
            if (message.threadId() != null && isThreadNotFound(ex)) {
                SendMessage fallback = new SendMessage();
                fallback.setChatId(message.chatId().toString());
                fallback.setText(message.text());
                execute(fallback);
                clearScheduleThreadIfMatches(message.chatId(), message.threadId());
                return;
            }
            throw ex;
        }
    }

    private void handleGroupMessage(Message message) {
        String text = message.getText().trim();
        if (!text.startsWith("/")) {
            return;
        }
        String command = normalizeCommand(text);
        switch (command) {
            case "/set_schedule_topic" -> handleSetScheduleTopic(message);
            case "/week" -> handleWeek(message, 0);
            case "/nextweek" -> handleWeek(message, 1);
            case "/twoweeks" -> handleTwoWeeks(message);
            case "/events" -> handleEvents(message);
            case "/help" -> handleHelp(message);
            default -> sendText(message.getChatId(), message.getMessageThreadId(), "Неизвестная команда. Используйте /help");
        }
    }

    private void handlePrivateMessage(Message message) {
        String text = message.getText().trim();
        if (handlePrivateButton(message, text)) {
            return;
        }
        if (text.startsWith("/")) {
            handlePrivateCommand(message, text);
            return;
        }

        ConversationState state = conversations.get(message.getFrom().getId());
        if (state == null) {
            sendPrivateHelp(message);
            return;
        }

        if (state.getFlow() == ConversationState.Flow.BOOKING) {
            handleBookingStep(state, message, text);
            return;
        }
        handleEventStep(state, message, text);
    }

    private void handlePrivateCommand(Message message, String text) {
        String command = normalizeCommand(text);
        switch (command) {
            case "/start", "/help" -> sendPrivateHelp(message);
            case "/book" -> startBooking(message);
            case "/event" -> startEvent(message);
            case "/cancel" -> {
                conversations.remove(message.getFrom().getId());
                sendText(message.getChatId(), null, "Диалог отменен.", buildPrivateMenuKeyboard());
            }
            default -> sendText(message.getChatId(), null, "Команда не распознана. Используйте кнопки ниже.");
        }
    }

    private void startBooking(Message message) {
        ConversationState state = new ConversationState();
        state.setFlow(ConversationState.Flow.BOOKING);
        state.setStep(ConversationState.Step.PICK_DATE);
        UserDto user = apiClient.upsertTelegramUser(message.getFrom().getId(), resolveUserName(message.getFrom()));
        state.setUserId(user.id());
        state.setUserName(user.name());
        conversations.put(message.getFrom().getId(), state);
        YearMonth month = YearMonth.now(resolveTimezone());
        sendDatePicker(message.getChatId(), null, "Выберите дату игры", DATE_TARGET_BOOKING, month);
    }

    private void startEvent(Message message) {
        ConversationState state = new ConversationState();
        state.setFlow(ConversationState.Flow.EVENT);
        state.setStep(ConversationState.Step.EVENT_TITLE);
        UserDto user = apiClient.upsertTelegramUser(message.getFrom().getId(), resolveUserName(message.getFrom()));
        state.setUserId(user.id());
        state.setUserName(user.name());
        conversations.put(message.getFrom().getId(), state);
        try {
            List<String> titles = apiClient.getEventTitles(20);
            state.setAvailableEventTitles(titles);
            if (titles == null || titles.isEmpty()) {
                sendText(message.getChatId(), null, "Введите название мероприятия.");
                return;
            }
            sendText(message.getChatId(), null, "Выберите название мероприятия:", buildEventTitleKeyboard(titles));
        } catch (HttpClientErrorException ex) {
            log.warn("Не удалось получить список мероприятий: {}", ex.getStatusCode());
            sendText(message.getChatId(), null, "Введите название мероприятия.");
        }
    }
    private void handleBookingStep(ConversationState state, Message message, String text) {
        switch (state.getStep()) {
            case PICK_DATE -> handleBookingDate(state, message, text);
            case PICK_TIME -> handleBookingTime(state, message, text);
            case PICK_GAME -> handleBookingGame(state, message, text);
            case PICK_CUSTOM_GAME -> handleCustomGame(state, message, text);
            case PICK_DURATION -> handleDuration(state, message, text);
            case PICK_TABLE_UNITS -> handleTableUnits(state, message, text);
            case PICK_OPPONENT -> handleOpponent(state, message, text);
            case PICK_OPPONENT_FACTION -> handleOpponentFaction(state, message, text);
            case PICK_ARMY_TYPE -> handleArmyType(state, message, text);
            case PICK_ARMY -> handleArmySelection(state, message, text);
            case PICK_OWN_FACTION -> handleOwnFaction(state, message, text);
            case PICK_CLUB_FACTION -> handleClubFaction(state, message, text);
            case CONFIRM -> handleBookingConfirm(state, message, text);
            default -> sendText(message.getChatId(), null, "Неожиданный шаг, начните заново /book");
        }
    }

    private void handleEventStep(ConversationState state, Message message, String text) {
        switch (state.getStep()) {
            case EVENT_TITLE -> {
                state.setEventTitle(text);
                state.setCustomEventTitle(isCustomEventTitle(state, text));
                state.setStep(ConversationState.Step.EVENT_TYPE);
                sendText(message.getChatId(), null, "Выберите тип мероприятия:", buildEventTypeKeyboard());
            }
            case EVENT_TITLE_CUSTOM -> {
                state.setEventTitle(text);
                state.setCustomEventTitle(true);
                state.setStep(ConversationState.Step.EVENT_TYPE);
                sendText(message.getChatId(), null, "Выберите тип мероприятия:", buildEventTypeKeyboard());
            }
            case EVENT_TYPE -> {
                String type = parseEventType(text);
                if (type == null) {
                    sendText(message.getChatId(), null, "Укажите тип мероприятия:", buildEventTypeKeyboard());
                    return;
                }
                state.setEventType(type);
                state.setStep(ConversationState.Step.EVENT_DATE);
                YearMonth month = YearMonth.now(resolveTimezone());
                sendDatePicker(message.getChatId(), null, "Выберите дату мероприятия", DATE_TARGET_EVENT, month);
            }
            case EVENT_DATE -> {
                LocalDate date = parseDate(text, resolveTimezone());
                if (date == null) {
                    sendText(message.getChatId(), null, "Неверная дата. Формат ДД.ММ или ДД.ММ.ГГГГ.");
                    YearMonth month = YearMonth.now(resolveTimezone());
                    sendDatePicker(message.getChatId(), null, "Выберите дату мероприятия", DATE_TARGET_EVENT, month);
                    return;
                }
                state.setDate(date);
                state.setStep(ConversationState.Step.EVENT_TIME);
                sendTimePicker(message.getChatId(), null, "Выберите время начала", TIME_TARGET_EVENT_START);
            }
            case EVENT_TIME -> {
                LocalTime time = parseTime(text);
                if (time == null) {
                    sendText(message.getChatId(), null, "Неверное время. Формат ЧЧ:ММ.");
                    return;
                }
                handleEventStartTimeSelected(state, message, time);
            }
            case EVENT_END_TIME -> {
                LocalTime time = parseTime(text);
                if (time == null) {
                    sendText(message.getChatId(), null, "Неверное время. Формат ЧЧ:ММ.");
                    return;
                }
                handleEventEndTimeSelected(state, message, time);
            }
            case EVENT_DESCRIPTION -> {
                if (!"-".equals(text)) {
                    state.setEventDescription(text);
                }
                state.setStep(ConversationState.Step.EVENT_CONFIRM);
                sendText(message.getChatId(), null, buildEventSummary(state), buildConfirmKeyboard());
            }
            case EVENT_CONFIRM -> handleEventConfirm(state, message, text);
            default -> sendText(message.getChatId(), null, "Неожиданный шаг, начните заново /event");
        }
    }

    private void handleBookingDate(ConversationState state, Message message, String text) {
        LocalDate date = parseDate(text, resolveTimezone());
        if (date == null) {
            sendText(message.getChatId(), null, "Неверная дата. Формат ДД.ММ или ДД.ММ.ГГГГ.");
            YearMonth month = YearMonth.now(resolveTimezone());
            sendDatePicker(message.getChatId(), null, "Выберите дату игры", DATE_TARGET_BOOKING, month);
            return;
        }
        state.setDate(date);
        state.setStep(ConversationState.Step.PICK_TIME);
        sendTimePicker(message.getChatId(), null, "Выберите время начала", TIME_TARGET_BOOKING_START);
    }

    private void handleBookingTime(ConversationState state, Message message, String text) {
        LocalTime time = parseTime(text);
        if (time == null) {
            sendText(message.getChatId(), null, "Неверное время. Формат ЧЧ:ММ.");
            return;
        }
        handleBookingTimeSelected(state, message, time);
    }

    private void handleBookingGame(ConversationState state, Message message, String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("другая")) {
            state.setStep(ConversationState.Step.PICK_CUSTOM_GAME);
            state.setCustomGame(true);
            sendText(message.getChatId(), null, "Введите название игры.");
            return;
        }
        int index = parseIndex(text);
        if (index == 0) {
            state.setStep(ConversationState.Step.PICK_CUSTOM_GAME);
            state.setCustomGame(true);
            sendText(message.getChatId(), null, "Введите название игры.");
            return;
        }
        List<GameDto> games = state.getAvailableGames();
        if (games == null || index < 1 || index > games.size()) {
            if (games != null && !games.isEmpty()) {
                sendText(message.getChatId(), null, "Выберите игру:", buildGameKeyboard(games));
            } else {
                sendText(message.getChatId(), null, "Введите название игры.");
                state.setStep(ConversationState.Step.PICK_CUSTOM_GAME);
            }
            return;
        }
        GameDto game = games.get(index - 1);
        state.setGame(game.name());
        state.setDurationMinutes(game.defaultDurationMinutes());
        state.setTableUnits(game.tableUnits());
        state.setCustomGame(false);
        state.setStep(ConversationState.Step.PICK_DURATION);
        sendText(message.getChatId(), null,
                "Обычно " + game.defaultDurationMinutes() + " минут. Отправьте число минут или 'по умолчанию'.");
    }

    private void handleCustomGame(ConversationState state, Message message, String text) {
        state.setGame(text);
        state.setDurationMinutes(null);
        state.setTableUnits(null);
        state.setCustomGame(true);
        state.setStep(ConversationState.Step.PICK_DURATION);
        sendText(message.getChatId(), null, "Сколько минут будет игра?");
    }

    private void handleDuration(ConversationState state, Message message, String text) {
        Integer duration = parseDuration(text, state.getDurationMinutes());
        if (duration == null || duration <= 0) {
            sendText(message.getChatId(), null, "Введите число минут или 'по умолчанию'.");
            return;
        }
        state.setDurationMinutes(duration);
        state.setStep(ConversationState.Step.PICK_TABLE_UNITS);
        String hint = state.getTableUnits() != null ? " (по умолчанию " + formatTableUnits(state.getTableUnits()) + ")" : "";
        sendText(message.getChatId(), null,
                "Сколько столов нужно?" + hint, buildTableUnitsKeyboard(state.getTableUnits()));
    }

    private void handleTableUnits(ConversationState state, Message message, String text) {
        Integer units = parseTableUnits(text, state.getTableUnits());
        if (units == null) {
            sendText(message.getChatId(), null, "Выберите количество столов:", buildTableUnitsKeyboard(state.getTableUnits()));
            return;
        }
        state.setTableUnits(units);
        state.setEndAt(state.getStartAt().plusMinutes(state.getDurationMinutes()));
        state.setStep(ConversationState.Step.PICK_OPPONENT);
        sendText(message.getChatId(), null, "С кем будете играть? @username или имя, '-' если один.");
    }

    private void handleOpponent(ConversationState state, Message message, String text) {
        if (state.getFoundUsers() != null && parseIndex(text) > 0) {
            int index = parseIndex(text);
            List<UserDto> candidates = state.getFoundUsers();
            if (index >= 1 && index <= candidates.size()) {
                UserDto user = candidates.get(index - 1);
                state.setOpponentUserId(user.id());
                state.setOpponentName(user.name());
                state.setFoundUsers(null);
                startOpponentFactionSelection(state, message);
                return;
            }
            sendText(message.getChatId(), null, "Выберите номер из списка.");
            return;
        }

        if (text.equalsIgnoreCase("-") || text.equalsIgnoreCase("нет")) {
            state.setOpponentName(null);
            state.setOpponentUserId(null);
            state.setOpponentFaction(null);
            state.setStep(ConversationState.Step.PICK_ARMY_TYPE);
            sendText(message.getChatId(), null, "Выберите тип армии:", buildArmyTypeKeyboard());
            return;
        }

        String query = normalizeUserQuery(text);
        if (query.isBlank()) {
            sendText(message.getChatId(), null, "Укажите имя соперника или '-' если играете один.");
            return;
        }
        List<UserDto> users = apiClient.searchUsers(query);
        if (users == null || users.isEmpty()) {
            state.setOpponentName(query);
            state.setOpponentUserId(null);
            state.setOpponentFaction(null);
            state.setStep(ConversationState.Step.PICK_ARMY_TYPE);
            sendText(message.getChatId(), null, "Выберите тип армии:", buildArmyTypeKeyboard());
            return;
        }
        if (users.size() == 1) {
            UserDto user = users.get(0);
            state.setOpponentUserId(user.id());
            state.setOpponentName(user.name());
            startOpponentFactionSelection(state, message);
            return;
        }
        state.setFoundUsers(users);
        sendText(message.getChatId(), null, "Несколько совпадений, выберите игрока:", buildOpponentKeyboard(users));
    }

    private void handleArmyType(ConversationState state, Message message, String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        if ("1".equals(normalized) || normalized.contains("своя") || normalized.contains("own")) {
            startOwnFactionSelection(state, message);
            return;
        }
        if ("2".equals(normalized) || normalized.contains("клуб") || normalized.contains("club")) {
            startClubArmySelection(state, message);
            return;
        }
        sendText(message.getChatId(), null, "Выберите тип армии:", buildArmyTypeKeyboard());
    }

    private void handleArmySelection(ConversationState state, Message message, String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        if ("0".equals(normalized) || normalized.contains("добав")) {
            startClubFactionInput(state, message);
            return;
        }
        int index = parseIndex(text);
        List<ArmyDto> armies = state.getAvailableArmies();
        if (armies == null || index < 1 || index > armies.size()) {
            if (armies != null && !armies.isEmpty()) {
                sendText(message.getChatId(), null, "Выберите клубную армию:", buildArmyKeyboard(armies));
            } else {
                sendText(message.getChatId(), null, "Нет доступных клубных армий.");
            }
            return;
        }
        ArmyDto army = armies.get(index - 1);
        state.setArmyId(army.id());
        state.setArmyLabel(army.game() + " / " + army.faction());
        state.setStep(ConversationState.Step.CONFIRM);
        sendText(message.getChatId(), null, buildBookingSummary(state), buildConfirmKeyboard());
    }

    private void handleOpponentFaction(ConversationState state, Message message, String text) {
        String normalized = text != null ? text.trim().toLowerCase(Locale.ROOT) : "";
        if (text != null && (text.equalsIgnoreCase("-") || text.equalsIgnoreCase("нет"))) {
            state.setOpponentFaction(null);
            state.setStep(ConversationState.Step.PICK_ARMY_TYPE);
            sendText(message.getChatId(), null, "Выберите тип армии:", buildArmyTypeKeyboard());
            return;
        }
        List<String> factions = state.getAvailableFactions();
        if (factions != null && !factions.isEmpty()) {
            if (normalized.contains("друг")) {
                state.setAvailableFactions(null);
                sendText(message.getChatId(), null, "Введите фракцию соперника (или '-' чтобы пропустить).");
                return;
            }
            if (!normalized.matches("\\d+")) {
                promptOpponentFaction(state, message);
                return;
            }
        }
        String faction = resolveFactionSelection(state, text);
        if (faction == null) {
            promptOpponentFaction(state, message);
            return;
        }
        state.setOpponentFaction(faction);
        state.setStep(ConversationState.Step.PICK_ARMY_TYPE);
        sendText(message.getChatId(), null, "Выберите тип армии:", buildArmyTypeKeyboard());
    }

    private void handleOwnFaction(ConversationState state, Message message, String text) {
        String normalized = text != null ? text.trim().toLowerCase(Locale.ROOT) : "";
        List<String> factions = state.getAvailableFactions();
        if (factions != null && !factions.isEmpty()) {
            if (normalized.contains("друг")) {
                state.setAvailableFactions(null);
                sendText(message.getChatId(), null, "Введите название фракции.");
                return;
            }
            if (!normalized.matches("\\d+")) {
                promptOwnFaction(state, message);
                return;
            }
        }
        String faction = resolveFactionSelection(state, text);
        if (faction == null) {
            promptOwnFaction(state, message);
            return;
        }
        applyOwnFactionSelection(state, faction);
        sendText(message.getChatId(), null, buildBookingSummary(state), buildConfirmKeyboard());
    }

    private void handleClubFaction(ConversationState state, Message message, String text) {
        String faction = text != null ? text.trim() : "";
        if (faction.isBlank()) {
            promptClubFaction(state, message);
            return;
        }
        applyClubFactionSelection(state, faction);
        sendText(message.getChatId(), null, buildBookingSummary(state), buildConfirmKeyboard());
    }

    private void startOwnFactionSelection(ConversationState state, Message message) {
        state.setClubArmy(false);
        state.setArmyId(null);
        state.setArmyLabel(null);
        state.setFaction(null);
        state.setAvailableFactions(null);

        List<ArmyDto> armies = null;
        if (state.getGame() != null && !state.getGame().isBlank()) {
            armies = apiClient.getArmies(state.getGame(), null, true);
        }
        state.setAvailableGameArmies(armies);
        List<String> factions = extractFactions(armies);
        if (factions.isEmpty()) {
            state.setStep(ConversationState.Step.PICK_OWN_FACTION);
            sendText(message.getChatId(), null, "Введите фракцию вашей армии.");
            return;
        }
        state.setAvailableFactions(factions);
        state.setStep(ConversationState.Step.PICK_OWN_FACTION);
        sendText(message.getChatId(), null, "Выберите фракцию или введите свою:", buildFactionKeyboard(factions));
    }

    private void startOpponentFactionSelection(ConversationState state, Message message) {
        if (state.getOpponentUserId() == null) {
            state.setStep(ConversationState.Step.PICK_ARMY_TYPE);
            sendText(message.getChatId(), null, "Выберите тип армии:", buildArmyTypeKeyboard());
            return;
        }
        state.setOpponentFaction(null);
        List<ArmyDto> armies = null;
        if (state.getGame() != null && !state.getGame().isBlank()) {
            armies = apiClient.getArmies(state.getGame(), null, true);
        }
        List<String> factions = extractFactions(armies);
        state.setAvailableFactions(factions.isEmpty() ? null : factions);
        state.setStep(ConversationState.Step.PICK_OPPONENT_FACTION);
        if (factions.isEmpty()) {
            sendText(message.getChatId(), null, "Укажите фракцию соперника (или '-' чтобы пропустить).");
            return;
        }
        sendText(message.getChatId(), null,
                "Укажите фракцию соперника (или '-' чтобы пропустить):",
                buildFactionKeyboard(factions));
    }

    private void startClubArmySelection(ConversationState state, Message message) {
        state.setClubArmy(true);
        state.setArmyId(null);
        state.setArmyLabel(null);
        state.setFaction(null);

        List<ArmyDto> armies = apiClient.getClubArmies();
        state.setAvailableArmies(armies);
        if (armies == null || armies.isEmpty()) {
            state.setStep(ConversationState.Step.PICK_CLUB_FACTION);
            sendText(message.getChatId(), null, "Нет доступных клубных армий. Введите фракцию, чтобы добавить.");
            return;
        }
        state.setStep(ConversationState.Step.PICK_ARMY);
        sendText(message.getChatId(), null, "Выберите клубную армию или добавьте новую:", buildArmyKeyboard(armies));
    }

    private void startClubFactionInput(ConversationState state, Message message) {
        state.setClubArmy(true);
        state.setArmyId(null);
        state.setArmyLabel(null);
        state.setFaction(null);
        state.setStep(ConversationState.Step.PICK_CLUB_FACTION);
        if (state.getGame() != null && !state.getGame().isBlank()) {
            sendText(message.getChatId(), null, "Введите фракцию клубной армии для игры " + state.getGame() + ".");
            return;
        }
        sendText(message.getChatId(), null, "Введите фракцию клубной армии.");
    }

    private void promptOwnFaction(ConversationState state, Message message) {
        List<String> factions = state.getAvailableFactions();
        if (factions != null && !factions.isEmpty()) {
            sendText(message.getChatId(), null, "Выберите фракцию или введите свою:", buildFactionKeyboard(factions));
            return;
        }
        sendText(message.getChatId(), null, "Введите фракцию вашей армии.");
    }

    private void promptOpponentFaction(ConversationState state, Message message) {
        List<String> factions = state.getAvailableFactions();
        if (factions != null && !factions.isEmpty()) {
            sendText(message.getChatId(), null,
                    "Укажите фракцию соперника (или '-' чтобы пропустить):",
                    buildFactionKeyboard(factions));
            return;
        }
        sendText(message.getChatId(), null, "Укажите фракцию соперника (или '-' чтобы пропустить).");
    }

    private void promptClubFaction(ConversationState state, Message message) {
        if (state.getGame() != null && !state.getGame().isBlank()) {
            sendText(message.getChatId(), null, "Введите фракцию клубной армии для игры " + state.getGame() + ".");
            return;
        }
        sendText(message.getChatId(), null, "Введите фракцию клубной армии.");
    }

    private String resolveFactionSelection(ConversationState state, String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        List<String> factions = state.getAvailableFactions();
        if (trimmed.matches("\\d+") && factions != null && !factions.isEmpty()) {
            int index = parseIndex(trimmed);
            if (index >= 1 && index <= factions.size()) {
                return factions.get(index - 1);
            }
            return null;
        }
        return trimmed;
    }

    private void applyOwnFactionSelection(ConversationState state, String faction) {
        state.setClubArmy(false);
        state.setFaction(faction);
        state.setArmyLabel(state.getGame() + " / " + faction);
        ArmyDto existing = findExistingPersonalArmy(state, faction);
        if (existing != null) {
            state.setArmyId(existing.id());
        }
        state.setStep(ConversationState.Step.CONFIRM);
    }

    private void applyClubFactionSelection(ConversationState state, String faction) {
        state.setClubArmy(true);
        state.setFaction(faction);
        state.setArmyLabel(state.getGame() + " / " + faction);
        ArmyDto existing = findExistingClubArmy(state, faction);
        if (existing != null) {
            state.setArmyId(existing.id());
        }
        state.setStep(ConversationState.Step.CONFIRM);
    }

    private ArmyDto findExistingPersonalArmy(ConversationState state, String faction) {
        List<ArmyDto> armies = state.getAvailableGameArmies();
        if (armies == null || faction == null) {
            return null;
        }
        for (ArmyDto army : armies) {
            if (army == null) {
                continue;
            }
            if (army.isClubShared()) {
                continue;
            }
            if (army.ownerUserId() == null || !army.ownerUserId().equals(state.getUserId())) {
                continue;
            }
            if (army.faction() != null && army.faction().equalsIgnoreCase(faction)) {
                return army;
            }
        }
        return null;
    }

    private ArmyDto findExistingClubArmy(ConversationState state, String faction) {
        List<ArmyDto> armies = state.getAvailableArmies();
        if (armies == null || faction == null) {
            return null;
        }
        for (ArmyDto army : armies) {
            if (army == null) {
                continue;
            }
            if (!army.isClubShared()) {
                continue;
            }
            if (army.faction() != null && army.faction().equalsIgnoreCase(faction)) {
                return army;
            }
        }
        return null;
    }

    private List<String> extractFactions(List<ArmyDto> armies) {
        if (armies == null || armies.isEmpty()) {
            return List.of();
        }
        Map<String, String> unique = new LinkedHashMap<>();
        for (ArmyDto army : armies) {
            if (army == null || army.faction() == null) {
                continue;
            }
            String faction = army.faction().trim();
            if (faction.isBlank()) {
                continue;
            }
            String key = faction.toLowerCase(Locale.ROOT);
            unique.putIfAbsent(key, faction);
        }
        return new ArrayList<>(unique.values());
    }

    private void handleBookingConfirm(ConversationState state, Message message, String text) {
        if (isYes(text)) {
            if (!ensureArmyForBooking(state, message)) {
                conversations.remove(message.getFrom().getId());
                return;
            }
            if (!ensureOpponentArmyForBooking(state, message)) {
                conversations.remove(message.getFrom().getId());
                return;
            }
            String notes = buildNotes(state);
            BookingCreateRequest request = new BookingCreateRequest(
                    null,
                    state.getUserId(),
                    state.getStartAt(),
                    state.getEndAt(),
                    state.getGame(),
                    state.getTableUnits(),
                    state.getOpponentUserId(),
                    state.getArmyId(),
                    notes
            );
            try {
                apiClient.createBooking(request);
                sendText(message.getChatId(), null, "Бронирование создано.");
                if (state.isCustomGame() && state.getGame() != null
                        && state.getDurationMinutes() != null && state.getTableUnits() != null) {
                    try {
                        apiClient.createGame(new GameCreateRequest(
                                state.getGame().trim(),
                                state.getDurationMinutes(),
                                state.getTableUnits()
                        ));
                    } catch (HttpClientErrorException ex) {
                        log.warn("Не удалось сохранить игру в каталоге: {}", ex.getStatusCode());
                    }
                }
            } catch (RestClientResponseException ex) {
                log.warn("Ошибка бронирования: {} {}", ex.getRawStatusCode(), ex.getStatusText());
                sendText(message.getChatId(), null, "Ошибка бронирования: " + ex.getRawStatusCode());
            } catch (RestClientException ex) {
                log.warn("Ошибка бронирования: {}", ex.getMessage());
                sendText(message.getChatId(), null, "Ошибка бронирования: не удалось связаться с API.");
            }
            conversations.remove(message.getFrom().getId());
            return;
        }
        if (isNo(text)) {
            conversations.remove(message.getFrom().getId());
            sendText(message.getChatId(), null, "Бронирование отменено.");
            return;
        }
        sendText(message.getChatId(), null, "Подтвердите действие:", buildConfirmKeyboard());
    }

    private void handleEventConfirm(ConversationState state, Message message, String text) {
        if (!isYes(text) && !isNo(text)) {
            sendText(message.getChatId(), null, "Подтвердите действие:", buildConfirmKeyboard());
            return;
        }
        if (isNo(text)) {
            conversations.remove(message.getFrom().getId());
            sendText(message.getChatId(), null, "Мероприятие отменено.");
            return;
        }
        EventCreateRequest request = new EventCreateRequest(
                state.getEventTitle(),
                state.getEventType(),
                state.getEventDescription(),
                state.getStartAt(),
                state.getEndAt(),
                state.getUserId(),
                null
        );
        try {
            EventDto created = apiClient.createEvent(request);
            BookingCreateRequest bookingRequest = new BookingCreateRequest(
                    null,
                    state.getUserId(),
                    state.getStartAt(),
                    state.getEndAt(),
                    "Мероприятие: " + state.getEventTitle(),
                    6,
                    null,
                    null,
                    "Зарезервировано под мероприятие " + created.id()
            );
            try {
                apiClient.createBooking(bookingRequest);
                sendText(message.getChatId(), null, "Мероприятие создано и столы зарезервированы.");
            } catch (HttpClientErrorException ex) {
                sendText(message.getChatId(), null, "Мероприятие создано, но столы не зарезервированы: " + ex.getStatusCode());
            }
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка создания мероприятия: {} {}", ex.getRawStatusCode(), ex.getStatusText());
            sendText(message.getChatId(), null, "Ошибка создания мероприятия: " + ex.getRawStatusCode());
        } catch (RestClientException ex) {
            log.warn("Ошибка создания мероприятия: {}", ex.getMessage());
            sendText(message.getChatId(), null, "Ошибка создания мероприятия: не удалось связаться с API.");
        }
        conversations.remove(message.getFrom().getId());
    }
    private void handleSetScheduleTopic(Message message) {
        Integer threadId = message.getMessageThreadId();
        if (threadId == null) {
            TelegramSettingsUpdateRequest request = new TelegramSettingsUpdateRequest(
                    message.getChatId(),
                    0,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            apiClient.updateTelegramSettings(request);
            sendText(message.getChatId(), null, "Топик расписания сброшен, сообщения будут в чате.");
            return;
        }
        TelegramSettingsUpdateRequest request = new TelegramSettingsUpdateRequest(
                message.getChatId(),
                threadId,
                null,
                null,
                null,
                null,
                null
        );
        apiClient.updateTelegramSettings(request);
        sendText(message.getChatId(), threadId, "Топик расписания сохранен.");
    }

    private void handleWeek(Message message, int offset) {
        WeekDigestDto digest = apiClient.getWeekDigest(offset);
        TelegramSettingsDto settings = apiClient.getTelegramSettings();
        Integer threadId = settings != null && settings.scheduleThreadId() != null
                ? settings.scheduleThreadId()
                : message.getMessageThreadId();
        String text = digestFormatter.format(digest);
        sendText(message.getChatId(), threadId, text);
    }

    private void handleTwoWeeks(Message message) {
        TelegramSettingsDto settings = apiClient.getTelegramSettings();
        Long chatId = message.getChatId();
        Integer threadId = settings != null && settings.scheduleThreadId() != null
                ? settings.scheduleThreadId()
                : message.getMessageThreadId();
        try {
            refreshTwoWeeks(chatId, threadId, sameChatSettings(settings, chatId));
        } catch (Exception ex) {
            log.warn("Не удалось отправить дайджест на две недели", ex);
            sendText(message.getChatId(), message.getMessageThreadId(), "Не удалось отправить дайджест на две недели.");
        }
    }

    private void handleEvents(Message message) {
        TelegramSettingsDto settings = apiClient.getTelegramSettings();
        Long chatId = message.getChatId();
        Integer threadId = settings != null && settings.scheduleThreadId() != null
                ? settings.scheduleThreadId()
                : message.getMessageThreadId();
        try {
            refreshEvents(chatId, threadId, sameChatSettings(settings, chatId));
        } catch (Exception ex) {
            log.warn("Не удалось отправить список мероприятий", ex);
            sendText(message.getChatId(), message.getMessageThreadId(), "Не удалось отправить список мероприятий.");
        }
    }

    private void refreshTwoWeeksFromOutbox(NotificationMessage message) throws TelegramApiException {
        TelegramSettingsDto settings = apiClient.getTelegramSettings();
        if (settings == null || settings.chatId() == null) {
            throw new IllegalStateException("Настройки Telegram не заданы");
        }
        Integer threadId = settings.scheduleThreadId() != null ? settings.scheduleThreadId() : message.threadId();
        refreshTwoWeeks(settings.chatId(), threadId, settings);
    }

    private void refreshEventsFromOutbox(NotificationMessage message) throws TelegramApiException {
        TelegramSettingsDto settings = apiClient.getTelegramSettings();
        if (settings == null || settings.chatId() == null) {
            throw new IllegalStateException("Настройки Telegram не заданы");
        }
        Integer threadId = settings.scheduleThreadId() != null ? settings.scheduleThreadId() : message.threadId();
        refreshEvents(settings.chatId(), threadId, settings);
    }

    private void sendResultPrompt(NotificationMessage message) throws TelegramApiException {
        Long bookingId = parseResultBookingId(message.text());
        if (bookingId == null || message.chatId() == null) {
            return;
        }
        SendMessage send = new SendMessage();
        send.setChatId(message.chatId().toString());
        send.setText("Игра завершена. Кто победил?");
        send.setReplyMarkup(buildResultKeyboard(bookingId));
        execute(send);
    }

    private Long parseResultBookingId(String text) {
        if (text == null || !text.startsWith(CMD_RESULT_PROMPT_PREFIX)) {
            return null;
        }
        String payload = text.substring(CMD_RESULT_PROMPT_PREFIX.length()).trim();
        if (payload.startsWith(":")) {
            payload = payload.substring(1).trim();
        }
        try {
            return Long.parseLong(payload);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void refreshTwoWeeks(Long chatId, Integer threadId, TelegramSettingsDto settings) throws TelegramApiException {
        if (chatId == null) {
            return;
        }
        if (settings != null) {
            deleteMessageSafe(chatId, settings.scheduleTwoweeksMessageId());
            deleteMessageSafe(chatId, settings.scheduleTwoweeksNextMessageId());
        }
        WeekDigestDto current = apiClient.getWeekDigest(0);
        WeekDigestDto next = apiClient.getWeekDigest(1);
        Integer currentMessageId = sendTextAndGetMessageId(chatId, threadId, digestFormatter.format(current));
        Integer nextMessageId = sendTextAndGetMessageId(chatId, threadId, digestFormatter.format(next));
        if (settings != null) {
            TelegramSettingsUpdateRequest request = new TelegramSettingsUpdateRequest(
                    chatId,
                    null,
                    null,
                    currentMessageId,
                    nextMessageId,
                    null,
                    null
            );
            apiClient.updateTelegramSettings(request);
        }
    }

    private void refreshEvents(Long chatId, Integer threadId, TelegramSettingsDto settings) throws TelegramApiException {
        if (chatId == null) {
            return;
        }
        if (settings != null) {
            deleteMessageSafe(chatId, settings.eventsMessageId());
        }
        ZoneId zoneId = resolveTimezone(settings);
        OffsetDateTime from = OffsetDateTime.now(zoneId);
        OffsetDateTime to = from.plusDays(14);
        List<EventDto> events = apiClient.getEvents(from, to);
        String timezone = settings != null && settings.timezone() != null ? settings.timezone() : "Europe/Moscow";
        Integer messageId = sendTextAndGetMessageId(chatId, threadId, eventsFormatter.formatUpcoming(events, timezone));
        if (settings != null) {
            TelegramSettingsUpdateRequest request = new TelegramSettingsUpdateRequest(
                    chatId,
                    null,
                    null,
                    null,
                    null,
                    messageId,
                    null
            );
            apiClient.updateTelegramSettings(request);
        }
    }

    private void handleHelp(Message message) {
        String text = "Команды:\n"
                + "/set_schedule_topic - привязать текущий топик к расписанию\n"
                + "/week - отправить расписание на эту неделю\n"
                + "/nextweek - отправить расписание на следующую неделю\n"
                + "/twoweeks - отправить расписание на две недели\n"
                + "/events - отправить ближайшие мероприятия\n"
                + "/help - показать это сообщение";
        sendText(message.getChatId(), message.getMessageThreadId(), text);
    }

    private String privateHelp() {
        return "Личные действия:\n"
                + BTN_BOOK + " - записаться на игру\n"
                + BTN_EVENT + " - создать мероприятие\n"
                + BTN_CANCEL + " - отменить диалог\n"
                + BTN_HELP + " - показать это сообщение";
    }

    private void sendPrivateHelp(Message message) {
        sendText(message.getChatId(), null, privateHelp(), buildPrivateMenuKeyboard());
    }

    private boolean handlePrivateButton(Message message, String text) {
        return switch (text) {
            case BTN_BOOK -> {
                startBooking(message);
                yield true;
            }
            case BTN_EVENT -> {
                startEvent(message);
                yield true;
            }
            case BTN_CANCEL -> {
                conversations.remove(message.getFrom().getId());
                sendText(message.getChatId(), null, "Диалог отменен.", buildPrivateMenuKeyboard());
                yield true;
            }
            case BTN_HELP -> {
                sendPrivateHelp(message);
                yield true;
            }
            default -> false;
        };
    }

    private ReplyKeyboardMarkup buildPrivateMenuKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        keyboard.setSelective(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add(BTN_BOOK);
        row1.add(BTN_EVENT);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(BTN_CANCEL);
        row2.add(BTN_HELP);

        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void sendText(Long chatId, Integer threadId, String text) {
        try {
            SendMessage send = new SendMessage();
            send.setChatId(chatId.toString());
            send.setText(text);
            send.setReplyMarkup(null);
            if (threadId != null) {
                send.setMessageThreadId(threadId);
            }
            execute(send);
        } catch (TelegramApiException ex) {
            log.warn("Не удалось отправить сообщение", ex);
        }
    }

    private void sendText(Long chatId, Integer threadId, String text, InlineKeyboardMarkup markup) {
        try {
            SendMessage send = new SendMessage();
            send.setChatId(chatId.toString());
            send.setText(text);
            send.setReplyMarkup(markup);
            if (threadId != null) {
                send.setMessageThreadId(threadId);
            }
            execute(send);
        } catch (TelegramApiException ex) {
            log.warn("Не удалось отправить сообщение", ex);
        }
    }

    private void sendText(Long chatId, Integer threadId, String text, ReplyKeyboard markup) {
        try {
            SendMessage send = new SendMessage();
            send.setChatId(chatId.toString());
            send.setText(text);
            send.setReplyMarkup(markup);
            if (threadId != null) {
                send.setMessageThreadId(threadId);
            }
            execute(send);
        } catch (TelegramApiException ex) {
            log.warn("Не удалось отправить сообщение", ex);
        }
    }

    private Integer sendTextAndGetMessageId(Long chatId, Integer threadId, String text) throws TelegramApiException {
        SendMessage send = new SendMessage();
        send.setChatId(chatId.toString());
        send.setText(text);
        if (threadId != null) {
            send.setMessageThreadId(threadId);
        }
        Message sent = execute(send);
        return sent != null ? sent.getMessageId() : null;
    }

    private void deleteMessageSafe(Long chatId, Integer messageId) {
        if (chatId == null || messageId == null) {
            return;
        }
        try {
            DeleteMessage delete = new DeleteMessage();
            delete.setChatId(chatId.toString());
            delete.setMessageId(messageId);
            execute(delete);
        } catch (TelegramApiException ex) {
            log.warn("Не удалось удалить сообщение {}", messageId, ex);
        }
    }

    private void editMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup markup) {
        try {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setReplyMarkup(markup);
            execute(edit);
        } catch (TelegramApiException ex) {
            log.warn("Не удалось обновить сообщение", ex);
        }
    }

    private void answerCallback(String callbackId) {
        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackId);
            execute(answer);
        } catch (TelegramApiException ex) {
            log.warn("Не удалось ответить на callback", ex);
        }
    }

    private void sendDatePicker(Long chatId, Integer threadId, String text, String target, YearMonth month) {
        String title = text + " (" + month.atDay(1).format(MONTH_FORMAT) + ")";
        sendText(chatId, threadId, title, buildCalendarKeyboard(month, target));
    }

    private void sendTimePicker(Long chatId, Integer threadId, String text, String target) {
        sendText(chatId, threadId, text, buildTimePeriodKeyboard(target));
    }

    private String normalizeCommand(String text) {
        String command = text.split("\\s+")[0];
        int atIndex = command.indexOf('@');
        if (atIndex > 0) {
            command = command.substring(0, atIndex);
        }
        return command.toLowerCase();
    }

    private String normalizeUserQuery(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("@")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed.trim();
    }

    private String resolveUserName(User user) {
        if (user.getUserName() != null && !user.getUserName().isBlank()) {
            return user.getUserName();
        }
        StringBuilder name = new StringBuilder();
        if (user.getFirstName() != null) {
            name.append(user.getFirstName());
        }
        if (user.getLastName() != null) {
            if (!name.isEmpty()) {
                name.append(" ");
            }
            name.append(user.getLastName());
        }
        return name.isEmpty() ? String.valueOf(user.getId()) : name.toString();
    }

    private LocalDate parseDate(String text, ZoneId zoneId) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        LocalDate today = LocalDate.now(zoneId);
        if (normalized.equals("today") || normalized.equals("сегодня")) {
            return today;
        }
        if (normalized.equals("tomorrow") || normalized.equals("завтра")) {
            return today.plusDays(1);
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(normalized);
        List<Integer> parts = new ArrayList<>();
        while (matcher.find()) {
            parts.add(Integer.parseInt(matcher.group()));
        }
        if (parts.size() < 2) {
            return null;
        }
        int day;
        int month;
        int year;
        if (parts.size() >= 3 && parts.get(0) > 31) {
            year = parts.get(0);
            month = parts.get(1);
            day = parts.get(2);
        } else {
            day = parts.get(0);
            month = parts.get(1);
            if (parts.size() >= 3) {
                year = parts.get(2);
            } else {
                year = today.getYear();
            }
        }
        if (year < 100) {
            year += 2000;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalTime parseTime(String text) {
        String normalized = text.trim();
        Matcher matcher = Pattern.compile("\\d+").matcher(normalized);
        List<String> parts = new ArrayList<>();
        while (matcher.find()) {
            parts.add(matcher.group());
        }
        if (parts.isEmpty()) {
            return null;
        }
        int hour;
        int minute;
        if (parts.size() >= 2) {
            hour = Integer.parseInt(parts.get(0));
            minute = Integer.parseInt(parts.get(1));
        } else {
            String value = parts.get(0);
            if (value.length() == 4) {
                hour = Integer.parseInt(value.substring(0, 2));
                minute = Integer.parseInt(value.substring(2));
            } else if (value.length() == 3) {
                hour = Integer.parseInt(value.substring(0, 1));
                minute = Integer.parseInt(value.substring(1));
            } else {
                hour = Integer.parseInt(value);
                minute = 0;
            }
        }
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }

    private Integer parseDuration(String text, Integer defaultValue) {
        if (isDefaultKeyword(text)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseTableUnits(String text, Integer defaultValue) {
        if (isDefaultKeyword(text)) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value == 1 || value == 2 || value == 3) {
                return value;
            }
        } catch (NumberFormatException ex) {
            return null;
        }
        return null;
    }

    private int parseIndex(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String buildGameList(List<GameDto> games) {
        StringBuilder sb = new StringBuilder("Выберите игру:\n");
        for (int i = 0; i < games.size(); i++) {
            GameDto game = games.get(i);
            sb.append(i + 1)
                    .append(") ")
                    .append(game.name())
                    .append(" - ")
                    .append(game.defaultDurationMinutes())
                    .append(" мин - столы ")
                    .append(formatTableUnits(game.tableUnits()))
                    .append("\n");
        }
        sb.append("0) Другая игра");
        return sb.toString();
    }

    private String buildArmyList(List<ArmyDto> armies) {
        StringBuilder sb = new StringBuilder("Выберите клубную армию:\n");
        for (int i = 0; i < armies.size(); i++) {
            ArmyDto army = armies.get(i);
            sb.append(i + 1)
                    .append(") ")
                    .append(army.game())
                    .append(" / ")
                    .append(army.faction())
                    .append(" (владелец: ")
                    .append(army.ownerName())
                    .append(")\n");
        }
        return sb.toString();
    }

    private String buildUserList(List<UserDto> users) {
        StringBuilder sb = new StringBuilder("Несколько совпадений, выберите номер:\n");
        for (int i = 0; i < users.size(); i++) {
            UserDto user = users.get(i);
            sb.append(i + 1)
                    .append(") ")
                    .append(user.name())
                    .append("\n");
        }
        return sb.toString();
    }

    private String buildBookingSummary(ConversationState state) {
        return "Подтвердите бронирование:\n"
                + "Дата: " + formatDate(state.getDate()) + "\n"
                + "Время: " + formatTime(state.getStartTime()) + " - " + formatTime(state.getEndAt().toLocalTime()) + "\n"
                + "Игра: " + state.getGame() + "\n"
                + "Столы: " + formatTableUnits(state.getTableUnits()) + "\n"
                + (state.getOpponentName() != null
                ? "Соперник: " + formatPlayerLabel(state.getOpponentName(), state.getOpponentFaction()) + "\n"
                : "")
                + (state.isClubArmy()
                ? "Армия: " + (state.getArmyLabel() != null ? state.getArmyLabel() : "клубная") + "\n"
                : "Армия: своя\n")
                + (!state.isClubArmy() && state.getFaction() != null ? "Фракция: " + state.getFaction() + "\n" : "")
                + "Отправьте 'да' или 'нет'.";
    }

    private String buildEventSummary(ConversationState state) {
        return "Подтвердите мероприятие:\n"
                + "Название: " + state.getEventTitle() + "\n"
                + "Тип: " + formatEventTypeLabel(state.getEventType()) + "\n"
                + "Дата: " + formatDate(state.getDate()) + "\n"
                + "Время: " + formatTime(state.getStartTime()) + " - " + formatTime(state.getEndTime()) + "\n"
                + (state.getEventDescription() != null ? "Описание: " + state.getEventDescription() + "\n" : "")
                + "Отправьте 'да' или 'нет'.";
    }

    private String buildNotes(ConversationState state) {
        StringBuilder notes = new StringBuilder();
        if (state.getOpponentName() != null && state.getOpponentUserId() == null) {
            notes.append("Соперник: ").append(state.getOpponentName()).append("; ");
        }
        if (state.getOpponentFaction() != null && !state.getOpponentFaction().isBlank()) {
            notes.append("Соперник фракция: ").append(state.getOpponentFaction()).append("; ");
        }
        if (state.isClubArmy() && state.getArmyLabel() != null) {
            notes.append("Армия: ").append(state.getArmyLabel());
        }
        return notes.toString().trim();
    }

    private String formatPlayerLabel(String name, String faction) {
        if (name == null || name.isBlank()) {
            return "-";
        }
        if (faction == null || faction.isBlank()) {
            return name;
        }
        return name + " (" + faction + ")";
    }

    private boolean ensureArmyForBooking(ConversationState state, Message message) {
        if (state.getArmyId() != null || state.getFaction() == null || state.getFaction().isBlank()) {
            return true;
        }
        try {
            ArmyDto created = apiClient.createArmy(new ArmyCreateRequest(
                    state.getUserId(),
                    state.getGame(),
                    state.getFaction(),
                    state.isClubArmy()
            ));
            if (created != null) {
                state.setArmyId(created.id());
                state.setArmyLabel(created.game() + " / " + created.faction());
            }
            return true;
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка сохранения армии: {} {}", ex.getRawStatusCode(), ex.getStatusText());
            sendText(message.getChatId(), null, "Ошибка сохранения армии: " + ex.getRawStatusCode());
            return false;
        } catch (RestClientException ex) {
            log.warn("Ошибка сохранения армии: {}", ex.getMessage());
            sendText(message.getChatId(), null, "Ошибка сохранения армии: не удалось связаться с API.");
            return false;
        }
    }

    private boolean ensureOpponentArmyForBooking(ConversationState state, Message message) {
        if (state.getOpponentUserId() == null) {
            return true;
        }
        String opponentFaction = state.getOpponentFaction();
        if (opponentFaction == null || opponentFaction.isBlank()) {
            return true;
        }
        List<ArmyDto> armies = null;
        if (state.getGame() != null && !state.getGame().isBlank()) {
            armies = apiClient.getArmies(state.getGame(), null, true);
        }
        if (armies != null) {
            for (ArmyDto army : armies) {
                if (army == null || army.isClubShared()) {
                    continue;
                }
                if (army.ownerUserId() == null || !army.ownerUserId().equals(state.getOpponentUserId())) {
                    continue;
                }
                if (army.faction() != null && army.faction().equalsIgnoreCase(opponentFaction)) {
                    return true;
                }
            }
        }
        try {
            apiClient.createArmy(new ArmyCreateRequest(
                    state.getOpponentUserId(),
                    state.getGame(),
                    opponentFaction,
                    false
            ));
            return true;
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка сохранения армии соперника: {} {}", ex.getRawStatusCode(), ex.getStatusText());
            sendText(message.getChatId(), null, "Ошибка сохранения армии соперника: " + ex.getRawStatusCode());
            return false;
        } catch (RestClientException ex) {
            log.warn("Ошибка сохранения армии соперника: {}", ex.getMessage());
            sendText(message.getChatId(), null, "Ошибка сохранения армии соперника: не удалось связаться с API.");
            return false;
        }
    }

    private String parseEventType(String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "1", "paint_day", "paint day", "день покраски", "покраска", "день-покраски" -> "PAINT_DAY";
            case "2", "work_day", "work day", "рабочий день", "хоздень", "хозяйственный день" -> "WORK_DAY";
            case "3", "tournament", "турнир" -> "TOURNAMENT";
            case "4", "other", "другое", "прочее" -> "OTHER";
            default -> null;
        };
    }

    private String formatTableUnits(int units) {
        return switch (units) {
            case 1 -> "0.5";
            case 2 -> "1";
            case 3 -> "1.5";
            case 4 -> "2";
            case 6 -> "3";
            default -> String.valueOf(units);
        };
    }

    private String formatEventTypeLabel(String type) {
        if (type == null) {
            return "-";
        }
        return switch (type) {
            case "PAINT_DAY" -> "День покраски";
            case "WORK_DAY" -> "Рабочий день";
            case "TOURNAMENT" -> "Турнир";
            case "OTHER" -> "Другое";
            default -> type;
        };
    }

    private boolean isDefaultKeyword(String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("default")
                || normalized.equals("d")
                || normalized.equals("по умолчанию")
                || normalized.equals("по-умолчанию")
                || normalized.equals("умолч");
    }

    private boolean isYes(String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("да") || normalized.equals("yes") || normalized.equals("y");
    }

    private boolean isNo(String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("нет") || normalized.equals("no") || normalized.equals("n");
    }

    private boolean isCustomEventTitle(ConversationState state, String title) {
        List<String> titles = state.getAvailableEventTitles();
        if (titles == null || titles.isEmpty() || title == null) {
            return true;
        }
        String normalized = title.trim().toLowerCase(Locale.ROOT);
        for (String existing : titles) {
            if (existing != null && existing.trim().toLowerCase(Locale.ROOT).equals(normalized)) {
                return false;
            }
        }
        return true;
    }

    private TelegramSettingsDto sameChatSettings(TelegramSettingsDto settings, Long chatId) {
        if (settings == null || chatId == null) {
            return null;
        }
        return chatId.equals(settings.chatId()) ? settings : null;
    }

    private boolean isThreadNotFound(TelegramApiRequestException ex) {
        String response = ex.getApiResponse();
        if (response == null) {
            return false;
        }
        return response.toLowerCase(Locale.ROOT).contains("message thread not found");
    }

    private void clearScheduleThreadIfMatches(Long chatId, Integer threadId) {
        TelegramSettingsDto settings = apiClient.getTelegramSettings();
        if (settings == null || settings.chatId() == null || threadId == null) {
            return;
        }
        if (!settings.chatId().equals(chatId)) {
            return;
        }
        if (settings.scheduleThreadId() == null || !settings.scheduleThreadId().equals(threadId)) {
            return;
        }
        TelegramSettingsUpdateRequest request = new TelegramSettingsUpdateRequest(
                chatId,
                0,
                null,
                null,
                null,
                null,
                null
        );
        apiClient.updateTelegramSettings(request);
    }

    private ZoneId resolveTimezone() {
        return resolveTimezone(apiClient.getTelegramSettings());
    }

    private ZoneId resolveTimezone(TelegramSettingsDto settings) {
        if (settings != null && settings.timezone() != null && !settings.timezone().isBlank()) {
            try {
                return ZoneId.of(settings.timezone());
            } catch (Exception ex) {
                return ZoneId.of("Europe/Moscow");
            }
        }
        return ZoneId.of("Europe/Moscow");
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return date.format(DATE_FORMAT);
    }

    private String formatTime(LocalTime time) {
        if (time == null) {
            return "-";
        }
        return time.format(TIME_FORMAT);
    }

    private void handleBookingTimeSelected(ConversationState state, Message message, LocalTime time) {
        if (state.getDate() == null) {
            sendText(message.getChatId(), null, "Сначала выберите дату.");
            YearMonth month = YearMonth.now(resolveTimezone());
            sendDatePicker(message.getChatId(), null, "Выберите дату игры", DATE_TARGET_BOOKING, month);
            return;
        }
        state.setStartTime(time);
        ZoneId zoneId = resolveTimezone();
        state.setStartAt(ZonedDateTime.of(state.getDate(), time, zoneId).toOffsetDateTime());
        List<GameDto> games = apiClient.getGames();
        state.setAvailableGames(games);
        if (games == null || games.isEmpty()) {
            state.setStep(ConversationState.Step.PICK_CUSTOM_GAME);
            sendText(message.getChatId(), null, "Введите название игры.");
            return;
        }
        state.setStep(ConversationState.Step.PICK_GAME);
        sendText(message.getChatId(), null, "Выберите игру:", buildGameKeyboard(games));
    }

    private void handleEventStartTimeSelected(ConversationState state, Message message, LocalTime time) {
        if (state.getDate() == null) {
            sendText(message.getChatId(), null, "Сначала выберите дату мероприятия.");
            YearMonth month = YearMonth.now(resolveTimezone());
            sendDatePicker(message.getChatId(), null, "Выберите дату мероприятия", DATE_TARGET_EVENT, month);
            return;
        }
        state.setStartTime(time);
        state.setStep(ConversationState.Step.EVENT_END_TIME);
        sendTimePicker(message.getChatId(), null, "Выберите время окончания", TIME_TARGET_EVENT_END);
    }

    private void handleEventEndTimeSelected(ConversationState state, Message message, LocalTime time) {
        if (state.getStartTime() == null) {
            sendText(message.getChatId(), null, "Сначала выберите время начала.");
            sendTimePicker(message.getChatId(), null, "Выберите время начала", TIME_TARGET_EVENT_START);
            return;
        }
        if (!time.isAfter(state.getStartTime())) {
            sendText(message.getChatId(), null, "Окончание должно быть позже начала.");
            sendTimePicker(message.getChatId(), null, "Выберите время окончания", TIME_TARGET_EVENT_END);
            return;
        }
        state.setEndTime(time);
        ZoneId zoneId = resolveTimezone();
        state.setStartAt(ZonedDateTime.of(state.getDate(), state.getStartTime(), zoneId).toOffsetDateTime());
        state.setEndAt(ZonedDateTime.of(state.getDate(), state.getEndTime(), zoneId).toOffsetDateTime());
        state.setStep(ConversationState.Step.EVENT_DESCRIPTION);
        sendText(message.getChatId(), null, "Описание (или '-' чтобы пропустить).");
    }

    private void handleCallbackQuery(CallbackQuery query) {
        if (query == null || query.getData() == null) {
            return;
        }
        String data = query.getData();
        if (CALLBACK_NOOP.equals(data)) {
            answerCallback(query.getId());
            return;
        }
        User from = query.getFrom();
        if (from == null) {
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("res:")) {
            handleResultCallback(query, data);
            answerCallback(query.getId());
            return;
        }
        ConversationState state = conversations.get(from.getId());
        if (state == null) {
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("d:")) {
            handleDateCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("t:")) {
            handleTimeCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("g:")) {
            handleGameCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("u:")) {
            handleTableUnitsCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("at:")) {
            handleArmyTypeCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("a:")) {
            handleArmyCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("f:")) {
            handleFactionCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("et:")) {
            handleEventTypeCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith(EVENT_TITLE_PREFIX + ":")) {
            handleEventTitleCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("c:")) {
            handleConfirmCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        if (data.startsWith("op:")) {
            handleOpponentCallback(state, query, data);
            answerCallback(query.getId());
            return;
        }
        answerCallback(query.getId());
    }

    private void handleDateCallback(ConversationState state, CallbackQuery query, String data) {
        String[] parts = data.split(":", 3);
        if (parts.length < 3) {
            return;
        }
        String target = parts[1];
        String payload = parts[2];
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        if (payload.matches("\\d{4}-\\d{2}")) {
            YearMonth month = YearMonth.parse(payload);
            String titlePrefix = DATE_TARGET_EVENT.equals(target) ? "Выберите дату мероприятия" : "Выберите дату игры";
            editMessage(message.getChatId(), message.getMessageId(),
                    titlePrefix + " (" + month.atDay(1).format(MONTH_FORMAT) + ")",
                    buildCalendarKeyboard(month, target));
            return;
        }
        if (!payload.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return;
        }
        LocalDate date = LocalDate.parse(payload);
        if (DATE_TARGET_BOOKING.equals(target) && state.getFlow() == ConversationState.Flow.BOOKING) {
            if (state.getStep() != ConversationState.Step.PICK_DATE) {
                return;
            }
            state.setDate(date);
            state.setStep(ConversationState.Step.PICK_TIME);
            editMessage(message.getChatId(), message.getMessageId(), "Дата выбрана: " + formatDate(date), null);
            sendTimePicker(message.getChatId(), null, "Выберите время начала", TIME_TARGET_BOOKING_START);
            return;
        }
        if (DATE_TARGET_EVENT.equals(target) && state.getFlow() == ConversationState.Flow.EVENT) {
            if (state.getStep() != ConversationState.Step.EVENT_DATE) {
                return;
            }
            state.setDate(date);
            state.setStep(ConversationState.Step.EVENT_TIME);
            editMessage(message.getChatId(), message.getMessageId(), "Дата выбрана: " + formatDate(date), null);
            sendTimePicker(message.getChatId(), null, "Выберите время начала", TIME_TARGET_EVENT_START);
        }
    }

    private void handleTimeCallback(ConversationState state, CallbackQuery query, String data) {
        String[] parts = data.split(":");
        if (parts.length < 4) {
            return;
        }
        String target = parts[1];
        String stage = parts[2];
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        if ("p".equals(stage)) {
            String period = parts[3];
            editMessage(message.getChatId(), message.getMessageId(),
                    "Выберите интервал", buildTimeBlockKeyboard(target, period));
            return;
        }
        if ("b".equals(stage) && parts.length >= 5) {
            String period = parts[3];
            int block = Integer.parseInt(parts[4]);
            int startHour = ("pm".equals(period) ? 12 : 0) + block * 3;
            editMessage(message.getChatId(), message.getMessageId(),
                    "Выберите время", buildTimeSlotKeyboard(target, startHour));
            return;
        }
        if ("s".equals(stage)) {
            String value = parts[3];
            if (!value.matches("\\d{4}")) {
                return;
            }
            int hour = Integer.parseInt(value.substring(0, 2));
            int minute = Integer.parseInt(value.substring(2));
            LocalTime time = LocalTime.of(hour, minute);
            editMessage(message.getChatId(), message.getMessageId(), "Время выбрано: " + formatTime(time), null);
            applyTimeSelection(state, message, target, time);
        }
    }

    private void applyTimeSelection(ConversationState state, Message message, String target, LocalTime time) {
        if (TIME_TARGET_BOOKING_START.equals(target) && state.getFlow() == ConversationState.Flow.BOOKING) {
            if (state.getStep() != ConversationState.Step.PICK_TIME) {
                return;
            }
            handleBookingTimeSelected(state, message, time);
            return;
        }
        if (TIME_TARGET_EVENT_START.equals(target) && state.getFlow() == ConversationState.Flow.EVENT) {
            if (state.getStep() != ConversationState.Step.EVENT_TIME) {
                return;
            }
            handleEventStartTimeSelected(state, message, time);
            return;
        }
        if (TIME_TARGET_EVENT_END.equals(target) && state.getFlow() == ConversationState.Flow.EVENT) {
            if (state.getStep() != ConversationState.Step.EVENT_END_TIME) {
                return;
            }
            handleEventEndTimeSelected(state, message, time);
        }
    }

    private void handleGameCallback(ConversationState state, CallbackQuery query, String data) {
        if (state.getStep() != ConversationState.Step.PICK_GAME) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        if (data.equals("g:other")) {
            state.setStep(ConversationState.Step.PICK_CUSTOM_GAME);
            state.setCustomGame(true);
            editMessage(message.getChatId(), message.getMessageId(), "Выбрана другая игра.", null);
            sendText(message.getChatId(), null, "Введите название игры.");
            return;
        }
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        int index = parseIndex(parts[1]);
        List<GameDto> games = state.getAvailableGames();
        if (games == null || index < 0 || index >= games.size()) {
            return;
        }
        GameDto game = games.get(index);
        state.setGame(game.name());
        state.setDurationMinutes(game.defaultDurationMinutes());
        state.setTableUnits(game.tableUnits());
        state.setCustomGame(false);
        state.setStep(ConversationState.Step.PICK_DURATION);
        editMessage(message.getChatId(), message.getMessageId(), "Вы выбрали игру: " + game.name(), null);
        sendText(message.getChatId(), null,
                "Обычно " + game.defaultDurationMinutes() + " минут. Отправьте число минут или 'по умолчанию'.");
    }

    private void handleTableUnitsCallback(ConversationState state, CallbackQuery query, String data) {
        if (state.getStep() != ConversationState.Step.PICK_TABLE_UNITS) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        Integer units = null;
        if ("default".equals(parts[1])) {
            units = state.getTableUnits();
        } else {
            try {
                units = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                return;
            }
        }
        if (units == null) {
            return;
        }
        state.setTableUnits(units);
        state.setEndAt(state.getStartAt().plusMinutes(state.getDurationMinutes()));
        state.setStep(ConversationState.Step.PICK_OPPONENT);
        editMessage(message.getChatId(), message.getMessageId(), "Столы выбраны: " + formatTableUnits(units), null);
        sendText(message.getChatId(), null, "С кем будете играть? @username или имя, '-' если один.");
    }

    private void handleArmyTypeCallback(ConversationState state, CallbackQuery query, String data) {
        if (state.getStep() != ConversationState.Step.PICK_ARMY_TYPE) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        if ("at:own".equals(data)) {
            editMessage(message.getChatId(), message.getMessageId(), "Армия: своя", null);
            startOwnFactionSelection(state, message);
            return;
        }
        if ("at:club".equals(data)) {
            editMessage(message.getChatId(), message.getMessageId(), "Армия: клубная", null);
            startClubArmySelection(state, message);
        }
    }

    private void handleArmyCallback(ConversationState state, CallbackQuery query, String data) {
        if (state.getStep() != ConversationState.Step.PICK_ARMY) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        if ("a:add".equals(data)) {
            editMessage(message.getChatId(), message.getMessageId(), "Добавление клубной армии", null);
            startClubFactionInput(state, message);
            return;
        }
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        int index = parseIndex(parts[1]);
        List<ArmyDto> armies = state.getAvailableArmies();
        if (armies == null || index < 0 || index >= armies.size()) {
            return;
        }
        ArmyDto army = armies.get(index);
        state.setArmyId(army.id());
        state.setArmyLabel(army.game() + " / " + army.faction());
        state.setStep(ConversationState.Step.CONFIRM);
        editMessage(message.getChatId(), message.getMessageId(), "Армия выбрана: " + state.getArmyLabel(), null);
        sendText(message.getChatId(), null, buildBookingSummary(state), buildConfirmKeyboard());
    }

    private void handleFactionCallback(ConversationState state, CallbackQuery query, String data) {
        boolean opponentStep = state.getStep() == ConversationState.Step.PICK_OPPONENT_FACTION;
        if (state.getStep() != ConversationState.Step.PICK_OWN_FACTION && !opponentStep) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        if ("f:other".equals(data)) {
            editMessage(message.getChatId(), message.getMessageId(), "Фракция: другая", null);
            if (opponentStep) {
                state.setAvailableFactions(null);
                sendText(message.getChatId(), null, "Введите фракцию соперника (или '-' чтобы пропустить).");
            } else {
                state.setAvailableFactions(null);
                sendText(message.getChatId(), null, "Введите название фракции.");
            }
            return;
        }
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        int index = parseIndex(parts[1]);
        List<String> factions = state.getAvailableFactions();
        if (factions == null || index < 0 || index >= factions.size()) {
            return;
        }
        String faction = factions.get(index);
        if (opponentStep) {
            state.setOpponentFaction(faction);
            state.setStep(ConversationState.Step.PICK_ARMY_TYPE);
            editMessage(message.getChatId(), message.getMessageId(), "Фракция соперника: " + faction, null);
            sendText(message.getChatId(), null, "Выберите тип армии:", buildArmyTypeKeyboard());
            return;
        }
        applyOwnFactionSelection(state, faction);
        editMessage(message.getChatId(), message.getMessageId(), "Фракция: " + faction, null);
        sendText(message.getChatId(), null, buildBookingSummary(state), buildConfirmKeyboard());
    }

    private void handleResultCallback(CallbackQuery query, String data) {
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        String[] parts = data.split(":");
        if (parts.length < 3) {
            return;
        }
        Long bookingId;
        try {
            bookingId = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            return;
        }
        String outcome = switch (parts[2]) {
            case "win" -> "WIN";
            case "loss" -> "LOSS";
            case "draw" -> "DRAW";
            default -> null;
        };
        if (outcome == null) {
            return;
        }
        User from = query.getFrom();
        if (from == null) {
            return;
        }
        UserDto user = apiClient.upsertTelegramUser(from.getId(), resolveUserName(from));
        BookingResultRequest request = new BookingResultRequest(user.id(), outcome);
        try {
            apiClient.submitBookingResult(bookingId, request);
            String label = switch (outcome) {
                case "WIN" -> "победа";
                case "LOSS" -> "поражение";
                case "DRAW" -> "ничья";
                default -> "записано";
            };
            editMessage(message.getChatId(), message.getMessageId(), "Результат записан: " + label, null);
        } catch (HttpClientErrorException.Conflict ex) {
            editMessage(message.getChatId(), message.getMessageId(), "Результат уже записан.", null);
        } catch (RestClientResponseException ex) {
            editMessage(message.getChatId(), message.getMessageId(),
                    "Ошибка записи результата: " + ex.getRawStatusCode(), null);
        } catch (RestClientException ex) {
            editMessage(message.getChatId(), message.getMessageId(),
                    "Ошибка записи результата: не удалось связаться с API.", null);
        }
    }

    private void handleEventTypeCallback(ConversationState state, CallbackQuery query, String data) {
        if (state.getStep() != ConversationState.Step.EVENT_TYPE) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        state.setEventType(parts[1]);
        state.setStep(ConversationState.Step.EVENT_DATE);
        editMessage(message.getChatId(), message.getMessageId(), "Тип: " + formatEventTypeLabel(parts[1]), null);
        YearMonth month = YearMonth.now(resolveTimezone());
        sendDatePicker(message.getChatId(), null, "Выберите дату мероприятия", DATE_TARGET_EVENT, month);
    }

    private void handleEventTitleCallback(ConversationState state, CallbackQuery query, String data) {
        if (state.getStep() != ConversationState.Step.EVENT_TITLE) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        if ("other".equals(parts[1])) {
            state.setStep(ConversationState.Step.EVENT_TITLE_CUSTOM);
            editMessage(message.getChatId(), message.getMessageId(), "Выбрано: другое", null);
            sendText(message.getChatId(), null, "Введите название мероприятия.");
            return;
        }
        int index = parseIndex(parts[1]);
        List<String> titles = state.getAvailableEventTitles();
        if (titles == null || index < 0 || index >= titles.size()) {
            return;
        }
        String title = titles.get(index);
        state.setEventTitle(title);
        state.setCustomEventTitle(false);
        state.setStep(ConversationState.Step.EVENT_TYPE);
        editMessage(message.getChatId(), message.getMessageId(), "Название: " + title, null);
        sendText(message.getChatId(), null, "Выберите тип мероприятия:", buildEventTypeKeyboard());
    }

    private void handleConfirmCallback(ConversationState state, CallbackQuery query, String data) {
        if (state.getStep() != ConversationState.Step.CONFIRM
                && state.getStep() != ConversationState.Step.EVENT_CONFIRM) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        boolean yes = "yes".equals(parts[1]);
        String answer = yes ? "да" : "нет";
        editMessage(message.getChatId(), message.getMessageId(), "Ответ: " + (yes ? "Да" : "Нет"), null);
        if (state.getStep() == ConversationState.Step.CONFIRM) {
            handleBookingConfirm(state, message, answer);
            conversations.remove(query.getFrom().getId());
            return;
        }
        if (state.getStep() == ConversationState.Step.EVENT_CONFIRM) {
            handleEventConfirm(state, message, answer);
            conversations.remove(query.getFrom().getId());
        }
    }

    private void handleOpponentCallback(ConversationState state, CallbackQuery query, String data) {
        if (state.getStep() != ConversationState.Step.PICK_OPPONENT) {
            return;
        }
        var maybeMessage = query.getMessage();
        if (!(maybeMessage instanceof Message message)) {
            return;
        }
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        int index = parseIndex(parts[1]);
        List<UserDto> users = state.getFoundUsers();
        if (users == null || index < 0 || index >= users.size()) {
            return;
        }
        UserDto user = users.get(index);
        state.setOpponentUserId(user.id());
        state.setOpponentName(user.name());
        state.setFoundUsers(null);
        editMessage(message.getChatId(), message.getMessageId(), "Соперник: " + user.name(), null);
        startOpponentFactionSelection(state, message);
    }

    private InlineKeyboardMarkup buildCalendarKeyboard(YearMonth month, String target) {
        LocalDate today = LocalDate.now(resolveTimezone());
        YearMonth currentMonth = YearMonth.from(today);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> header = new ArrayList<>();
        for (String label : new String[]{"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"}) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(label);
            button.setCallbackData(CALLBACK_NOOP);
            header.add(button);
        }
        rows.add(header);
        int firstDay = month.atDay(1).getDayOfWeek().getValue();
        int daysInMonth = month.lengthOfMonth();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 1; i < firstDay; i++) {
            InlineKeyboardButton blank = new InlineKeyboardButton();
            blank.setText(" ");
            blank.setCallbackData(CALLBACK_NOOP);
            row.add(blank);
        }
        for (int day = 1; day <= daysInMonth; day++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String date = String.format("%04d-%02d-%02d", month.getYear(), month.getMonthValue(), day);
            LocalDate current = LocalDate.parse(date);
            if (current.isBefore(today)) {
                button.setText(" ");
                button.setCallbackData(CALLBACK_NOOP);
            } else {
                button.setText(String.valueOf(day));
                button.setCallbackData("d:" + target + ":" + date);
            }
            row.add(button);
            if (row.size() == 7) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            while (row.size() < 7) {
                InlineKeyboardButton blank = new InlineKeyboardButton();
                blank.setText(" ");
                blank.setCallbackData(CALLBACK_NOOP);
                row.add(blank);
            }
            rows.add(row);
        }
        List<InlineKeyboardButton> nav = new ArrayList<>();
        InlineKeyboardButton prev = new InlineKeyboardButton();
        prev.setText("«");
        if (month.isAfter(currentMonth)) {
            YearMonth prevMonth = month.minusMonths(1);
            prev.setCallbackData("d:" + target + ":" + String.format("%04d-%02d", prevMonth.getYear(), prevMonth.getMonthValue()));
        } else {
            prev.setCallbackData(CALLBACK_NOOP);
        }
        InlineKeyboardButton label = new InlineKeyboardButton();
        label.setText(month.atDay(1).format(MONTH_FORMAT));
        label.setCallbackData(CALLBACK_NOOP);
        InlineKeyboardButton next = new InlineKeyboardButton();
        next.setText("»");
        YearMonth nextMonth = month.plusMonths(1);
        next.setCallbackData("d:" + target + ":" + String.format("%04d-%02d", nextMonth.getYear(), nextMonth.getMonthValue()));
        nav.add(prev);
        nav.add(label);
        nav.add(next);
        rows.add(nav);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildTimePeriodKeyboard(String target) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton am = new InlineKeyboardButton();
        am.setText("до 12");
        am.setCallbackData("t:" + target + ":p:am");
        InlineKeyboardButton pm = new InlineKeyboardButton();
        pm.setText("после 12");
        pm.setCallbackData("t:" + target + ":p:pm");
        row.add(am);
        row.add(pm);
        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildTimeBlockKeyboard(String target, String period) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        int base = "pm".equals(period) ? 12 : 0;
        for (int i = 0; i < 4; i++) {
            int start = base + i * 3;
            int end = start + 3;
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%02d-%02d", start, end));
            button.setCallbackData("t:" + target + ":b:" + period + ":" + i);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildTimeSlotKeyboard(String target, int startHour) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int hour = startHour + (i / 2);
            int minute = (i % 2) * 30;
            String label = String.format("%02d:%02d", hour, minute);
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(label);
            button.setCallbackData("t:" + target + ":s:" + String.format("%02d%02d", hour, minute));
            row.add(button);
            if (row.size() == 3) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildEventTypeKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton paint = new InlineKeyboardButton();
        paint.setText("День покраски");
        paint.setCallbackData("et:PAINT_DAY");
        InlineKeyboardButton work = new InlineKeyboardButton();
        work.setText("Рабочий день");
        work.setCallbackData("et:WORK_DAY");
        InlineKeyboardButton tournament = new InlineKeyboardButton();
        tournament.setText("Турнир");
        tournament.setCallbackData("et:TOURNAMENT");
        InlineKeyboardButton other = new InlineKeyboardButton();
        other.setText("Другое");
        other.setCallbackData("et:OTHER");
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(paint);
        row1.add(work);
        rows.add(row1);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(tournament);
        row2.add(other);
        rows.add(row2);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildEventTitleKeyboard(List<String> titles) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            InlineKeyboardButton button = new InlineKeyboardButton();
            String label = title;
            if (label != null && label.length() > 40) {
                label = label.substring(0, 37) + "...";
            }
            button.setText(label);
            button.setCallbackData(EVENT_TITLE_PREFIX + ":" + i);
            row.add(button);
            if (row.size() == 2) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        InlineKeyboardButton other = new InlineKeyboardButton();
        other.setText("Другое");
        other.setCallbackData(EVENT_TITLE_PREFIX + ":other");
        List<InlineKeyboardButton> otherRow = new ArrayList<>();
        otherRow.add(other);
        rows.add(otherRow);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildGameKeyboard(List<GameDto> games) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < games.size(); i++) {
            GameDto game = games.get(i);
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(game.name());
            button.setCallbackData("g:" + i);
            row.add(button);
            if (row.size() == 2) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        InlineKeyboardButton other = new InlineKeyboardButton();
        other.setText("Другая игра");
        other.setCallbackData("g:other");
        List<InlineKeyboardButton> otherRow = new ArrayList<>();
        otherRow.add(other);
        rows.add(otherRow);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildTableUnitsKeyboard(Integer defaultUnits) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton half = new InlineKeyboardButton();
        half.setText("0.5 стола");
        half.setCallbackData("u:1");
        InlineKeyboardButton one = new InlineKeyboardButton();
        one.setText("1 стол");
        one.setCallbackData("u:2");
        InlineKeyboardButton oneHalf = new InlineKeyboardButton();
        oneHalf.setText("1.5 стола");
        oneHalf.setCallbackData("u:3");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(half);
        row.add(one);
        row.add(oneHalf);
        rows.add(row);
        if (defaultUnits != null) {
            InlineKeyboardButton def = new InlineKeyboardButton();
            def.setText("По умолчанию");
            def.setCallbackData("u:default");
            List<InlineKeyboardButton> defaultRow = new ArrayList<>();
            defaultRow.add(def);
            rows.add(defaultRow);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildArmyTypeKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton own = new InlineKeyboardButton();
        own.setText("Своя");
        own.setCallbackData("at:own");
        InlineKeyboardButton club = new InlineKeyboardButton();
        club.setText("Клубная");
        club.setCallbackData("at:club");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(own);
        row.add(club);
        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildArmyKeyboard(List<ArmyDto> armies) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < armies.size(); i++) {
            ArmyDto army = armies.get(i);
            InlineKeyboardButton button = new InlineKeyboardButton();
            String label = army.game() + " / " + army.faction();
            button.setText(label);
            button.setCallbackData("a:" + i);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }
        InlineKeyboardButton add = new InlineKeyboardButton();
        add.setText("Добавить клубную");
        add.setCallbackData("a:add");
        List<InlineKeyboardButton> addRow = new ArrayList<>();
        addRow.add(add);
        rows.add(addRow);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildFactionKeyboard(List<String> factions) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < factions.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(factions.get(i));
            button.setCallbackData("f:" + i);
            row.add(button);
            if (row.size() == 2) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        InlineKeyboardButton other = new InlineKeyboardButton();
        other.setText("Другая");
        other.setCallbackData("f:other");
        List<InlineKeyboardButton> otherRow = new ArrayList<>();
        otherRow.add(other);
        rows.add(otherRow);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildConfirmKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton yes = new InlineKeyboardButton();
        yes.setText("Да");
        yes.setCallbackData("c:yes");
        InlineKeyboardButton no = new InlineKeyboardButton();
        no.setText("Нет");
        no.setCallbackData("c:no");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(yes);
        row.add(no);
        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildResultKeyboard(Long bookingId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton win = new InlineKeyboardButton();
        win.setText("Я победил");
        win.setCallbackData("res:" + bookingId + ":win");
        InlineKeyboardButton loss = new InlineKeyboardButton();
        loss.setText("Поражение");
        loss.setCallbackData("res:" + bookingId + ":loss");
        InlineKeyboardButton draw = new InlineKeyboardButton();
        draw.setText("Ничья");
        draw.setCallbackData("res:" + bookingId + ":draw");
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(win);
        row1.add(loss);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(draw);
        rows.add(row1);
        rows.add(row2);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup buildOpponentKeyboard(List<UserDto> users) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            UserDto user = users.get(i);
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(user.name());
            button.setCallbackData("op:" + i);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
