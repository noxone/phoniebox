package eu.noxone.phoniebox.app.audio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpServer;
import eu.noxone.phoniebox.app.http.HttpClientProvider;
import eu.noxone.phoniebox.media.application.port.out.AudioStreamRepository;
import eu.noxone.phoniebox.media.application.port.out.FileStoragePort;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;
import eu.noxone.phoniebox.media.domain.model.audiostream.StreamName;
import eu.noxone.phoniebox.media.domain.model.audiostream.StreamUrl;
import eu.noxone.phoniebox.media.domain.model.shared.MimeType;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MediaFileAudioStreamAdapterTest {

  private HttpServer server;
  private String baseUrl;
  private final Map<String, byte[]> responseBodies = new HashMap<>();
  private final Map<String, Integer> responseStatuses = new HashMap<>();

  private final FileStoragePort storage = mock(FileStoragePort.class);
  private final AudioStreamRepository audioStreamRepository = mock(AudioStreamRepository.class);
  private final HttpClientProvider httpClientProvider = mock(HttpClientProvider.class);
  private final OkHttpClient httpClient =
      new OkHttpClient.Builder()
          .connectTimeout(Duration.ofSeconds(5))
          .readTimeout(Duration.ofSeconds(5))
          .writeTimeout(Duration.ofSeconds(5))
          .build();
  private final MediaFileAudioStreamAdapter adapter =
      new MediaFileAudioStreamAdapter(storage, audioStreamRepository);

  @BeforeEach
  void startServer() throws IOException, ReflectiveOperationException {
    responseBodies.clear();
    responseStatuses.clear();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/",
        exchange -> {
          String path = exchange.getRequestURI().getPath();
          int status = responseStatuses.getOrDefault(path, 200);
          byte[] body = responseBodies.getOrDefault(path, new byte[0]);
          exchange.sendResponseHeaders(status, body.length);
          try (var out = exchange.getResponseBody()) {
            out.write(body);
          }
        });
    server.start();
    baseUrl = "http://localhost:" + server.getAddress().getPort();
    when(httpClientProvider.get()).thenReturn(httpClient);
    Field field = MediaFileAudioStreamAdapter.class.getDeclaredField("httpClientProvider");
    field.setAccessible(true);
    field.set(adapter, httpClientProvider);
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
    reset(storage, audioStreamRepository, httpClientProvider);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void serve(final String path, final int status, final String body) {
    responseStatuses.put(path, status);
    responseBodies.put(path, body.getBytes(StandardCharsets.UTF_8));
  }

  private void serve(final String path, final int status, final byte[] body) {
    responseStatuses.put(path, status);
    responseBodies.put(path, body);
  }

  private String url(final String path) {
    return baseUrl + path;
  }

  private AudioStream streamAt(final String url) {
    return AudioStream.create(StreamName.of("Test"), StreamUrl.of(url), MimeType.of("audio/mpeg"));
  }

  // ── AUDIO_STREAM: direct URL ──────────────────────────────────────────────

  @Test
  void openStream_audioStream_returnsResponseBody() throws IOException {
    byte[] expected = "audio data".getBytes(StandardCharsets.UTF_8);
    serve("/stream", 200, expected);
    when(audioStreamRepository.findById(any())).thenReturn(Optional.of(streamAt(url("/stream"))));

    try (InputStream is = adapter.openStream("AUDIO_STREAM", UUID.randomUUID())) {
      assertArrayEquals(expected, is.readAllBytes());
    }
  }

  @Test
  void openStream_audioStream_throwsOnHttpError() {
    serve("/stream", 500, "");
    when(audioStreamRepository.findById(any())).thenReturn(Optional.of(streamAt(url("/stream"))));

    assertThrows(IOException.class, () -> adapter.openStream("AUDIO_STREAM", UUID.randomUUID()));
  }

  @Test
  void openStream_audioStream_throwsWhenStreamNotInRepository() {
    when(audioStreamRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(IOException.class, () -> adapter.openStream("AUDIO_STREAM", UUID.randomUUID()));
  }

  // ── AUDIO_STREAM: M3U playlist ────────────────────────────────────────────

  @Test
  void openStream_audioStream_resolvesM3uPlaylist() throws IOException {
    byte[] streamBody = "radio content".getBytes(StandardCharsets.UTF_8);
    serve("/playlist.m3u", 200, "#EXTM3U\n" + url("/stream.mp3") + "\n");
    serve("/stream.mp3", 200, streamBody);
    when(audioStreamRepository.findById(any()))
        .thenReturn(Optional.of(streamAt(url("/playlist.m3u"))));

    try (InputStream is = adapter.openStream("AUDIO_STREAM", UUID.randomUUID())) {
      assertArrayEquals(streamBody, is.readAllBytes());
    }
  }

  @Test
  void openStream_audioStream_m3uPlaylist_skipsCommentAndExtinfLines() throws IOException {
    byte[] streamBody = "radio content".getBytes(StandardCharsets.UTF_8);
    String m3u = "#EXTM3U\n#EXTINF:0,Radio Name\n" + url("/stream.mp3") + "\n";
    serve("/playlist.m3u", 200, m3u);
    serve("/stream.mp3", 200, streamBody);
    when(audioStreamRepository.findById(any()))
        .thenReturn(Optional.of(streamAt(url("/playlist.m3u"))));

    try (InputStream is = adapter.openStream("AUDIO_STREAM", UUID.randomUUID())) {
      assertArrayEquals(streamBody, is.readAllBytes());
    }
  }

  @Test
  void openStream_audioStream_resolvesM3u8Playlist() throws IOException {
    byte[] streamBody = "hls content".getBytes(StandardCharsets.UTF_8);
    serve("/playlist.m3u8", 200, url("/stream.mp3") + "\n");
    serve("/stream.mp3", 200, streamBody);
    when(audioStreamRepository.findById(any()))
        .thenReturn(Optional.of(streamAt(url("/playlist.m3u8"))));

    try (InputStream is = adapter.openStream("AUDIO_STREAM", UUID.randomUUID())) {
      assertArrayEquals(streamBody, is.readAllBytes());
    }
  }

  @Test
  void openStream_audioStream_throwsWhenM3uPlaylistReturnsError() {
    serve("/playlist.m3u", 404, "");
    when(audioStreamRepository.findById(any()))
        .thenReturn(Optional.of(streamAt(url("/playlist.m3u"))));

    assertThrows(IOException.class, () -> adapter.openStream("AUDIO_STREAM", UUID.randomUUID()));
  }

  // ── AUDIO_STREAM: PLS playlist ────────────────────────────────────────────

  @Test
  void openStream_audioStream_resolvesPlsPlaylist() throws IOException {
    byte[] streamBody = "pls stream content".getBytes(StandardCharsets.UTF_8);
    String pls = "[playlist]\nFile1=" + url("/stream.mp3") + "\nNumberOfEntries=1\n";
    serve("/playlist.pls", 200, pls);
    serve("/stream.mp3", 200, streamBody);
    when(audioStreamRepository.findById(any()))
        .thenReturn(Optional.of(streamAt(url("/playlist.pls"))));

    try (InputStream is = adapter.openStream("AUDIO_STREAM", UUID.randomUUID())) {
      assertArrayEquals(streamBody, is.readAllBytes());
    }
  }

  @Test
  void openStream_audioStream_throwsWhenPlsPlaylistReturnsError() {
    serve("/playlist.pls", 503, "");
    when(audioStreamRepository.findById(any()))
        .thenReturn(Optional.of(streamAt(url("/playlist.pls"))));

    assertThrows(IOException.class, () -> adapter.openStream("AUDIO_STREAM", UUID.randomUUID()));
  }

  // ── MEDIA_FILE ────────────────────────────────────────────────────────────

  @Test
  void openStream_mediaFile_readsFromStorage() throws IOException {
    byte[] content = "local file bytes".getBytes(StandardCharsets.UTF_8);
    Path tmp = Files.createTempFile("phoniebox-test", ".mp3");
    try {
      Files.write(tmp, content);
      when(storage.resolve(any())).thenReturn(tmp);

      try (InputStream is = adapter.openStream("MEDIA_FILE", UUID.randomUUID())) {
        assertArrayEquals(content, is.readAllBytes());
      }
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  // ── Unknown kind ──────────────────────────────────────────────────────────

  @Test
  void openStream_unknownKind_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class, () -> adapter.openStream("UNKNOWN", UUID.randomUUID()));
  }
}
