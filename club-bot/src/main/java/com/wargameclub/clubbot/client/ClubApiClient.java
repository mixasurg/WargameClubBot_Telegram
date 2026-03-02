package com.wargameclub.clubbot.client;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.wargameclub.clubbot.config.ApiProperties;
import com.wargameclub.clubbot.dto.ArmyCreateRequest;
import com.wargameclub.clubbot.dto.ArmyDto;
import com.wargameclub.clubbot.dto.ArmyClubShareUpdateRequest;
import com.wargameclub.clubbot.dto.BookingCreateRequest;
import com.wargameclub.clubbot.dto.BookingResultRequest;
import com.wargameclub.clubbot.dto.EventCreateRequest;
import com.wargameclub.clubbot.dto.EventDto;
import com.wargameclub.clubbot.dto.GameCreateRequest;
import com.wargameclub.clubbot.dto.GameDto;
import com.wargameclub.clubbot.dto.NotificationFailRequest;
import com.wargameclub.clubbot.dto.NotificationOutboxDto;
import com.wargameclub.clubbot.dto.TelegramSettingsDto;
import com.wargameclub.clubbot.dto.TelegramSettingsUpdateRequest;
import com.wargameclub.clubbot.dto.TelegramUserUpsertRequest;
import com.wargameclub.clubbot.dto.UserDto;
import com.wargameclub.clubbot.dto.UserPrivateStatsDto;
import com.wargameclub.clubbot.dto.WeekDigestDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP-клиент для взаимодействия с club-api из club-bot.
 */
@Component
public class ClubApiClient {

    /**
     * RestTemplate для выполнения HTTP-запросов.
     */
    private final RestTemplate restTemplate;

    /**
     * Настройки доступа к API.
     */
    private final ApiProperties apiProperties;

    /**
     * Создает API-клиент.
     *
     * @param restTemplate RestTemplate
     * @param apiProperties настройки API
     */
    public ClubApiClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    /**
     * Возвращает список ожидающих уведомлений для Telegram.
     *
     * @param limit максимальное число уведомлений
     * @return список уведомлений
     */
    public List<NotificationOutboxDto> getPendingNotifications(int limit) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/notifications/pending")
                .queryParam("target", "TELEGRAM")
                .queryParam("limit", limit)
                .toUriString();
        ResponseEntity<List<NotificationOutboxDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Подтверждает успешную отправку уведомления.
     *
     * @param id идентификатор уведомления
     */
    public void ackNotification(UUID id) {
        restTemplate.postForLocation(baseUrl() + "/api/notifications/" + id + "/ack", null);
    }

    /**
     * Помечает уведомление как ошибочное и сохраняет причину.
     *
     * @param id идентификатор уведомления
     * @param error описание ошибки
     */
    public void failNotification(UUID id, String error) {
        NotificationFailRequest request = new NotificationFailRequest(error);
        restTemplate.postForLocation(baseUrl() + "/api/notifications/" + id + "/fail", request);
    }

