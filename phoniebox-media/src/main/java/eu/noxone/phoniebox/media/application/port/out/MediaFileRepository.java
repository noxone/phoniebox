package eu.noxone.phoniebox.media.application.port.out;

import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFile;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.paging.PageResponse;
import java.util.Optional;

/** Secondary port: persistence contract for {@link MediaFile} aggregates. */
public interface MediaFileRepository {

  void save(MediaFile mediaFile);

  Optional<MediaFile> findById(MediaFileId id);

  PageResponse<MediaFile> findAll(PageRequest pageRequest);

  /**
   * @return {@code true} if a record with the given id existed and was removed
   */
  boolean deleteById(MediaFileId id);
}
