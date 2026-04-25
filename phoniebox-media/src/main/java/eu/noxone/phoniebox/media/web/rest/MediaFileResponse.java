package eu.noxone.phoniebox.media.web.rest;

import eu.noxone.phoniebox.media.domain.model.AudioMetadata;
import eu.noxone.phoniebox.media.domain.model.MediaFile;

import java.time.Instant;
import java.util.UUID;

/**
 * JSON response DTO for a single media file.
 *
 * <p>Deliberately kept flat (no nesting) for simple frontend consumption.
 * Domain attribute values are unwrapped here — the web layer is the correct
 * place to convert domain types to serialisable primitives.
 * Mapping from the domain aggregate happens via the static factory {@link #from}.
 */
public record MediaFileResponse(
        UUID id,
        String originalFileName,
        String mimeType,
        long sizeInBytes,
        Instant uploadedAt,
        // audio metadata — all nullable; null when extraction failed or file has no tags
        Integer durationSeconds,
        Integer bitrateKbps,
        Integer sampleRateHz,
        String trackTitle,
        String trackArtist,
        String trackAlbum,
        Integer trackNumber,
        Integer trackYear,
        String trackGenre
) {

    public static MediaFileResponse from(final MediaFile domain) {
        final AudioMetadata audio = domain.getAudioMetadata().orElse(null);
        return new MediaFileResponse(
                domain.getId().getValue(),
                domain.getMetadata().getOriginalFileName().getValue(),
                domain.getMetadata().getMimeType().getValue(),
                domain.getMetadata().getSizeInBytes().getValue(),
                domain.getUploadedAt().getValue(),
                audio != null && audio.getDuration()   != null ? audio.getDuration().getValue()   : null,
                audio != null && audio.getBitrate()    != null ? audio.getBitrate().getValue()    : null,
                audio != null && audio.getSampleRate() != null ? audio.getSampleRate().getValue() : null,
                audio != null && audio.getTitle()      != null ? audio.getTitle().getValue()      : null,
                audio != null && audio.getArtist()     != null ? audio.getArtist().getValue()     : null,
                audio != null && audio.getAlbum()      != null ? audio.getAlbum().getValue()      : null,
                audio != null && audio.getTrackNumber()!= null ? audio.getTrackNumber().getValue(): null,
                audio != null && audio.getYear()       != null ? audio.getYear().getValue()       : null,
                audio != null && audio.getGenre()      != null ? audio.getGenre().getValue()      : null
        );
    }
}