    /**
     * Возвращает недельный дайджест.
     *
     * @param offset смещение недели (0 — текущая, 1 — следующая)
     * @return недельный дайджест
     */
    public WeekDigestDto getWeekDigest(int offset) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/digest/week")
                .queryParam("offset", offset)
                .toUriString();
        return restTemplate.getForObject(url, WeekDigestDto.class);
    }

    /**
     * Возвращает мероприятия, пересекающие заданный интервал.
     *
     * @param from начало интервала
     * @param to конец интервала
     * @return список мероприятий
     */
    public List<EventDto> getEvents(OffsetDateTime from, OffsetDateTime to) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/events")
                .queryParam("from", from)
                .queryParam("to", to)
                .toUriString();
        ResponseEntity<List<EventDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Возвращает список названий мероприятий для автодополнения.
     *
     * @param limit максимальное число названий
     * @return список названий мероприятий
     */
    public List<String> getEventTitles(int limit) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/events/titles")
                .queryParam("limit", limit)
                .toUriString();
        ResponseEntity<List<String>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Возвращает текущие настройки Telegram или {@code null}, если они не заданы.
     *
     * @return настройки Telegram или null
     */
    public TelegramSettingsDto getTelegramSettings() {
        try {
            return restTemplate.getForObject(baseUrl() + "/api/telegram/settings", TelegramSettingsDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        }
    }

    /**
     * Обновляет настройки Telegram.
     *
     * @param request запрос на обновление настроек
     * @return обновленные настройки
     */
    public TelegramSettingsDto updateTelegramSettings(TelegramSettingsUpdateRequest request) {
        HttpEntity<TelegramSettingsUpdateRequest> entity = new HttpEntity<>(request);
        ResponseEntity<TelegramSettingsDto> response = restTemplate.exchange(
                baseUrl() + "/api/telegram/settings",
                HttpMethod.PUT,
                entity,
                TelegramSettingsDto.class
        );
        return response.getBody();
    }

    /**
     * Создает или обновляет пользователя Telegram.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @param name имя пользователя
     * @return DTO пользователя
     */
    public UserDto upsertTelegramUser(Long telegramId, String name) {
        TelegramUserUpsertRequest request = new TelegramUserUpsertRequest(telegramId, name);
        return restTemplate.postForObject(baseUrl() + "/api/users/telegram", request, UserDto.class);
    }

    /**
     * Возвращает расширенную статистику пользователя для личного меню.
     *
     * @param userId идентификатор пользователя
     * @return статистика пользователя
     */
    public UserPrivateStatsDto getUserPrivateStats(Long userId) {
        return restTemplate.getForObject(baseUrl() + "/api/users/" + userId + "/private-stats", UserPrivateStatsDto.class);
    }

    /**
     * Выполняет поиск пользователей по имени.
     *
     * @param query строка поиска (опционально)
     * @return список пользователей
     */
    public List<UserDto> searchUsers(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/users")
                .queryParam("query", query)
                .toUriString();
        ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Возвращает список активных игр.
     *
     * @return список игр
     */
    public List<GameDto> getGames() {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/games")
                .queryParam("active", true)
                .toUriString();
        ResponseEntity<List<GameDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Создает игру в каталоге.
     *
     * @param request запрос на создание игры
     * @return DTO созданной игры
     */
    public GameDto createGame(GameCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/games", request, GameDto.class);
    }

    /**
     * Возвращает список активных клубных армий.
     *
     * @return список клубных армий
     */
    public List<ArmyDto> getClubArmies() {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/armies")
                .queryParam("clubShared", true)
                .queryParam("active", true)
                .toUriString();
        ResponseEntity<List<ArmyDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Возвращает список армий с учетом фильтров.
     *
     * @param game название игры (опционально)
     * @param clubShared признак клубной армии (опционально)
     * @param ownerUserId идентификатор владельца (опционально)
     * @param active признак активности (опционально)
     * @return список армий
     */
    public List<ArmyDto> getArmies(String game, Boolean clubShared, Long ownerUserId, Boolean active) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/armies");
        if (game != null && !game.isBlank()) {
            builder.queryParam("game", game);
        }
        if (clubShared != null) {
            builder.queryParam("clubShared", clubShared);
        }
        if (ownerUserId != null) {
            builder.queryParam("ownerUserId", ownerUserId);
        }
        if (active != null) {
            builder.queryParam("active", active);
        }
        String url = builder.toUriString();
        ResponseEntity<List<ArmyDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    /**
     * Обновляет доступность армии для использования клубом.
     *
     * @param armyId идентификатор армии
     * @param ownerUserId идентификатор владельца армии
     * @param clubShared новый признак доступности для клуба
     * @return обновленная армия
     */
    public ArmyDto updateArmyClubShare(Long armyId, Long ownerUserId, boolean clubShared) {
        ArmyClubShareUpdateRequest request = new ArmyClubShareUpdateRequest(ownerUserId, clubShared);
        ResponseEntity<ArmyDto> response = restTemplate.exchange(
                baseUrl() + "/api/armies/" + armyId + "/club-share",
                HttpMethod.POST,
                new HttpEntity<>(request),
                ArmyDto.class
        );
        return response.getBody();
    }

    /**
     * Создает армию.
     *
     * @param request запрос на создание армии
     * @return DTO созданной армии
     */
    public ArmyDto createArmy(ArmyCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/armies", request, ArmyDto.class);
    }

    /**
     * Создает бронирование.
     *
     * @param request запрос на создание бронирования
     */
    public void createBooking(BookingCreateRequest request) {
        restTemplate.postForLocation(baseUrl() + "/api/bookings", request);
    }

    /**
     * Создает мероприятие.
     *
     * @param request запрос на создание мероприятия
     * @return DTO созданного мероприятия
     */
    public EventDto createEvent(EventCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/events", request, EventDto.class);
    }

    /**
     * Отправляет результат игры по бронированию.
     *
     * @param bookingId идентификатор бронирования
     * @param request запрос с результатом
     */
    public void submitBookingResult(Long bookingId, BookingResultRequest request) {
        restTemplate.postForLocation(baseUrl() + "/api/bookings/" + bookingId + "/result", request);
    }

    /**
     * Возвращает базовый URL club-api.
     *
     * @return базовый URL
     */
    private String baseUrl() {
        return apiProperties.getBaseUrl();
    }
}
