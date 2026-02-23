package com.wargameclub.clubapi.repository;

import java.util.Optional;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для ClubTelegramSettings.
 */
public interface ClubTelegramSettingsRepository extends JpaRepository<ClubTelegramSettings, Long> {

    /**
     * Возвращает FirstByOrderByChatIdAsc.
     */
    Optional<ClubTelegramSettings> findFirstByOrderByChatIdAsc();

    /**
     * Возвращает ClubTelegramSettings.
     */
    Optional<ClubTelegramSettings> findByChatId(Long chatId);
}

