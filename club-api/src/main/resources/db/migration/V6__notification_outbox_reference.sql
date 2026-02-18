alter table notification_outbox add column if not exists reference_type varchar(50);
alter table notification_outbox add column if not exists reference_id bigint;

create index if not exists idx_notification_outbox_reference
    on notification_outbox(reference_type, reference_id);
