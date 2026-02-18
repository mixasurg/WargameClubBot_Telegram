alter table club_telegram_settings add column if not exists schedule_twoweeks_message_id integer;
alter table club_telegram_settings add column if not exists schedule_twoweeks_next_message_id integer;
alter table club_telegram_settings add column if not exists events_message_id integer;
