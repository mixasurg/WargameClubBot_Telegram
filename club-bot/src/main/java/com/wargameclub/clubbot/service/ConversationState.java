package com.wargameclub.clubbot.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubbot.dto.ArmyDto;
import com.wargameclub.clubbot.dto.GameDto;
import com.wargameclub.clubbot.dto.UserDto;

/**
 * Состояние диалога пользователя при создании бронирования или мероприятия.
 */
public class ConversationState {

    /**
     * Тип сценария диалога.
     */
    public enum Flow {
        /**
         * Сценарий создания бронирования.
         */
        BOOKING,
        /**
         * Сценарий создания мероприятия.
         */
        EVENT
    }

    /**
     * Шаг сценария диалога.
     */
    public enum Step {
        /** Выбор даты. */
        PICK_DATE,
        /** Выбор времени начала. */
        PICK_TIME,
        /** Выбор игры из списка. */
        PICK_GAME,
        /** Ввод пользовательского названия игры. */
        PICK_CUSTOM_GAME,
        /** Выбор длительности. */
        PICK_DURATION,
        /** Выбор количества единиц стола. */
        PICK_TABLE_UNITS,
        /** Выбор соперника. */
        PICK_OPPONENT,
        /** Ввод фракции соперника. */
        PICK_OPPONENT_FACTION,
        /** Выбор типа армии (личная/клубная). */
        PICK_ARMY_TYPE,
        /** Выбор конкретной армии. */
        PICK_ARMY,
        /** Ввод собственной фракции. */
        PICK_OWN_FACTION,
        /** Ввод фракции для клубной армии. */
        PICK_CLUB_FACTION,
        /** Подтверждение бронирования. */
        CONFIRM,
        /** Выбор/ввод названия мероприятия. */
        EVENT_TITLE,
        /** Ввод пользовательского названия мероприятия. */
        EVENT_TITLE_CUSTOM,
        /** Выбор типа мероприятия. */
        EVENT_TYPE,
        /** Выбор даты мероприятия. */
        EVENT_DATE,
        /** Выбор времени начала мероприятия. */
        EVENT_TIME,
        /** Выбор времени окончания мероприятия. */
        EVENT_END_TIME,
        /** Ввод описания мероприятия. */
        EVENT_DESCRIPTION,
        /** Подтверждение создания мероприятия. */
        EVENT_CONFIRM
    }

    /**
     * Текущий сценарий диалога.
     */
    private Flow flow;

    /**
     * Текущий шаг диалога.
     */
    private Step step;

    /**
     * Идентификатор пользователя.
     */
    private Long userId;

    /**
     * Имя пользователя.
     */
    private String userName;

    /**
     * Выбранная дата.
     */
    private LocalDate date;

    /**
     * Выбранное время начала.
     */
    private LocalTime startTime;

    /**
     * Выбранное время окончания.
     */
    private LocalTime endTime;

    /**
     * Рассчитанная дата и время начала.
     */
    private OffsetDateTime startAt;

    /**
     * Рассчитанная дата и время окончания.
     */
    private OffsetDateTime endAt;

    /**
     * Название игры/системы.
     */
    private String game;

    /**
     * Длительность игры в минутах.
     */
    private Integer durationMinutes;

    /**
     * Количество единиц стола.
     */
    private Integer tableUnits;

    /**
     * Признак пользовательского названия игры.
     */
    private boolean customGame;

    /**
     * Идентификатор соперника.
     */
    private Long opponentUserId;

    /**
     * Имя соперника.
     */
    private String opponentName;

    /**
     * Фракция соперника.
     */
    private String opponentFaction;

    /**
     * Признак выбора клубной армии.
     */
    private boolean clubArmy;

    /**
     * Идентификатор выбранной армии.
     */
    private Long armyId;

    /**
     * Текстовая метка выбранной армии.
     */
    private String armyLabel;

    /**
     * Фракция пользователя.
     */
    private String faction;

    /**
     * Название мероприятия.
     */
    private String eventTitle;

    /**
     * Тип мероприятия.
     */
    private String eventType;

    /**
     * Описание мероприятия.
     */
    private String eventDescription;

    /**
     * Признак пользовательского названия мероприятия.
     */
    private boolean customEventTitle;

    /**
     * Доступные игры для выбора.
     */
    private List<GameDto> availableGames;

    /**
     * Доступные армии для выбора.
     */
    private List<ArmyDto> availableArmies;

    /**
     * Доступные армии для выбранной игры.
     */
    private List<ArmyDto> availableGameArmies;

