package com.wargameclub.clubbot.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubbot.dto.ArmyDto;
import com.wargameclub.clubbot.dto.GameDto;
import com.wargameclub.clubbot.dto.UserDto;

/**
 * Сервис для работы с сущностью ConversationState.
 */
public class ConversationState {

    /**
     * Сервис для работы с сущностью Flow.
     */
    public enum Flow {
        BOOKING,
        EVENT
    }

    /**
     * Сервис для работы с сущностью Step.
     */
    public enum Step {
        PICK_DATE,
        PICK_TIME,
        PICK_GAME,
        PICK_CUSTOM_GAME,
        PICK_DURATION,
        PICK_TABLE_UNITS,
        PICK_OPPONENT,
        PICK_OPPONENT_FACTION,
        PICK_ARMY_TYPE,
        PICK_ARMY,
        PICK_OWN_FACTION,
        PICK_CLUB_FACTION,
        CONFIRM,
        EVENT_TITLE,
        EVENT_TITLE_CUSTOM,
        EVENT_TYPE,
        EVENT_DATE,
        EVENT_TIME,
        EVENT_END_TIME,
        EVENT_DESCRIPTION,
        EVENT_CONFIRM
    }

    /**
     * Поле состояния.
     */
    private Flow flow;

    /**
     * Поле состояния.
     */
    private Step step;

    /**
     * Поле состояния.
     */
    private Long userId;

    /**
     * Поле состояния.
     */
    private String userName;

    /**
     * Поле состояния.
     */
    private LocalDate date;

    /**
     * Поле состояния.
     */
    private LocalTime startTime;

    /**
     * Поле состояния.
     */
    private LocalTime endTime;

    /**
     * Поле состояния.
     */
    private OffsetDateTime startAt;

    /**
     * Поле состояния.
     */
    private OffsetDateTime endAt;

    /**
     * Поле состояния.
     */
    private String game;

    /**
     * Поле состояния.
     */
    private Integer durationMinutes;

    /**
     * Поле состояния.
     */
    private Integer tableUnits;

    /**
     * Поле состояния.
     */
    private boolean customGame;

    /**
     * Поле состояния.
     */
    private Long opponentUserId;

    /**
     * Поле состояния.
     */
    private String opponentName;

    /**
     * Поле состояния.
     */
    private String opponentFaction;

    /**
     * Поле состояния.
     */
    private boolean clubArmy;

    /**
     * Поле состояния.
     */
    private Long armyId;

    /**
     * Поле состояния.
     */
    private String armyLabel;

    /**
     * Поле состояния.
     */
    private String faction;

    /**
     * Поле состояния.
     */
    private String eventTitle;

    /**
     * Поле состояния.
     */
    private String eventType;

    /**
     * Поле состояния.
     */
    private String eventDescription;

    /**
     * Поле состояния.
     */
    private boolean customEventTitle;

    /**
     * Поле состояния.
     */
    private List<GameDto> availableGames;

    /**
     * Поле состояния.
     */
    private List<ArmyDto> availableArmies;

    /**
     * Поле состояния.
     */
    private List<ArmyDto> availableGameArmies;

    /**
     * Поле состояния.
     */
    private List<String> availableFactions;

    /**
     * Поле состояния.
     */
    private List<UserDto> foundUsers;

    /**
     * Поле состояния.
     */
    private List<String> availableEventTitles;

    /**
     * Возвращает Flow.
     */
    public Flow getFlow() {
        return flow;
    }

    /**
     * Устанавливает Flow.
     */
    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    /**
     * Возвращает Step.
     */
    public Step getStep() {
        return step;
    }

    /**
     * Устанавливает Step.
     */
    public void setStep(Step step) {
        this.step = step;
    }

    /**
     * Возвращает идентификатор пользователя.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Устанавливает идентификатор пользователя.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Возвращает UserName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Устанавливает UserName.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Возвращает Date.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Устанавливает Date.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Возвращает StartTime.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Устанавливает StartTime.
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Возвращает EndTime.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Устанавливает EndTime.
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Возвращает время начала.
     */
    public OffsetDateTime getStartAt() {
        return startAt;
    }

    /**
     * Устанавливает время начала.
     */
    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    /**
     * Возвращает время окончания.
     */
    public OffsetDateTime getEndAt() {
        return endAt;
    }

    /**
     * Устанавливает время окончания.
     */
    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    /**
     * Возвращает игру.
     */
    public String getGame() {
        return game;
    }

    /**
     * Устанавливает игру.
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * Возвращает DurationMinutes.
     */
    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Устанавливает DurationMinutes.
     */
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    /**
     * Возвращает количество столов.
     */
    public Integer getTableUnits() {
        return tableUnits;
    }

