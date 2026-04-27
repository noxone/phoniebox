package eu.noxone.phoniebox.media.application.port.in;

import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFile;

import java.util.Optional;
import java.util.UUID;

/** Primary port: look up a single media file by its UUID. */
public interface GetMediaFileUseCase {

    Optional<MediaFile> findById(UUID id);
}
