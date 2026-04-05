-- Миграция БД: подтверждение участия в мероприятиях.

alter table event_registration
    add column if not exists confirmation_requested_at timestamptz;

alter table event_registration
    add column if not exists confirmed_at timestamptz;

create index if not exists idx_event_registration_event_status
    on event_registration(event_id, status);

create index if not exists idx_event_registration_confirmation_requested_at
    on event_registration(confirmation_requested_at);
