package eu.noxone.phoniebox.media.application.port.in;

import java.util.UUID;

/** Primary port: permanently remove a media file and its stored bytes. */
public interface DeleteMediaFileUseCase {

  /**
   * @param id the UUID of the file to delete
   * @return {@code true} if the file existed and was deleted, {@code false} if it was not found
   */
  boolean delete(UUID id);
}
