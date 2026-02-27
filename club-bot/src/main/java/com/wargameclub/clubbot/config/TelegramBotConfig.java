package com.wargameclub.clubbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.wargameclub.clubbot.service.TelegramClubBot;

/**
 * Конфигурация регистрации Telegram-бота.
 */
@Configuration
public class TelegramBotConfig {

    /**
     * Создает и регистрирует {@link TelegramBotsApi} с ботом.
     *
     * @param bot экземпляр Telegram-бота
     * @return TelegramBotsApi
     * @throws TelegramApiException при ошибке регистрации бота
     */
    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramClubBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}
