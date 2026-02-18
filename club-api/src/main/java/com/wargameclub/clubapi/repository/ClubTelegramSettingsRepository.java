package com.wargameclub.clubapi.repository;

import java.util.Optional;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubTelegramSettingsRepository extends JpaRepository<ClubTelegramSettings, Long> {
    Optional<ClubTelegramSettings> findFirstByOrderByChatIdAsc();

    Optional<ClubTelegramSettings> findByChatId(Long chatId);
}

