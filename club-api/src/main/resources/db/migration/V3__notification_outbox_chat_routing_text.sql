alter table notification_outbox
    alter column chat_routing type text
    using chat_routing::text;
