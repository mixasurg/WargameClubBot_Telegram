package com.wargameclub.clubbot.client;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.wargameclub.clubbot.config.ApiProperties;
import com.wargameclub.clubbot.dto.ArmyCreateRequest;
import com.wargameclub.clubbot.dto.ArmyDto;
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
 * HTTP-клиент для ClubApi.
 */
@Component
public class ClubApiClient {

    /**
     * Поле состояния.
     */
    private final RestTemplate restTemplate;

    /**
     * Параметры конфигурации Api.
     */
    private final ApiProperties apiProperties;

    /**
     * Конструктор ClubApiClient.
     */
    public ClubApiClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    /**
     * Возвращает PendingNotifications.
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
     * Выполняет операцию.
     */
    public void ackNotification(UUID id) {
        restTemplate.postForLocation(baseUrl() + "/api/notifications/" + id + "/ack", null);
    }

    /**
     * Выполняет операцию.
     */
    public void failNotification(UUID id, String error) {
        NotificationFailRequest request = new NotificationFailRequest(error);
        restTemplate.postForLocation(baseUrl() + "/api/notifications/" + id + "/fail", request);
    }

    /**
     * Возвращает WeekDigest.
     */
    public WeekDigestDto getWeekDigest(int offset) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/digest/week")
                .queryParam("offset", offset)
                .toUriString();
        return restTemplate.getForObject(url, WeekDigestDto.class);
    }

    /**
     * Возвращает Events.
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
     * Возвращает EventTitles.
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
     * Возвращает настройки Telegram.
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
     * Создает или обновляет TelegramUser.
     */
    public UserDto upsertTelegramUser(Long telegramId, String name) {
        TelegramUserUpsertRequest request = new TelegramUserUpsertRequest(telegramId, name);
        return restTemplate.postForObject(baseUrl() + "/api/users/telegram", request, UserDto.class);
    }

    /**
     * Возвращает список Users.
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
     * Возвращает Games.
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
     * Создает игру.
     */
    public GameDto createGame(GameCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/games", request, GameDto.class);
    }

    /**
     * Возвращает ClubArmies.
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
     * Возвращает Armies.
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
        System.out.println(response);
        System.out.println(response.getBody());
        return response.getBody();
    }

    /**
     * Создает армию.
     */
    public ArmyDto createArmy(ArmyCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/armies", request, ArmyDto.class);
    }

    /**
     * Создает бронирование.
     */
    public void createBooking(BookingCreateRequest request) {
        restTemplate.postForLocation(baseUrl() + "/api/bookings", request);
    }

    /**
     * Создает мероприятие.
     */
    public EventDto createEvent(EventCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/events", request, EventDto.class);
    }

    /**
     * Выполняет операцию.
     */
    public void submitBookingResult(Long bookingId, BookingResultRequest request) {
        restTemplate.postForLocation(baseUrl() + "/api/bookings/" + bookingId + "/result", request);
    }

    /**
     * Выполняет операцию.
     */
    private String baseUrl() {
        return apiProperties.getBaseUrl();
    }
}

