-- Migration V2: audio metadata columns
--
-- All columns are nullable: extraction may fail or the file may have no tags.
-- Durations and rates are stored as INTEGER (SQLite affinity); bitrate in kbps.

ALTER TABLE media_files ADD COLUMN track_duration_seconds INTEGER;
ALTER TABLE media_files ADD COLUMN track_bitrate_kbps     INTEGER;
ALTER TABLE media_files ADD COLUMN track_sample_rate_hz   INTEGER;
ALTER TABLE media_files ADD COLUMN track_title            TEXT;
ALTER TABLE media_files ADD COLUMN track_artist           TEXT;
ALTER TABLE media_files ADD COLUMN track_album            TEXT;
ALTER TABLE media_files ADD COLUMN track_number           INTEGER;
ALTER TABLE media_files ADD COLUMN track_year             INTEGER;
ALTER TABLE media_files ADD COLUMN track_genre            TEXT;
