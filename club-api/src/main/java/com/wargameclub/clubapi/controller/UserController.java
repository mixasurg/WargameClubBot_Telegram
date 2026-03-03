package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.dto.TelegramUserUpsertRequest;
import com.wargameclub.clubapi.dto.UserDto;
import com.wargameclub.clubapi.dto.UserGameStatsDto;
import com.wargameclub.clubapi.dto.UserPrivateStatsDto;
import com.wargameclub.clubapi.dto.UserRegisterRequest;
import com.wargameclub.clubapi.service.DtoMapper;
import com.wargameclub.clubapi.service.GameResultService;
import com.wargameclub.clubapi.service.LoyaltyService;
import com.wargameclub.clubapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления пользователями.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Сервис пользователя.
     */
    private final UserService userService;

    /**
     * Сервис результата игры.
     */
    private final GameResultService resultService;

    /**
     * Сервис лояльности.
     */
    private final LoyaltyService loyaltyService;

    /**
     * Создает контроллер для операций с пользователями.
     *
     * @param userService сервис пользователей
     * @param resultService сервис фиксации результатов игр
     * @param loyaltyService сервис лояльности
     */
    public UserController(UserService userService, GameResultService resultService, LoyaltyService loyaltyService) {
        this.userService = userService;
        this.resultService = resultService;
        this.loyaltyService = loyaltyService;
    }

    /**
     * Регистрирует нового пользователя клуба.
     *
     * @param request данные регистрации
     * @return зарегистрированный пользователь
     */
    @PostMapping("/register")
    public UserDto register(@Valid @RequestBody UserRegisterRequest request) {
        return DtoMapper.toUserDto(userService.register(request.name()));
    }

    /**
     * Создает или обновляет данные пользователя Telegram.
     *
     * @param request данные Telegram пользователя
     * @return обновленный пользователь
     */
    @PostMapping("/telegram")
    public UserDto upsertTelegram(@Valid @RequestBody TelegramUserUpsertRequest request) {
        return DtoMapper.toUserDto(userService.upsertTelegramUser(request.telegramId(), request.name()));
    }

    /**
     * Возвращает список пользователей по поисковому запросу.
     *
     * @param query строка поиска по имени (опционально)
     * @return список пользователей
     */
    @GetMapping
    public List<UserDto> list(@RequestParam(name = "query", required = false) String query) {
        return userService.searchByName(query).stream()
                .map(DtoMapper::toUserDto)
                .toList();
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    @GetMapping("/{id}")
    public UserDto get(@PathVariable("id") Long id) {
        return DtoMapper.toUserDto(userService.getById(id));
    }

    /**
     * Возвращает статистику результатов игр пользователя.
     *
     * @param id идентификатор пользователя
     * @return статистика пользователя
     */
    @GetMapping("/{id}/stats")
    public UserGameStatsDto getStats(@PathVariable("id") Long id) {
        return DtoMapper.toUserGameStatsDto(resultService.getStats(id));
    }

    /**
     * Возвращает расширенную статистику пользователя для личного меню.
     *
     * @param id идентификатор пользователя
     * @return персональная статистика пользователя
     */
    @GetMapping("/{id}/private-stats")
    public UserPrivateStatsDto getPrivateStats(@PathVariable("id") Long id) {
        GameResultService.ResultSnapshot total = resultService.getResultSnapshot(id, null);
        GameResultService.ResultSnapshot month = resultService.getResultSnapshot(id, OffsetDateTime.now().minusDays(30));
        return new UserPrivateStatsDto(
                id,
                loyaltyService.getPoints(id),
                total.games(),
                month.games(),
                total.winRate(),
                month.winRate()
        );
    }
}
