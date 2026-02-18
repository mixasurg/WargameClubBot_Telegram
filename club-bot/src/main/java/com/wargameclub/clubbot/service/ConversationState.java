package com.wargameclub.clubbot.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubbot.dto.ArmyDto;
import com.wargameclub.clubbot.dto.GameDto;
import com.wargameclub.clubbot.dto.UserDto;

public class ConversationState {
    public enum Flow {
        BOOKING,
        EVENT
    }

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

    private Flow flow;
    private Step step;

    private Long userId;
    private String userName;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;

    private String game;
    private Integer durationMinutes;
    private Integer tableUnits;
    private boolean customGame;

    private Long opponentUserId;
    private String opponentName;
    private String opponentFaction;

    private boolean clubArmy;
    private Long armyId;
    private String armyLabel;
    private String faction;

    private String eventTitle;
    private String eventType;
    private String eventDescription;
    private boolean customEventTitle;

    private List<GameDto> availableGames;
    private List<ArmyDto> availableArmies;
    private List<ArmyDto> availableGameArmies;
    private List<String> availableFactions;
    private List<UserDto> foundUsers;
    private List<String> availableEventTitles;

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getTableUnits() {
        return tableUnits;
    }

    public void setTableUnits(Integer tableUnits) {
        this.tableUnits = tableUnits;
    }

    public boolean isCustomGame() {
        return customGame;
    }

    public void setCustomGame(boolean customGame) {
        this.customGame = customGame;
    }

    public Long getOpponentUserId() {
        return opponentUserId;
    }

    public void setOpponentUserId(Long opponentUserId) {
        this.opponentUserId = opponentUserId;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getOpponentFaction() {
        return opponentFaction;
    }

    public void setOpponentFaction(String opponentFaction) {
        this.opponentFaction = opponentFaction;
    }

    public boolean isClubArmy() {
        return clubArmy;
    }

    public void setClubArmy(boolean clubArmy) {
        this.clubArmy = clubArmy;
    }

    public Long getArmyId() {
        return armyId;
    }

    public void setArmyId(Long armyId) {
        this.armyId = armyId;
    }

    public String getArmyLabel() {
        return armyLabel;
    }

    public void setArmyLabel(String armyLabel) {
        this.armyLabel = armyLabel;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public boolean isCustomEventTitle() {
        return customEventTitle;
    }

    public void setCustomEventTitle(boolean customEventTitle) {
        this.customEventTitle = customEventTitle;
    }

    public List<GameDto> getAvailableGames() {
        return availableGames;
    }

    public void setAvailableGames(List<GameDto> availableGames) {
        this.availableGames = availableGames;
    }

    public List<ArmyDto> getAvailableArmies() {
        return availableArmies;
    }

    public void setAvailableArmies(List<ArmyDto> availableArmies) {
        this.availableArmies = availableArmies;
    }

    public List<ArmyDto> getAvailableGameArmies() {
        return availableGameArmies;
    }

    public void setAvailableGameArmies(List<ArmyDto> availableGameArmies) {
        this.availableGameArmies = availableGameArmies;
    }

    public List<String> getAvailableFactions() {
        return availableFactions;
    }

    public void setAvailableFactions(List<String> availableFactions) {
        this.availableFactions = availableFactions;
    }

    public List<UserDto> getFoundUsers() {
        return foundUsers;
    }

    public void setFoundUsers(List<UserDto> foundUsers) {
        this.foundUsers = foundUsers;
    }

    public List<String> getAvailableEventTitles() {
        return availableEventTitles;
    }

    public void setAvailableEventTitles(List<String> availableEventTitles) {
        this.availableEventTitles = availableEventTitles;
    }
}

