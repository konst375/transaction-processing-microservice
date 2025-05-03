CREATE KEYSPACE IF NOT EXISTS exchange_rate_keyspace
    WITH replication = {
        'class': 'SimpleStrategy',
        'replication_factor': 1
        };

USE exchange_rate_keyspace;

CREATE TABLE IF NOT EXISTS exchange_rate
(
    base   TEXT,
    target TEXT,
    date   DATE,
    rate   DECIMAL,
    PRIMARY KEY ((base, target), date)
) WITH CLUSTERING ORDER BY (date DESC)
   AND default_time_to_live = 2592000;