    /**
     * Устанавливает количество столов.
     */
    public void setTableUnits(Integer tableUnits) {
        this.tableUnits = tableUnits;
    }

    /**
     * Проверяет CustomGame.
     */
    public boolean isCustomGame() {
        return customGame;
    }

    /**
     * Устанавливает CustomGame.
     */
    public void setCustomGame(boolean customGame) {
        this.customGame = customGame;
    }

    /**
     * Возвращает идентификатор соперника.
     */
    public Long getOpponentUserId() {
        return opponentUserId;
    }

    /**
     * Устанавливает идентификатор соперника.
     */
    public void setOpponentUserId(Long opponentUserId) {
        this.opponentUserId = opponentUserId;
    }

    /**
     * Возвращает OpponentName.
     */
    public String getOpponentName() {
        return opponentName;
    }

    /**
     * Устанавливает OpponentName.
     */
    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    /**
     * Возвращает фракцию соперника.
     */
    public String getOpponentFaction() {
        return opponentFaction;
    }

    /**
     * Устанавливает фракцию соперника.
     */
    public void setOpponentFaction(String opponentFaction) {
        this.opponentFaction = opponentFaction;
    }

    /**
     * Проверяет ClubArmy.
     */
    public boolean isClubArmy() {
        return clubArmy;
    }

    /**
     * Устанавливает ClubArmy.
     */
    public void setClubArmy(boolean clubArmy) {
        this.clubArmy = clubArmy;
    }

    /**
     * Возвращает идентификатор армии.
     */
    public Long getArmyId() {
        return armyId;
    }

    /**
     * Устанавливает идентификатор армии.
     */
    public void setArmyId(Long armyId) {
        this.armyId = armyId;
    }

    /**
     * Возвращает ArmyLabel.
     */
    public String getArmyLabel() {
        return armyLabel;
    }

    /**
     * Устанавливает ArmyLabel.
     */
    public void setArmyLabel(String armyLabel) {
        this.armyLabel = armyLabel;
    }

    /**
     * Возвращает фракцию.
     */
    public String getFaction() {
        return faction;
    }

    /**
     * Устанавливает фракцию.
     */
    public void setFaction(String faction) {
        this.faction = faction;
    }

    /**
     * Возвращает EventTitle.
     */
    public String getEventTitle() {
        return eventTitle;
    }

    /**
     * Устанавливает EventTitle.
     */
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    /**
     * Возвращает EventType.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Устанавливает EventType.
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Возвращает EventDescription.
     */
    public String getEventDescription() {
        return eventDescription;
    }

    /**
     * Устанавливает EventDescription.
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    /**
     * Проверяет CustomEventTitle.
     */
    public boolean isCustomEventTitle() {
        return customEventTitle;
    }

    /**
     * Устанавливает CustomEventTitle.
     */
    public void setCustomEventTitle(boolean customEventTitle) {
        this.customEventTitle = customEventTitle;
    }

    /**
     * Возвращает AvailableGames.
     */
    public List<GameDto> getAvailableGames() {
        return availableGames;
    }

    /**
     * Устанавливает AvailableGames.
     */
    public void setAvailableGames(List<GameDto> availableGames) {
        this.availableGames = availableGames;
    }

    /**
     * Возвращает AvailableArmies.
     */
    public List<ArmyDto> getAvailableArmies() {
        return availableArmies;
    }

    /**
     * Устанавливает AvailableArmies.
     */
    public void setAvailableArmies(List<ArmyDto> availableArmies) {
        this.availableArmies = availableArmies;
    }

    /**
     * Возвращает AvailableGameArmies.
     */
    public List<ArmyDto> getAvailableGameArmies() {
        return availableGameArmies;
    }

    /**
     * Устанавливает AvailableGameArmies.
     */
    public void setAvailableGameArmies(List<ArmyDto> availableGameArmies) {
        this.availableGameArmies = availableGameArmies;
    }

    /**
     * Возвращает AvailableFactions.
     */
    public List<String> getAvailableFactions() {
        return availableFactions;
    }

    /**
     * Устанавливает AvailableFactions.
     */
    public void setAvailableFactions(List<String> availableFactions) {
        this.availableFactions = availableFactions;
    }

    /**
     * Возвращает FoundUsers.
     */
    public List<UserDto> getFoundUsers() {
        return foundUsers;
    }

    /**
     * Устанавливает FoundUsers.
     */
    public void setFoundUsers(List<UserDto> foundUsers) {
        this.foundUsers = foundUsers;
    }

    /**
     * Возвращает AvailableEventTitles.
     */
    public List<String> getAvailableEventTitles() {
        return availableEventTitles;
    }

    /**
     * Устанавливает AvailableEventTitles.
     */
    public void setAvailableEventTitles(List<String> availableEventTitles) {
        this.availableEventTitles = availableEventTitles;
    }
}

