-- Миграция БД: журнал HTTP-запросов.

create table if not exists request_log (
    id bigserial primary key,
    method varchar(10) not null,
    path varchar(300) not null,
    query varchar(500),
    status integer not null,
    duration_ms bigint not null,
    remote_addr varchar(100),
    user_agent varchar(300),
    created_at timestamptz not null default now()
);

create index if not exists idx_request_log_created_at on request_log(created_at);
create index if not exists idx_request_log_path on request_log(path);
