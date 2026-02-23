-- Миграция БД: бронирования, игры и пользователи.

alter table app_user add column if not exists telegram_id bigint;
create unique index if not exists idx_app_user_telegram_id on app_user(telegram_id);

alter table booking add column if not exists game varchar(120);
alter table booking add column if not exists table_units integer not null default 2;
alter table booking add column if not exists opponent_user_id bigint references app_user(id);
alter table booking add column if not exists notes text;
alter table booking add column if not exists army_id bigint references army(id);
alter table booking add column if not exists table_assignments jsonb;

update booking
set table_assignments = jsonb_build_array(jsonb_build_object('tableId', table_id, 'units', 2))
where table_assignments is null;

create table if not exists game_catalog (
    id bigserial primary key,
    name varchar(120) not null unique,
    default_duration_minutes integer not null,
    table_units integer not null,
    is_active boolean not null default true,
    created_at timestamptz not null default now()
);

insert into game_catalog (name, default_duration_minutes, table_units, is_active)
values
    ('Warhammer 40K', 180, 2, true),
    ('Kill Team', 120, 1, true),
    ('Age of Sigmar', 180, 2, true),
    ('Necromunda', 150, 2, true),
    ('Blood Bowl', 150, 2, true)
on conflict (name) do nothing;

