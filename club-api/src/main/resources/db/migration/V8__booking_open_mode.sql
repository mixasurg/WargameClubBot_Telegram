-- Миграция БД: открытые бронирования и join-дедлайн.

alter table booking add column if not exists booking_mode varchar(20) not null default 'FIXED';
alter table booking add column if not exists join_deadline_at timestamptz;
alter table booking add column if not exists cancel_reason varchar(50);

update booking
set booking_mode = 'FIXED'
where booking_mode is null;

create index if not exists idx_booking_open_lookup
    on booking(status, booking_mode, start_at, join_deadline_at)
    where status = 'CREATED' and booking_mode = 'OPEN';
