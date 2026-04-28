CREATE TABLE settings (
    id    TEXT NOT NULL PRIMARY KEY,
    key   TEXT NOT NULL UNIQUE,
    value TEXT
);
