package eu.noxone.phoniebox.media.application.port.in;

import eu.noxone.phoniebox.media.domain.model.MediaFile;

import java.util.List;

/** Primary port: return all stored media files. */
public interface ListMediaFilesUseCase {

    List<MediaFile> listAll();
}
