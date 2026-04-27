package eu.noxone.phoniebox.media.infrastructure.storage;

import eu.noxone.phoniebox.media.application.port.out.FileStoragePort;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Stores uploaded files on the local file system.
 *
 * <p>Each file is saved under its UUID as the sole filename (no extension). The original name and
 * MIME type live in the database, so the storage layer stays intentionally simple.
 *
 * <p>Configure the root directory with:
 *
 * <pre>phoniebox.media.storage-path=/var/lib/phoniebox/media</pre>
 */
@ApplicationScoped
public class LocalFileStorageAdapter implements FileStoragePort {

  private final Path storageDirectory;

  public LocalFileStorageAdapter(
      @ConfigProperty(name = "phoniebox.media.storage-path") final Path storageDirectory) {
    this.storageDirectory = storageDirectory;
    ensureDirectoryExists(storageDirectory);
  }

  @Override
  public void store(final MediaFileId id, final InputStream content) {
    try {
      Files.copy(content, resolve(id), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to store file with id: " + id, e);
    }
  }

  @Override
  public Path resolve(final MediaFileId id) {
    return storageDirectory.resolve(id.asString());
  }

  @Override
  public boolean delete(final MediaFileId id) {
    try {
      return Files.deleteIfExists(resolve(id));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to delete file with id: " + id, e);
    }
  }

  private static void ensureDirectoryExists(final Path directory) {
    try {
      Files.createDirectories(directory);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot create storage directory: " + directory, e);
    }
  }
}