    /**
     * Доступные фракции для выбора.
     */
    private List<String> availableFactions;

    /**
     * Найденные пользователи (соперники).
     */
    private List<UserDto> foundUsers;

    /**
     * Доступные названия мероприятий.
     */
    private List<String> availableEventTitles;

    /**
     * Возвращает текущий сценарий диалога.
     *
     * @return сценарий диалога
     */
    public Flow getFlow() {
        return flow;
    }

    /**
     * Устанавливает текущий сценарий диалога.
     *
     * @param flow сценарий диалога
     */
    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    /**
     * Возвращает текущий шаг диалога.
     *
     * @return шаг диалога
     */
    public Step getStep() {
        return step;
    }

    /**
     * Устанавливает текущий шаг диалога.
     *
     * @param step шаг диалога
     */
    public void setStep(Step step) {
        this.step = step;
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Устанавливает идентификатор пользователя.
     *
     * @param userId идентификатор пользователя
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Возвращает имя пользователя.
     *
     * @return имя пользователя
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Устанавливает имя пользователя.
     *
     * @param userName имя пользователя
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Возвращает выбранную дату.
     *
     * @return выбранная дата
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Устанавливает выбранную дату.
     *
     * @param date выбранная дата
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Возвращает время начала.
     *
     * @return время начала
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Устанавливает время начала.
     *
     * @param startTime время начала
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Возвращает время окончания.
     *
     * @return время окончания
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Устанавливает время окончания.
     *
     * @param endTime время окончания
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Возвращает дату и время начала.
     *
     * @return дата и время начала
     */
    public OffsetDateTime getStartAt() {
        return startAt;
    }

    /**
     * Устанавливает дату и время начала.
     *
     * @param startAt дата и время начала
     */
    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    /**
     * Возвращает дату и время окончания.
     *
     * @return дата и время окончания
     */
    public OffsetDateTime getEndAt() {
        return endAt;
    }

    /**
     * Устанавливает дату и время окончания.
     *
     * @param endAt дата и время окончания
     */
    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    /**
     * Возвращает название игры.
     *
     * @return название игры
     */
    public String getGame() {
        return game;
    }

    /**
     * Устанавливает название игры.
     *
     * @param game название игры
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * Возвращает длительность игры в минутах.
     *
     * @return длительность игры
     */
    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Устанавливает длительность игры в минутах.
     *
     * @param durationMinutes длительность игры
     */
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    /**
     * Возвращает количество единиц стола.
     *
     * @return количество единиц стола
     */
    public Integer getTableUnits() {
        return tableUnits;
    }

    /**
     * Устанавливает количество единиц стола.
     *
     * @param tableUnits количество единиц стола
     */
    public void setTableUnits(Integer tableUnits) {
        this.tableUnits = tableUnits;
    }

    /**
     * Проверяет, выбрана ли пользовательская игра.
     *
     * @return true, если используется пользовательская игра
     */
    public boolean isCustomGame() {
        return customGame;
    }

    /**
     * Устанавливает признак пользовательской игры.
     *
     * @param customGame признак пользовательской игры
     */
    public void setCustomGame(boolean customGame) {
        this.customGame = customGame;
    }

    /**
     * Возвращает идентификатор соперника.
     *
     * @return идентификатор соперника
     */
    public Long getOpponentUserId() {
        return opponentUserId;
    }

    /**
     * Устанавливает идентификатор соперника.
     *
     * @param opponentUserId идентификатор соперника
     */
    public void setOpponentUserId(Long opponentUserId) {
        this.opponentUserId = opponentUserId;
    }

    /**
     * Возвращает имя соперника.
     *
     * @return имя соперника
     */
    public String getOpponentName() {
        return opponentName;
    }

    /**
     * Устанавливает имя соперника.
     *
     * @param opponentName имя соперника
     */
    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    /**
     * Возвращает фракцию соперника.
     *
     * @return фракция соперника
     */
    public String getOpponentFaction() {
        return opponentFaction;
    }

    /**
     * Устанавливает фракцию соперника.
     *
     * @param opponentFaction фракция соперника
     */
    public void setOpponentFaction(String opponentFaction) {
        this.opponentFaction = opponentFaction;
    }

    /**
     * Проверяет, выбрана ли клубная армия.
     *
     * @return true, если выбрана клубная армия
     */
    public boolean isClubArmy() {
        return clubArmy;
    }

    /**
     * Устанавливает признак клубной армии.
     *
     * @param clubArmy признак клубной армии
     */
    public void setClubArmy(boolean clubArmy) {
        this.clubArmy = clubArmy;
    }

    /**
     * Возвращает идентификатор армии.
     *
     * @return идентификатор армии
     */
    public Long getArmyId() {
        return armyId;
    }

    /**
     * Устанавливает идентификатор армии.
     *
     * @param armyId идентификатор армии
     */
    public void setArmyId(Long armyId) {
        this.armyId = armyId;
    }

    /**
     * Возвращает метку выбранной армии.
     *
     * @return метка армии
     */
    public String getArmyLabel() {
        return armyLabel;
    }

    /**
     * Устанавливает метку выбранной армии.
     *
     * @param armyLabel метка армии
     */
    public void setArmyLabel(String armyLabel) {
        this.armyLabel = armyLabel;
    }

    /**
     * Возвращает фракцию пользователя.
     *
     * @return фракция пользователя
     */
    public String getFaction() {
        return faction;
    }

    /**
     * Устанавливает фракцию пользователя.
     *
     * @param faction фракция пользователя
     */
    public void setFaction(String faction) {
        this.faction = faction;
    }

    /**
     * Возвращает название мероприятия.
     *
     * @return название мероприятия
     */
    public String getEventTitle() {
        return eventTitle;
    }

    /**
     * Устанавливает название мероприятия.
     *
     * @param eventTitle название мероприятия
     */
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    /**
     * Возвращает тип мероприятия.
     *
     * @return тип мероприятия
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Устанавливает тип мероприятия.
     *
     * @param eventType тип мероприятия
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Возвращает описание мероприятия.
     *
     * @return описание мероприятия
     */
    public String getEventDescription() {
        return eventDescription;
    }

    /**
     * Устанавливает описание мероприятия.
     *
     * @param eventDescription описание мероприятия
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    /**
     * Проверяет, используется ли пользовательское название мероприятия.
     *
     * @return true, если используется пользовательское название
     */
    public boolean isCustomEventTitle() {
        return customEventTitle;
    }

    /**
     * Устанавливает признак пользовательского названия мероприятия.
     *
     * @param customEventTitle признак пользовательского названия
     */
    public void setCustomEventTitle(boolean customEventTitle) {
        this.customEventTitle = customEventTitle;
    }

    /**
     * Возвращает доступные игры.
     *
     * @return список игр
     */
    public List<GameDto> getAvailableGames() {
        return availableGames;
    }

    /**
     * Устанавливает доступные игры.
     *
     * @param availableGames список игр
     */
    public void setAvailableGames(List<GameDto> availableGames) {
        this.availableGames = availableGames;
    }

    /**
     * Возвращает доступные армии.
     *
     * @return список армий
     */
    public List<ArmyDto> getAvailableArmies() {
        return availableArmies;
    }

    /**
     * Устанавливает доступные армии.
     *
     * @param availableArmies список армий
     */
    public void setAvailableArmies(List<ArmyDto> availableArmies) {
        this.availableArmies = availableArmies;
    }

    /**
     * Возвращает доступные армии выбранной игры.
     *
     * @return список армий выбранной игры
     */
    public List<ArmyDto> getAvailableGameArmies() {
        return availableGameArmies;
    }

    /**
     * Устанавливает доступные армии выбранной игры.
     *
     * @param availableGameArmies список армий выбранной игры
     */
    public void setAvailableGameArmies(List<ArmyDto> availableGameArmies) {
        this.availableGameArmies = availableGameArmies;
    }

    /**
     * Возвращает доступные фракции.
     *
     * @return список фракций
     */
    public List<String> getAvailableFactions() {
        return availableFactions;
    }

    /**
     * Устанавливает доступные фракции.
     *
     * @param availableFactions список фракций
     */
    public void setAvailableFactions(List<String> availableFactions) {
        this.availableFactions = availableFactions;
    }

    /**
     * Возвращает найденных пользователей.
     *
     * @return список пользователей
     */
    public List<UserDto> getFoundUsers() {
        return foundUsers;
    }

    /**
     * Устанавливает найденных пользователей.
     *
     * @param foundUsers список пользователей
     */
    public void setFoundUsers(List<UserDto> foundUsers) {
        this.foundUsers = foundUsers;
    }

    /**
     * Возвращает доступные названия мероприятий.
     *
     * @return список названий мероприятий
     */
    public List<String> getAvailableEventTitles() {
        return availableEventTitles;
    }

    /**
     * Устанавливает доступные названия мероприятий.
     *
     * @param availableEventTitles список названий мероприятий
     */
    public void setAvailableEventTitles(List<String> availableEventTitles) {
        this.availableEventTitles = availableEventTitles;
    }
}
