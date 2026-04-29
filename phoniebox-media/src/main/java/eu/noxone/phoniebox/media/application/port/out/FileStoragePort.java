package eu.noxone.phoniebox.media.application.port.out;

import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import java.io.InputStream;
import java.nio.file.Path;

/** Secondary port: binary-file storage contract (local filesystem, S3, …). */
public interface FileStoragePort {

  /** Persists the bytes from {@code content} under the given {@code id}. */
  void store(MediaFileId id, InputStream content);

  /** Returns the local path where the bytes for {@code id} reside. */
  Path resolve(MediaFileId id);

  /**
   * Removes the stored bytes.
   *
   * @return {@code true} if the file existed and was deleted
   */
  boolean delete(MediaFileId id);
}
