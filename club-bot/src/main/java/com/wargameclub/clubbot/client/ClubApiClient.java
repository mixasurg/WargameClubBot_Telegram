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

@Component
public class ClubApiClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public ClubApiClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

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

    public void ackNotification(UUID id) {
        restTemplate.postForLocation(baseUrl() + "/api/notifications/" + id + "/ack", null);
    }

    public void failNotification(UUID id, String error) {
        NotificationFailRequest request = new NotificationFailRequest(error);
        restTemplate.postForLocation(baseUrl() + "/api/notifications/" + id + "/fail", request);
    }

    public WeekDigestDto getWeekDigest(int offset) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/digest/week")
                .queryParam("offset", offset)
                .toUriString();
        return restTemplate.getForObject(url, WeekDigestDto.class);
    }

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

    public TelegramSettingsDto getTelegramSettings() {
        try {
            return restTemplate.getForObject(baseUrl() + "/api/telegram/settings", TelegramSettingsDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        }
    }

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

    public UserDto upsertTelegramUser(Long telegramId, String name) {
        TelegramUserUpsertRequest request = new TelegramUserUpsertRequest(telegramId, name);
        return restTemplate.postForObject(baseUrl() + "/api/users/telegram", request, UserDto.class);
    }

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

    public GameDto createGame(GameCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/games", request, GameDto.class);
    }

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

    public List<ArmyDto> getArmies(String game, Boolean clubShared, Boolean active) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .path("/api/armies");
        if (game != null && !game.isBlank()) {
            builder.queryParam("game", game);
        }
        if (clubShared != null) {
            builder.queryParam("clubShared", clubShared);
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

    public ArmyDto createArmy(ArmyCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/armies", request, ArmyDto.class);
    }

    public void createBooking(BookingCreateRequest request) {
        restTemplate.postForLocation(baseUrl() + "/api/bookings", request);
    }

    public EventDto createEvent(EventCreateRequest request) {
        return restTemplate.postForObject(baseUrl() + "/api/events", request, EventDto.class);
    }

    public void submitBookingResult(Long bookingId, BookingResultRequest request) {
        restTemplate.postForLocation(baseUrl() + "/api/bookings/" + bookingId + "/result", request);
    }

    private String baseUrl() {
        return apiProperties.getBaseUrl();
    }
}

