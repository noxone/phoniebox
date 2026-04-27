package eu.noxone.phoniebox.audio.application.service;

import eu.noxone.phoniebox.audio.application.AudioPlaybackException;
import eu.noxone.phoniebox.audio.application.PlaybackState;
import eu.noxone.phoniebox.audio.application.PlaybackStatus;
import eu.noxone.phoniebox.audio.application.port.in.PlayAudioUseCase;
import eu.noxone.phoniebox.audio.application.port.in.PlaybackControlUseCase;
import eu.noxone.phoniebox.audio.application.port.out.AudioStreamPort;
import eu.noxone.phoniebox.shared.domain.Playable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * Stateful audio playback service backed by {@code javax.sound.sampled}.
 *
 * <p>A single daemon thread handles the read/write loop. Control methods ({@link #play()}, {@link
 * #pause()}, {@link #stop()}) interact with the active {@link SourceDataLine} directly from the
 * caller's thread — all state is guarded by the object's intrinsic lock.
 *
 * <p>Pause works by calling {@link SourceDataLine#stop()} which halts audio output; the write loop
 * eventually blocks when the internal buffer fills. Resume calls {@link SourceDataLine#start()}
 * which drains the buffer and unblocks the write. Stop calls {@link SourceDataLine#flush()} after
 * stopping, clearing the buffer so any blocked write can complete and the loop can check the {@code
 * stopRequested} flag and exit cleanly.
 *
 * <p>Supported formats: WAV/AIFF/AU (JDK native) + MP3 (via mp3spi SPI on the classpath). Any
 * compressed format is transparently decoded to PCM.
 */
@ApplicationScoped
public class AudioApplicationService implements PlayAudioUseCase, PlaybackControlUseCase {

  private static final Logger LOG = Logger.getLogger(AudioApplicationService.class.getName());
  private static final int BUFFER_SIZE = 8 * 1024;

  private final AudioStreamPort streamPort;

  // ── All fields below are guarded by 'this' ────────────────────────────────
  private PlaybackStatus status = PlaybackStatus.IDLE;
  private String currentTrackKind;
  private UUID currentTrackId;
  private SourceDataLine activeLine;

  // Written under lock, read from playback thread without lock (volatile)
  private volatile boolean stopRequested;

  @Inject private ManagedExecutor executor;

  @Inject
  public AudioApplicationService(final AudioStreamPort streamPort) {
    this.streamPort = streamPort;
  }

  // ── PlayAudioUseCase ──────────────────────────────────────────────────────

  @Override
  public synchronized void play(final Playable source) {
    play(source.getPlayableKind(), source.getPlayableId());
  }

  // ── PlaybackControlUseCase ────────────────────────────────────────────────

  @Override
  public synchronized PlaybackState getState() {
    return snapshot();
  }

  @Override
  public synchronized PlaybackState setTrack(final String kind, final UUID trackId) {
    stopCurrent();
    currentTrackKind = kind;
    currentTrackId = trackId;
    return snapshot();
  }

  @Override
  public synchronized PlaybackState play() {
    if (currentTrackId == null) return snapshot();
    switch (status) {
      case IDLE -> startPlaybackThread(currentTrackKind, currentTrackId);
      case PAUSED -> resumeLine();
      case PLAYING -> {
        /* already playing, no-op */
      }
    }
    return snapshot();
  }

  @Override
  public synchronized PlaybackState play(final String kind, final UUID trackId) {
    stopCurrent();
    currentTrackKind = kind;
    currentTrackId = trackId;
    startPlaybackThread(kind, trackId);
    return snapshot();
  }

  @Override
  public synchronized PlaybackState pause() {
    if (status == PlaybackStatus.PLAYING) {
      status = PlaybackStatus.PAUSED;
      if (activeLine != null) activeLine.stop();
    }
    return snapshot();
  }

  @Override
  public synchronized PlaybackState stop() {
    stopCurrent();
    currentTrackKind = null;
    currentTrackId = null;
    return snapshot();
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private PlaybackState snapshot() {
    return new PlaybackState(status, currentTrackKind, currentTrackId);
  }

  private void startPlaybackThread(final String kind, final UUID trackId) {
    stopRequested = false;
    status = PlaybackStatus.PLAYING;
    try {
      var stream = streamPort.openStream(kind, trackId);
      executor.execute(() -> runPlayback(kind, trackId, stream));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void resumeLine() {
    status = PlaybackStatus.PLAYING;
    if (activeLine != null) activeLine.start();
  }

  private void stopCurrent() {
    stopRequested = true;
    if (activeLine != null) {
      activeLine.stop();
      activeLine.flush();
    }
    status = PlaybackStatus.IDLE;
    activeLine = null;
  }

  // ── Playback thread ───────────────────────────────────────────────────────

  private void runPlayback(final String kind, final UUID trackId, InputStream stream) {
    if (!(stream instanceof BufferedInputStream)) {
      stream = new BufferedInputStream(stream);
    }
    SourceDataLine line = null;
    try (var raw = stream;
        var audio = decoded(AudioSystem.getAudioInputStream(new BufferedInputStream(raw)))) {

      final AudioFormat format = audio.getFormat();
      line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
      line.open(format);

      synchronized (this) {
        if (stopRequested) {
          return;
        }
        activeLine = line;
        line.start();
      }

      writeLoop(audio, line);

    } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
      LOG.log(Level.WARNING, "Playback error for " + kind + "/" + trackId, e);
      throw new AudioPlaybackException("Playback failed for " + kind + "/" + trackId, e);
    } finally {
      if (line != null) {
        if (!stopRequested) {
          line.drain();
        }
        line.stop();
        line.close();
      }
      synchronized (this) {
        if (activeLine == line) {
          activeLine = null;
        }
        if (!stopRequested && status == PlaybackStatus.PLAYING) {
          status = PlaybackStatus.IDLE;
        }
      }
    }
  }

  private void writeLoop(final AudioInputStream audio, final SourceDataLine line)
      throws IOException {
    final byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    while (!stopRequested && (bytesRead = audio.read(buffer, 0, buffer.length)) != -1) {
      line.write(buffer, 0, bytesRead);
    }
  }

  /**
   * If the stream is compressed (e.g. MP3), converts it to 16-bit signed PCM so {@link
   * SourceDataLine} can play it directly. PCM streams are returned unchanged.
   */
  private static AudioInputStream decoded(final AudioInputStream raw) {
    final AudioFormat format = raw.getFormat();
    if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
        || format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
      return raw;
    }
    final AudioFormat pcm =
        new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            format.getSampleRate(),
            16,
            format.getChannels(),
            format.getChannels() * 2,
            format.getSampleRate(),
            false);
    return AudioSystem.getAudioInputStream(pcm, raw);
  }
}
