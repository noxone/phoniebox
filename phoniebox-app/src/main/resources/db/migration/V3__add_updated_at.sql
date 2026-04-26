-- Migration V3: last-edited timestamp
--
-- updated_at mirrors uploaded_at on initial upload; the application sets it
-- to the current instant whenever tag metadata is edited.
-- SQLite cannot add a NOT NULL column without a default, so the column is
-- nullable at the DDL level.  The backfill below ensures every existing row
-- has a value, and the application layer always writes a value on insert/update.

ALTER TABLE media_files ADD COLUMN updated_at TEXT;
UPDATE media_files SET updated_at = uploaded_at;
