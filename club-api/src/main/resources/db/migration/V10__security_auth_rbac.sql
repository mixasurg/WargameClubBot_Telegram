-- Миграция БД: безопасность (JWT/RBAC), учетные данные пользователей и аудит.

alter table app_user
    add column if not exists login varchar(120);

alter table app_user
    add column if not exists password_hash varchar(120);

alter table app_user
    add column if not exists role varchar(20);

alter table app_user
    add column if not exists is_enabled boolean;

alter table app_user
    add column if not exists last_login_at timestamptz;

update app_user
set role = 'MEMBER'
where role is null;

update app_user
set is_enabled = true
where is_enabled is null;

alter table app_user
    alter column role set default 'MEMBER';

alter table app_user
    alter column role set not null;

alter table app_user
    alter column is_enabled set default true;

alter table app_user
    alter column is_enabled set not null;

create unique index if not exists idx_app_user_login on app_user(lower(login));

insert into app_user(name, login, password_hash, role, is_enabled, created_at)
select 'Administrator', 'admin', '$2b$10$SrV4pSwchGzZoH9aS0mZyeMq10tWEsuaZKlwUe.V1B.G/qN56WJwG', 'ADMIN', true, now()
where not exists (select 1 from app_user where lower(login) = 'admin');

insert into app_user(name, login, password_hash, role, is_enabled, created_at)
select 'Club Bot', 'club_bot', '$2b$10$FLEJbnTtlrmgNUSMUS4Ule.RFp9a9WzQrt6HwPN0oYdwMCfd4GhkK', 'BOT_SERVICE', true, now()
where not exists (select 1 from app_user where lower(login) = 'club_bot');

create table if not exists audit_log (
    id bigserial primary key,
    actor_user_id bigint,
    actor_login varchar(120),
    actor_role varchar(20),
    action varchar(100) not null,
    http_method varchar(10),
    path varchar(255),
    query varchar(500),
    status integer,
    client_ip varchar(64),
    user_agent varchar(255),
    details varchar(1000),
    occurred_at timestamptz not null default now()
);

create index if not exists idx_audit_log_occurred_at on audit_log(occurred_at);
create index if not exists idx_audit_log_actor_user_id on audit_log(actor_user_id);
create index if not exists idx_audit_log_action on audit_log(action);
