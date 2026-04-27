package eu.noxone.phoniebox.media.domain.model.audio;

import eu.noxone.phoniebox.shared.domain.DomainAttribute;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Compound value object for audio track metadata extracted from the uploaded file.
 *
 * <p>All fields are nullable — technical fields (duration, bitrate, sample rate) come
 * from the audio stream header and are usually present; tag fields (title, artist, …)
 * depend on whether the file was tagged.  If metadata extraction fails entirely, the
 * {@code audioMetadata} field on {@link MediaFile} is {@code null}.
 *
 * <p>Marked {@code @Embeddable} so Hibernate maps its columns directly into the
 * {@code media_files} table alongside the entity's own columns.
 */
@Embeddable
public class AudioMetadata implements DomainAttribute {

    /** Duration of the track in seconds. */
    @Column(name = "track_duration_seconds")
    private TrackDuration duration;

    /** Bitrate in kilobits per second. */
    @Column(name = "track_bitrate_kbps")
    private TrackBitrate bitrate;

    /** Sample rate in hertz (e.g. 44100, 48000). */
    @Column(name = "track_sample_rate_hz")
    private TrackSampleRate sampleRate;

    @Column(name = "track_title")
    private TrackTitle title;

    @Column(name = "track_artist")
    private TrackArtist artist;

    @Column(name = "track_album")
    private TrackAlbum album;

    @Column(name = "track_number")
    private TrackNumber trackNumber;

    @Column(name = "track_year")
    private TrackYear year;

    @Column(name = "track_genre")
    private TrackGenre genre;

    /** Required by JPA. Not for use by application code. */
    protected AudioMetadata() {
    }

    public AudioMetadata(
            final TrackDuration duration,
            final TrackBitrate bitrate,
            final TrackSampleRate sampleRate,
            final TrackTitle title,
            final TrackArtist artist,
            final TrackAlbum album,
            final TrackNumber trackNumber,
            final TrackYear year,
            final TrackGenre genre) {
        this.duration = duration;
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.trackNumber = trackNumber;
        this.year = year;
        this.genre = genre;
    }

    public TrackDuration getDuration()       { return duration; }
    public TrackBitrate getBitrate()         { return bitrate; }
    public TrackSampleRate getSampleRate()   { return sampleRate; }
    public TrackTitle getTitle()             { return title; }
    public TrackArtist getArtist()           { return artist; }
    public TrackAlbum getAlbum()             { return album; }
    public TrackNumber getTrackNumber()      { return trackNumber; }
    public TrackYear getYear()               { return year; }
    public TrackGenre getGenre()             { return genre; }

    /** Returns a new instance with updated tag fields; technical fields (duration, bitrate, sample rate, track number, year) are preserved. */
    public AudioMetadata withTags(final TrackTitle title, final TrackArtist artist, final TrackAlbum album, final TrackGenre genre) {
        return new AudioMetadata(this.duration, this.bitrate, this.sampleRate,
                title, artist, album, this.trackNumber, this.year, genre);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioMetadata that)) return false;
        return Objects.equals(duration, that.duration)
                && Objects.equals(bitrate, that.bitrate)
                && Objects.equals(sampleRate, that.sampleRate)
                && Objects.equals(title, that.title)
                && Objects.equals(artist, that.artist)
                && Objects.equals(album, that.album)
                && Objects.equals(trackNumber, that.trackNumber)
                && Objects.equals(year, that.year)
                && Objects.equals(genre, that.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, bitrate, sampleRate, title, artist, album, trackNumber, year, genre);
    }
}
