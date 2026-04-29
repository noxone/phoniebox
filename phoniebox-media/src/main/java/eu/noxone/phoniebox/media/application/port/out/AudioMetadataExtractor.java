package eu.noxone.phoniebox.media.application.port.out;

import eu.noxone.phoniebox.media.domain.model.audio.AudioMetadata;
import java.nio.file.Path;
import java.util.Optional;

/** Secondary port: reads audio metadata (tags and stream properties) from a local file. */
public interface AudioMetadataExtractor {

  /**
   * Attempts to extract audio metadata from the file at {@code filePath}.
   *
   * @return an {@link Optional} containing the extracted metadata, or empty if the file format is
   *     unsupported or the extraction fails
   */
  Optional<AudioMetadata> extract(Path filePath);
}
