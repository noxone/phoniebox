package eu.noxone.phoniebox.media.application.port.in;

import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFile;

/** Primary port: store a new media file and return its persisted representation. */
public interface UploadMediaFileUseCase {

  MediaFile upload(UploadMediaFileCommand command);
}
