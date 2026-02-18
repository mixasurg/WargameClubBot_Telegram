# Отчет по ЛР1

Контекст: система бронирования столов. Термин "билеты" из задания соответствует бронированиям столов и регистрациям на мероприятия.

**3.1 REST API**
- Мероприятия: создание/обновление (POST /api/events, PUT /api/events/{id}) — `club-api/src/main/java/com/wargameclub/clubapi/controller/EventController.java:38`
- Мероприятия: список с фильтрацией по дате/типу (GET /api/events?from&to&type) — `club-api/src/main/java/com/wargameclub/clubapi/controller/EventController.java:56`
- Мероприятия: список заголовков (GET /api/events/titles) — `club-api/src/main/java/com/wargameclub/clubapi/controller/EventController.java:67`
- Регистрация/отмена регистрации (POST /api/events/{id}/register|unregister) — `club-api/src/main/java/com/wargameclub/clubapi/controller/EventController.java:72`
- Бронирования (аналог билетов): создать/получить по периоду/отменить — `club-api/src/main/java/com/wargameclub/clubapi/controller/BookingController.java:34`, `club-api/src/main/java/com/wargameclub/clubapi/controller/BookingController.java:39`, `club-api/src/main/java/com/wargameclub/clubapi/controller/BookingController.java:50`
- Результат игры (POST /api/bookings/{id}/result) — `club-api/src/main/java/com/wargameclub/clubapi/controller/BookingController.java:55`
- Пользователи: регистрация/поиск/получение — `club-api/src/main/java/com/wargameclub/clubapi/controller/UserController.java:31`, `club-api/src/main/java/com/wargameclub/clubapi/controller/UserController.java:41`, `club-api/src/main/java/com/wargameclub/clubapi/controller/UserController.java:48`
- Пользователи: статистика игр — `club-api/src/main/java/com/wargameclub/clubapi/controller/UserController.java:53`
- Пользователи: upsert Telegram — `club-api/src/main/java/com/wargameclub/clubapi/controller/UserController.java:36`
- Игры: каталог (GET/POST /api/games) — `club-api/src/main/java/com/wargameclub/clubapi/controller/GameController.java:25`, `club-api/src/main/java/com/wargameclub/clubapi/controller/GameController.java:32`
- Столы: список — `club-api/src/main/java/com/wargameclub/clubapi/controller/TableController.java:20`
- Армии: создание/список/деактивация/использование — `club-api/src/main/java/com/wargameclub/clubapi/controller/ArmyController.java:27`, `club-api/src/main/java/com/wargameclub/clubapi/controller/ArmyController.java:37`, `club-api/src/main/java/com/wargameclub/clubapi/controller/ArmyController.java:49`, `club-api/src/main/java/com/wargameclub/clubapi/controller/ArmyController.java:54`
- Лояльность: баллы пользователя — `club-api/src/main/java/com/wargameclub/clubapi/controller/LoyaltyController.java:19`
- Дайджест недели — `club-api/src/main/java/com/wargameclub/clubapi/controller/DigestController.java:23`
- Уведомления outbox — `club-api/src/main/java/com/wargameclub/clubapi/controller/NotificationController.java:31`, `club-api/src/main/java/com/wargameclub/clubapi/controller/NotificationController.java:39`, `club-api/src/main/java/com/wargameclub/clubapi/controller/NotificationController.java:44`
- Настройки Telegram — `club-api/src/main/java/com/wargameclub/clubapi/controller/TelegramSettingsController.java:23`, `club-api/src/main/java/com/wargameclub/clubapi/controller/TelegramSettingsController.java:30`
- Валидация входных данных (пример) — `club-api/src/main/java/com/wargameclub/clubapi/dto/BookingCreateRequest.java:10`
- Обработка ошибок/HTTP статусы — `club-api/src/main/java/com/wargameclub/clubapi/exception/GlobalExceptionHandler.java:18`
- Логирование запросов/ответов в БД — `club-api/src/main/java/com/wargameclub/clubapi/filter/RequestLogFilter.java:21`, `club-api/src/main/java/com/wargameclub/clubapi/service/RequestLogService.java:26`, `club-api/src/main/resources/db/migration/V4__request_log.sql:1`
- Доп. логирование HTTP в консоль — `club-api/src/main/java/com/wargameclub/clubapi/config/RequestLoggingConfig.java:8`
- Сложный запрос с параметрами — `club-api/src/main/java/com/wargameclub/clubapi/controller/BookingController.java:35`

