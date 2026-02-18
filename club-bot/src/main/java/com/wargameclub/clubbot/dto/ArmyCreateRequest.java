package com.wargameclub.clubbot.dto;

public record ArmyCreateRequest(
        Long ownerUserId,
        String game,
        String faction,
        boolean isClubShared
) {
}
