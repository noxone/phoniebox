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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

/**
 * Wires the audio module's {@link AudioStreamPort} to the media and stream sources.
 *
 * <p>Routes by the kind discriminator from {@link eu.noxone.phoniebox.shared.domain.Playable}:
 *
 * <ul>
 *   <li>{@code MEDIA_FILE} — reads bytes from the local file system via {@link FileStoragePort}
 *   <li>{@code AUDIO_STREAM} — opens a live HTTP connection to the stream URL via {@link
 *       HttpClient}
 * </ul>
 */
@ApplicationScoped
public class MediaFileAudioStreamAdapter implements AudioStreamPort {

  private static final String KIND_MEDIA_FILE = "MEDIA_FILE";
  private static final String KIND_AUDIO_STREAM = "AUDIO_STREAM";

  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

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
    String url = resolvePlaylistUrl(stream.getUrl().getValue());
    return fetchStream(url);
  }

  private InputStream fetchStream(final String url) throws IOException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
    HttpResponse<InputStream> response = send(request, BodyHandlers.ofInputStream());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      response.body().close();
      throw new IOException("HTTP " + response.statusCode() + " opening stream: " + url);
    }
    return response.body();
  }

  private String resolvePlaylistUrl(final String url) throws IOException {
    String path = url;
    int queryIndex = url.indexOf('?');
    if (queryIndex >= 0) {
      path = url.substring(0, queryIndex);
    }
    String lower = path.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".m3u") || lower.endsWith(".m3u8")) {
      return fetchFirstUrlFromPlaylist(url, false);
    }
    if (lower.endsWith(".pls")) {
      return fetchFirstUrlFromPlaylist(url, true);
    }
    return url;
  }

  private String fetchFirstUrlFromPlaylist(final String playlistUrl, final boolean isPls)
      throws IOException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(playlistUrl))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
    HttpResponse<String> response = send(request, BodyHandlers.ofString());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IOException("HTTP " + response.statusCode() + " fetching playlist: " + playlistUrl);
    }
    try (var reader = new BufferedReader(new StringReader(response.body()))) {
      return isPls ? parsePls(reader) : parseM3u(reader);
    }
  }

  private <T> HttpResponse<T> send(
      final HttpRequest request, final HttpResponse.BodyHandler<T> handler) throws IOException {
    try {
      return HTTP_CLIENT.send(request, handler);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted: " + request.uri(), e);
    }
  }

  private String parseM3u(final BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.strip();
      if (!line.isEmpty() && !line.startsWith("#")) {
        return line;
      }
    }
    throw new IOException("No stream URL found in M3U playlist");
  }

  private String parsePls(final BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      String stripped = line.strip();
      if (stripped.toLowerCase(Locale.ROOT).startsWith("file1=")) {
        return stripped.substring("file1=".length()).strip();
      }
    }
    throw new IOException("No stream URL found in PLS playlist");
  }
}
