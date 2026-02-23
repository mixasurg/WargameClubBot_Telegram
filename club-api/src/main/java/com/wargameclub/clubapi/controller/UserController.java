package com.wargameclub.clubapi.controller;

import com.wargameclub.clubapi.dto.UserDto;
import com.wargameclub.clubapi.dto.UserGameStatsDto;
import com.wargameclub.clubapi.dto.UserRegisterRequest;
import com.wargameclub.clubapi.dto.TelegramUserUpsertRequest;
import com.wargameclub.clubapi.service.DtoMapper;
import com.wargameclub.clubapi.service.GameResultService;
import com.wargameclub.clubapi.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
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
     * Конструктор UserController.
     */
    public UserController(UserService userService, GameResultService resultService) {
        this.userService = userService;
        this.resultService = resultService;
    }

    /**
     * Регистрирует пользователя.
     */
    @PostMapping("/register")
    public UserDto register(@Valid @RequestBody UserRegisterRequest request) {
        return DtoMapper.toUserDto(userService.register(request.name()));
    }

    /**
     * Создает или обновляет Telegram.
     */
    @PostMapping("/telegram")
    public UserDto upsertTelegram(@Valid @RequestBody TelegramUserUpsertRequest request) {
        return DtoMapper.toUserDto(userService.upsertTelegramUser(request.telegramId(), request.name()));
    }

    /**
     * Возвращает список пользователей.
     */
    @GetMapping
    public List<UserDto> list(@RequestParam(name = "query", required = false) String query) {
        return userService.searchByName(query).stream()
                .map(DtoMapper::toUserDto)
                .toList();
    }

    /**
     * Возвращает пользователя.
     */
    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return DtoMapper.toUserDto(userService.getById(id));
    }

    /**
     * Возвращает статистику.
     */
    @GetMapping("/{id}/stats")
    public UserGameStatsDto getStats(@PathVariable Long id) {
        return DtoMapper.toUserGameStatsDto(resultService.getStats(id));
    }
}

