package eu.noxone.phoniebox.media.web.rest;

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
        Instant uploadedAt
) {

    public static MediaFileResponse from(final MediaFile domain) {
        return new MediaFileResponse(
                domain.getId().getValue(),
                domain.getMetadata().getOriginalFileName().getValue(),
                domain.getMetadata().getMimeType().getValue(),
                domain.getMetadata().getSizeInBytes().getValue(),
                domain.getUploadedAt().getValue()
        );
    }
}
