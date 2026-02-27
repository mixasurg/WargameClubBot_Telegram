package com.wargameclub.clubapi.repository;

import java.util.Optional;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для настроек Telegram клуба.
 */
public interface ClubTelegramSettingsRepository extends JpaRepository<ClubTelegramSettings, Long> {

    /**
     * Возвращает любую запись настроек (первую по chatId).
     *
     * @return настройки Telegram, если есть
     */
    Optional<ClubTelegramSettings> findFirstByOrderByChatIdAsc();

    /**
     * Возвращает настройки по идентификатору чата.
     *
     * @param chatId идентификатор чата
     * @return настройки Telegram, если найдены
     */
    Optional<ClubTelegramSettings> findByChatId(Long chatId);
}
