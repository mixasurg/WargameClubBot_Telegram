-- Миграция БД: базовая схема.

create table app_user (
    id bigserial primary key,
    name varchar(100) not null,
    created_at timestamptz not null default now()
);

create table club_table (
    id bigserial primary key,
    name varchar(50) not null,
    is_active boolean not null default true,
    notes text
);

insert into club_table (name, is_active, notes) values
    ('Table-1', true, null),
    ('Table-2', true, null),
    ('Table-3', true, null);

create table booking (
    id bigserial primary key,
    table_id bigint not null references club_table(id),
    user_id bigint not null references app_user(id),
    start_at timestamptz not null,
    end_at timestamptz not null,
    status varchar(20) not null,
    created_at timestamptz not null default now()
);

create index idx_booking_table_time on booking(table_id, start_at, end_at, status);

create table club_event (
    id bigserial primary key,
    title varchar(200) not null,
    type varchar(30) not null,
    description text,
    start_at timestamptz not null,
    end_at timestamptz not null,
    organizer_user_id bigint not null references app_user(id),
    capacity integer,
    status varchar(20) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table event_registration (
    id bigserial primary key,
    event_id bigint not null references club_event(id) on delete cascade,
    user_id bigint not null references app_user(id) on delete cascade,
    status varchar(20) not null,
    created_at timestamptz not null default now(),
    unique (event_id, user_id)
);

create table army (
    id bigserial primary key,
    owner_user_id bigint not null references app_user(id),
    game varchar(100) not null,
    faction varchar(100) not null,
    is_club_shared boolean not null default false,
    is_active boolean not null default true,
    created_at timestamptz not null default now()
);

create table army_usage (
    id bigserial primary key,
    army_id bigint not null references army(id),
    used_by_user_id bigint not null references app_user(id),
    used_at timestamptz not null,
    notes text,
    created_at timestamptz not null default now()
);

create table loyalty_account (
    user_id bigint primary key references app_user(id),
    points integer not null default 0
);

create table notification_outbox (
    id uuid primary key,
    target varchar(20) not null,
    chat_routing jsonb not null,
    text text not null,
    status varchar(20) not null,
    attempts integer not null default 0,
    next_attempt_at timestamptz not null default now(),
    created_at timestamptz not null default now(),
    sent_at timestamptz,
    last_error text
);

create index idx_notification_pending on notification_outbox(target, status, next_attempt_at);

create table club_telegram_settings (
    chat_id bigint primary key,
    schedule_thread_id integer,
    events_thread_id integer,
    timezone varchar(50) not null default 'Europe/Moscow'
);

