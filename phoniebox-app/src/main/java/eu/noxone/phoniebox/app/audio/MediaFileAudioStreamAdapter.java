package eu.noxone.phoniebox.app.audio;

import eu.noxone.phoniebox.audio.application.port.out.AudioStreamPort;
import eu.noxone.phoniebox.media.application.port.out.AudioStreamRepository;
import eu.noxone.phoniebox.media.application.port.out.FileStoragePort;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStreamId;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Wires the audio module's {@link AudioStreamPort} to the media and stream sources.
 *
 * <p>Routes by the kind discriminator from {@link eu.noxone.phoniebox.shared.domain.Playable}:
 *
 * <ul>
 *   <li>{@code MEDIA_FILE} — reads bytes from the local file system via {@link FileStoragePort}
 *   <li>{@code AUDIO_STREAM} — opens a live HTTP connection to the stream URL
 * </ul>
 */
@ApplicationScoped
public class MediaFileAudioStreamAdapter implements AudioStreamPort {

  private static final String KIND_MEDIA_FILE = "MEDIA_FILE";
  private static final String KIND_AUDIO_STREAM = "AUDIO_STREAM";

  private final FileStoragePort storage;
  private final AudioStreamRepository audioStreamRepository;

  @Inject
  public MediaFileAudioStreamAdapter(
      final FileStoragePort storage, final AudioStreamRepository audioStreamRepository) {
    this.storage = storage;
    this.audioStreamRepository = audioStreamRepository;
  }

  @Override
  public InputStream openStream(final String kind, final UUID playableId) throws IOException {
    return switch (kind) {
      case KIND_MEDIA_FILE -> openMediaFile(playableId);
      case KIND_AUDIO_STREAM -> openAudioStream(playableId);
      default -> throw new IllegalArgumentException("Unknown playable kind: " + kind);
    };
  }

  private InputStream openMediaFile(final UUID id) throws IOException {
    return new BufferedInputStream(Files.newInputStream(storage.resolve(MediaFileId.of(id))));
  }

  private InputStream openAudioStream(final UUID id) throws IOException {
    AudioStream stream =
        audioStreamRepository
            .findById(AudioStreamId.of(id))
            .orElseThrow(() -> new IOException("Audio stream not found: " + id));
    return new BufferedInputStream(URI.create(stream.getUrl().getValue()).toURL().openStream());
  }
}
