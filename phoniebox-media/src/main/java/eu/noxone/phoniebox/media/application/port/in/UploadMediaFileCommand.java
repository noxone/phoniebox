package eu.noxone.phoniebox.media.application.port.in;

import java.io.InputStream;

/**
 * Carries all data required to upload a new media file.
 *
 * <p>The {@code content} stream is consumed exactly once by the application service.
 * Callers must not close or re-read the stream after passing it in.
 */
public record UploadMediaFileCommand(
        String originalFileName,
        String mimeType,
        long sizeInBytes,
        InputStream content
) {
}
