package com.wargameclub.clubbot.dto;

/**
 * DTO запроса на создание армии.
 */
public record ArmyCreateRequest(
        Long ownerUserId,
        String game,
        String faction,
        boolean isClubShared
) {
}
