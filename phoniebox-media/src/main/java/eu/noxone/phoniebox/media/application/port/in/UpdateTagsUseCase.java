package eu.noxone.phoniebox.media.application.port.in;

import eu.noxone.phoniebox.media.domain.model.MediaFile;

import java.util.Optional;

/** Updates the editable tag metadata (title, artist, album, genre) of an existing media file. */
public interface UpdateTagsUseCase {
    Optional<MediaFile> updateTags(UpdateTagsCommand command);
}
