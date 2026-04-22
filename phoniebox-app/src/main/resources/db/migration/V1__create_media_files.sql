-- Migration V1: initial schema
--
-- SQLite type affinity notes:
--   TEXT  → stores UUID strings and ISO-8601 timestamps
--   INTEGER → exact integer; covers file sizes up to 2^63-1 bytes

CREATE TABLE IF NOT EXISTS media_files (
    id                 TEXT    NOT NULL PRIMARY KEY,
    original_file_name TEXT    NOT NULL,
    mime_type          TEXT    NOT NULL,
    size_in_bytes      INTEGER NOT NULL,
    uploaded_at        TEXT    NOT NULL   -- ISO-8601, e.g. 2024-03-15T10:30:00Z
);