**3.2 Pub/Sub**
- Используется Spring Kafka — `club-api/pom.xml:51`
- Конфиг брокера и сериализации — `club-api/src/main/resources/application.yml:15`
- Kafka в docker-compose — `docker-compose.yml:20`
- Топики — `club-api/src/main/java/com/wargameclub/clubapi/messaging/KafkaTopics.java:3`
- Создание топиков и DLQ — `club-api/src/main/java/com/wargameclub/clubapi/config/KafkaConfig.java:45`
- Продюсер — `club-api/src/main/java/com/wargameclub/clubapi/messaging/KafkaEventPublisher.java:9`
- Публикация событий из сервисов (регистрация/обновление/покупка/отмена/бронирования) — `club-api/src/main/java/com/wargameclub/clubapi/service/EventService.java:96`, `club-api/src/main/java/com/wargameclub/clubapi/service/EventService.java:140`, `club-api/src/main/java/com/wargameclub/clubapi/service/EventService.java:158`, `club-api/src/main/java/com/wargameclub/clubapi/service/BookingService.java:125`, `club-api/src/main/java/com/wargameclub/clubapi/service/BookingService.java:148`, `club-api/src/main/java/com/wargameclub/clubapi/service/UserService.java:27`
- Подписчик Email — `club-api/src/main/java/com/wargameclub/clubapi/messaging/EmailNotificationConsumer.java:13`
- Подписчик аналитики — `club-api/src/main/java/com/wargameclub/clubapi/messaging/AnalyticsConsumer.java:15`
- Подписчик напоминаний о бронировании и запроса результата — `club-api/src/main/java/com/wargameclub/clubapi/messaging/BookingReminderConsumer.java:41`
- Подтверждения (ack) — `club-api/src/main/java/com/wargameclub/clubapi/config/KafkaConfig.java:29`, `club-api/src/main/java/com/wargameclub/clubapi/messaging/EmailNotificationConsumer.java:27`, `club-api/src/main/java/com/wargameclub/clubapi/messaging/AnalyticsConsumer.java:22`
- Обработка ошибок + DLQ — `club-api/src/main/java/com/wargameclub/clubapi/config/KafkaConfig.java:35`, `club-api/src/main/java/com/wargameclub/clubapi/messaging/EmailNotificationConsumer.java:19`
- Хранение аналитики в памяти/файл — `club-api/src/main/java/com/wargameclub/clubapi/messaging/AnalyticsService.java:18`

**3.3 Модели данных и хранение**
- JPA модели (пример) — `club-api/src/main/java/com/wargameclub/clubapi/entity/Booking.java:19`
- JPA модели результатов и статистики — `club-api/src/main/java/com/wargameclub/clubapi/entity/BookingResult.java:17`, `club-api/src/main/java/com/wargameclub/clubapi/entity/UserGameStats.java:13`
- Схема БД (Flyway) — `club-api/src/main/resources/db/migration/V1__init.sql:1`, `club-api/src/main/resources/db/migration/V2__booking_games_users.sql:1`, `club-api/src/main/resources/db/migration/V5__telegram_settings_message_ids.sql:1`, `club-api/src/main/resources/db/migration/V6__notification_outbox_reference.sql:1`, `club-api/src/main/resources/db/migration/V7__booking_results_stats.sql:1`
- Настройки БД — `club-api/src/main/resources/application.yml:1`

**3.4 Тестирование и документация**
- Unit тесты (booking/loyalty) — `club-api/src/test/java/com/wargameclub/clubapi/BookingServiceTest.java:20`, `club-api/src/test/java/com/wargameclub/clubapi/LoyaltyServiceTest.java:15`
- Unit тесты Pub/Sub — `club-api/src/test/java/com/wargameclub/clubapi/EventServiceKafkaPublishTest.java:31`, `club-api/src/test/java/com/wargameclub/clubapi/UserServiceKafkaPublishTest.java:16`, `club-api/src/test/java/com/wargameclub/clubapi/AnalyticsServiceTest.java:20`
- Unit тесты логирования — `club-api/src/test/java/com/wargameclub/clubapi/RequestLogFilterTest.java:20`
- Swagger/OpenAPI — `club-api/src/main/java/com/wargameclub/clubapi/config/OpenApiConfig.java:8`
- Postman коллекция — `README.md:125`
- Инструкция запуска — `README.md:11`

**Дополнительно**
- Дайджест расписания показывает пары игроков с фракциями и скрывает пустые дни — `club-bot/src/main/java/com/wargameclub/clubbot/service/DigestFormatter.java:14`
- Фракция соперника запрашивается при бронировании через Telegram-оппонента и сохраняется для дальнейших игр — `club-bot/src/main/java/com/wargameclub/clubbot/service/TelegramClubBot.java:392`
