package eu.noxone.phoniebox.media.infrastructure.audio;

import eu.noxone.phoniebox.media.application.port.out.AudioMetadataExtractor;
import eu.noxone.phoniebox.media.domain.model.AudioMetadata;
import eu.noxone.phoniebox.media.domain.model.TrackAlbum;
import eu.noxone.phoniebox.media.domain.model.TrackArtist;
import eu.noxone.phoniebox.media.domain.model.TrackBitrate;
import eu.noxone.phoniebox.media.domain.model.TrackDuration;
import eu.noxone.phoniebox.media.domain.model.TrackGenre;
import eu.noxone.phoniebox.media.domain.model.TrackNumber;
import eu.noxone.phoniebox.media.domain.model.TrackSampleRate;
import eu.noxone.phoniebox.media.domain.model.TrackTitle;
import eu.noxone.phoniebox.media.domain.model.TrackYear;
import jakarta.enterprise.context.ApplicationScoped;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;

/** Extracts audio metadata using the JAudioTagger library. */
@ApplicationScoped
public class JaudiotaggerAudioMetadataExtractor implements AudioMetadataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(JaudiotaggerAudioMetadataExtractor.class);

    static {
        // JAudioTagger uses java.util.logging — silence it to avoid console noise
        java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.WARNING);
    }

    @Override
    public Optional<AudioMetadata> extract(final Path filePath) {
        try {
            final var audioFile = AudioFileIO.readMagic(filePath.toFile());
            final AudioHeader header = audioFile.getAudioHeader();
            final Tag tag = audioFile.getTag();

            final var duration   = TrackDuration.of(header.getTrackLength());
            final var bitrate    = TrackBitrate.of((int) header.getBitRateAsNumber());
            final var sampleRate = TrackSampleRate.of(header.getSampleRateAsNumber());

            TrackTitle  title       = null;
            TrackArtist artist      = null;
            TrackAlbum  album       = null;
            TrackNumber trackNumber = null;
            TrackYear   year        = null;
            TrackGenre  genre       = null;

            if (tag != null) {
                title       = stringTag(tag, FieldKey.TITLE,  TrackTitle.class);
                artist      = stringTag(tag, FieldKey.ARTIST, TrackArtist.class);
                album       = stringTag(tag, FieldKey.ALBUM,  TrackAlbum.class);
                genre       = stringTag(tag, FieldKey.GENRE,  TrackGenre.class);
                trackNumber = intTag(tag, FieldKey.TRACK, TrackNumber.class);
                year        = intTag(tag, FieldKey.YEAR,  TrackYear.class);
            }

            return Optional.of(new AudioMetadata(duration, bitrate, sampleRate,
                    title, artist, album, trackNumber, year, genre));
        } catch (final Exception e) {
            LOG.warn("Could not extract audio metadata from {}: {}", filePath.getFileName(), e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static <A> A stringTag(final Tag tag, final FieldKey key, final Class<A> type) {
        try {
            final String value = tag.getFirst(key);
            if (value == null || value.isBlank()) return null;
            return (A) type.getMethod("of", String.class).invoke(null, value);
        } catch (final Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <A> A intTag(final Tag tag, final FieldKey key, final Class<A> type) {
        try {
            final String raw = tag.getFirst(key);
            if (raw == null || raw.isBlank()) return null;
            // track numbers may arrive as "3/12"; take only the part before the slash
            final String trimmed = raw.contains("/") ? raw.substring(0, raw.indexOf('/')).trim() : raw.trim();
            final int value = Integer.parseInt(trimmed);
            return (A) type.getMethod("of", int.class).invoke(null, value);
        } catch (final Exception e) {
            return null;
        }
    }
}
