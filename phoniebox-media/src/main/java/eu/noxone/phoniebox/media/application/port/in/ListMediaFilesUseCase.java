package eu.noxone.phoniebox.media.application.port.in;

import eu.noxone.phoniebox.media.domain.model.MediaFile;
import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.paging.PageResponse;

/** Primary port: return a page of stored media files. */
public interface ListMediaFilesUseCase {

    PageResponse<MediaFile> list(PageRequest pageRequest);
}
