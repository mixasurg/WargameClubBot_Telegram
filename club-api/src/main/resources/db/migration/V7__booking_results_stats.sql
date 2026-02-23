-- Миграция БД: результаты игр и статистика.

create table if not exists booking_result (
    booking_id bigint primary key references booking(id) on delete cascade,
    reporter_user_id bigint not null references app_user(id),
    outcome varchar(10) not null,
    recorded_at timestamp with time zone not null default now()
);

create table if not exists user_game_stats (
    user_id bigint primary key references app_user(id) on delete cascade,
    wins integer not null default 0,
    losses integer not null default 0,
    draws integer not null default 0,
    updated_at timestamp with time zone not null default now()
);
