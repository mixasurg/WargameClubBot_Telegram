# Wargame Club

Бэкенд на Java 21 + Spring Boot и фронтенд в виде Telegram-бота. Бот общается с API только по HTTP. Kafka используется для Pub/Sub, а уведомления Telegram доставляются через таблицу outbox и polling.

## Сервисы
- club-api: REST API на Spring Boot (Swagger: `/swagger-ui/index.html`)
- club-bot: Telegram-бот с long polling
- postgres: база данных
- kafka: брокер сообщений для Pub/Sub

## Быстрый старт (Docker)
1. Создайте файл `.env` или экспортируйте переменные (см. ниже).
2. Запустите:
   ```bash
   docker compose up --build
   ```
   Если токена нет, можно поднять только API:
   ```bash
   docker compose up --build postgres kafka club-api
   ```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Переменные окружения
Общие:
- `POSTGRES_DB` (по умолчанию `clubdb`)
- `POSTGRES_USER` (по умолчанию `club`)
- `POSTGRES_PASSWORD` (по умолчанию `club`)

club-api:
- `SPRING_DATASOURCE_URL` (по умолчанию `jdbc:postgresql://localhost:5432/clubdb`)
- `SPRING_DATASOURCE_USERNAME` (по умолчанию `club`)
- `SPRING_DATASOURCE_PASSWORD` (по умолчанию `club`)
- `LOYALTY_POINTS_ARMY_USED` (по умолчанию `10`)
- `LOYALTY_POINTS_ARMY_SHARED` (по умолчанию `5`)
- `APP_TIMEZONE` (по умолчанию `Europe/Moscow`)
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` (по умолчанию `localhost:9092`)
- `NOTIFICATIONS_MAX_ATTEMPTS` (по умолчанию `5`)
- `NOTIFICATIONS_BACKOFF_SECONDS` (по умолчанию `30`)
- `ANALYTICS_FILE` (опционально, путь для сохранения статистики)

club-bot:
- `TELEGRAM_BOT_TOKEN` (обязательно)
- `TELEGRAM_BOT_USERNAME` (по умолчанию `wargameclub_bot`)
- `API_BASE_URL` (по умолчанию `http://club-api:8080` внутри compose)
- `BOT_POLL_INTERVAL_SECONDS` (по умолчанию `10`)

## Настройка Telegram
1. Создайте бота через BotFather и установите `TELEGRAM_BOT_TOKEN`.
2. Добавьте бота в супергруппу с чатом клуба.
3. Создайте топик (thread) для расписания.
4. В нужном топике выполните:
   - `/set_schedule_topic`
5. При необходимости обновите `timezone` через `PUT /api/telegram/settings`.

Все уведомления, дайджесты и мероприятия отправляются в топик расписания.

Настройки хранятся в `club_telegram_settings`. Если настройки не заданы, уведомления пропускаются.

## Команды бота
- `/book` - начать личный диалог бронирования (DM)
- `/event` - начать личный диалог создания мероприятия (DM)
- `/cancel` - отменить текущий личный диалог
- `/week` - отправить дайджест на эту неделю в топик расписания
- `/nextweek` - отправить дайджест на следующую неделю
- `/twoweeks` - отправить дайджест на две недели
- `/events` - отправить список ближайших мероприятий (14 дней)
- `/set_schedule_topic` - привязать текущий топик к расписанию
- `/help`
- `/start` - показать меню в личке

Диалог бронирования в личке спрашивает дату, время, игру (из каталога или свою), длительность, размер стола, соперника, выбор армии и фракции (для своей армии). При выборе клубной армии можно добавить новую. Если выбран соперник по Telegram, бот дополнительно попросит его фракцию.

## API (кратко)
Base URL: `http://localhost:8080`

Пользователи:
- `POST /api/users/register`
- `POST /api/users/telegram`
- `GET /api/users/{id}`
- `GET /api/users?query=...`
- `GET /api/users/{id}/stats`

Игры:
- `GET /api/games?active=true`
- `POST /api/games`

Столы:
- `GET /api/tables`

Бронирования:
- `POST /api/bookings`
- `GET /api/bookings?from=...&to=...&tableId=...`
- `POST /api/bookings/{id}/cancel`
- `POST /api/bookings/{id}/result`

Мероприятия:
- `POST /api/events`
- `PUT /api/events/{id}`
- `GET /api/events?from=...&to=...&type=...`
- `GET /api/events/titles?limit=20`
- `POST /api/events/{id}/register`
- `POST /api/events/{id}/unregister`

Армии:
- `POST /api/armies`
- `GET /api/armies?game=...&faction=...&clubShared=...&active=...`
- `POST /api/armies/{id}/deactivate`
- `POST /api/armies/{id}/use`

Лояльность:
- `GET /api/loyalty/{userId}`

Дайджест:
- `GET /api/digest/week?offset=0|1`

Уведомления:
- `GET /api/notifications/pending?target=TELEGRAM&limit=20`
- `POST /api/notifications/{id}/ack`
- `POST /api/notifications/{id}/fail`

Настройки Telegram:
- `GET /api/telegram/settings`
- `PUT /api/telegram/settings`

## Postman
Коллекция: `postman/wargame-club.postman_collection.json`

## Заметки / TODO
- `tableUnits` измеряется в половинах стола (1 = 0.5 стола, 2 = 1 стол, 3 = 1.5 стола, 6 = все столы).
- `tableUnits` сейчас принимает значения `1, 2, 3, 4, 6` (значение `5` отклоняется валидацией).
- Каталог игр засеян; для реальных игр обновите `game_catalog` в БД.
- Напоминание за день до игры отправляется в личку при наличии `telegram_id` у пользователя.
- В дайджесте расписания указывается соперник и фракции игроков, если они заданы.
- Пустые дни в расписании не выводятся.
- Через 1.5 часа после окончания игры бот просит отметить результат и обновляет личную статистику.
- Если топик расписания не настроен, бот пишет в текущий чат или топик.

## Kafka Pub/Sub
Топики:
- `ticket.purchased`
- `ticket.cancelled`
- `booking.created`
- `booking.cancelled`
- `event.updated`
- `user.registered`
