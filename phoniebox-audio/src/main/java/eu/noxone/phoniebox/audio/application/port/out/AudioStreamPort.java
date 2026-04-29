package eu.noxone.phoniebox.audio.application.port.out;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Secondary port: open a raw audio byte stream for a playable source.
 *
 * <p>Implementations are responsible for locating the audio data by the {@code playableId} supplied
 * by a {@link eu.noxone.phoniebox.shared.domain.Playable}. The caller is responsible for closing
 * the returned stream.
 */
public interface AudioStreamPort {

  InputStream openStream(String kind, UUID playableId) throws IOException;
}
