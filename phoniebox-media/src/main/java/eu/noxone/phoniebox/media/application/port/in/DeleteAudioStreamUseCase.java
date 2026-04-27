package eu.noxone.phoniebox.media.application.port.in;

import java.util.UUID;

public interface DeleteAudioStreamUseCase {

  /**
   * @return {@code true} if the stream existed and was deleted, {@code false} if not found
   */
  boolean delete(UUID id);
}
