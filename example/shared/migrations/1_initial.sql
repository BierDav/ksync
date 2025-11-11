create table server_event_log
(
    sequence          integer not null primary key autoincrement,
    events_serialized text   not null,
    author            text   not null,
    author_sequence   integer not null
);

create table base_event_log
(
    sequence integer not null primary key autoincrement
);

create table outgoing_event_log
(
    sequence         integer not null primary key autoincrement,
    event_serialized text   not null
);

create table todo
(
    id      integer not null primary key autoincrement,
    task    text    not null,
    is_done boolean
)