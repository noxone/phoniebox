package eu.noxone.phoniebox.audio.application.service;

import eu.noxone.phoniebox.audio.application.AudioPlaybackException;
import eu.noxone.phoniebox.audio.application.AudioSettingKeys;
import eu.noxone.phoniebox.audio.application.PlaybackState;
import eu.noxone.phoniebox.audio.application.PlaybackStatus;
import eu.noxone.phoniebox.audio.application.port.in.GetVolumeUseCase;
import eu.noxone.phoniebox.audio.application.port.in.PlayAudioUseCase;
import eu.noxone.phoniebox.audio.application.port.in.PlaybackControlUseCase;
import eu.noxone.phoniebox.audio.application.port.in.SetVolumeUseCase;
import eu.noxone.phoniebox.audio.application.port.out.AudioStreamPort;
import eu.noxone.phoniebox.settings.application.port.in.GetSettingUseCase;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingCommand;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingUseCase;
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
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
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
 *
 * <p>Volume is applied via {@link FloatControl.Type#MASTER_GAIN} when the hardware supports it,
 * falling back to {@link FloatControl.Type#VOLUME}. When neither control is available (typical on
 * Raspberry Pi ALSA output), 16-bit signed PCM samples are scaled in the write loop. The scale
 * factor is held in {@link #softVolumeScale} (volatile) so the write loop can read it without
 * holding the lock — the same pattern used for {@link #stopRequested}.
 */
@ApplicationScoped
public class AudioApplicationService
    implements PlayAudioUseCase, PlaybackControlUseCase, GetVolumeUseCase, SetVolumeUseCase {

  private static final Logger LOG = Logger.getLogger(AudioApplicationService.class.getName());
  private static final int BUFFER_SIZE = 8 * 1024;
  private static final int DEFAULT_VOLUME = 80;

  private final AudioStreamPort streamPort;
  private final GetSettingUseCase getSetting;
  private final SetSettingUseCase setSetting;

  // ── All fields below are guarded by 'this' ────────────────────────────────
  private PlaybackStatus status = PlaybackStatus.IDLE;
  private String currentTrackKind;
  private UUID currentTrackId;
  private SourceDataLine activeLine;
  private int volume;

  // Written under lock, read from playback thread without lock (volatile)
  private volatile boolean stopRequested;
  private volatile float softVolumeScale;

  @Inject private ManagedExecutor executor;

  @Inject
  public AudioApplicationService(
      final AudioStreamPort streamPort,
      final GetSettingUseCase getSetting,
      final SetSettingUseCase setSetting) {
    this.streamPort = streamPort;
    this.getSetting = getSetting;
    this.setSetting = setSetting;
    this.volume =
        getSetting
            .getSetting(AudioSettingKeys.VOLUME)
            .map(Integer::parseInt)
            .orElse(DEFAULT_VOLUME);
    this.softVolumeScale = this.volume / 100f;
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

  // ── GetVolumeUseCase / SetVolumeUseCase ───────────────────────────────────

  @Override
  public synchronized int getVolume() {
    return volume;
  }

  @Override
  public synchronized void setVolume(final int newVolume) {
    if (newVolume < 0 || newVolume > 100) {
      throw new IllegalArgumentException("Volume must be between 0 and 100, was: " + newVolume);
    }
    volume = newVolume;
    softVolumeScale = newVolume / 100f; // picked up by write loop on next iteration
    setSetting.setSetting(
        new SetSettingCommand(AudioSettingKeys.VOLUME, String.valueOf(newVolume)));
    if (activeLine != null) {
      applyVolumeHardware(activeLine, newVolume);
    }
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
      String mixerName = getSetting.getSetting(AudioSettingKeys.SELECTED_MIXER_NAME).orElse(null);
      executor.execute(() -> runPlayback(kind, trackId, stream, mixerName));
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

  private void runPlayback(
      final String kind, final UUID trackId, final InputStream stream, final String mixerName) {
    SourceDataLine line = null;
    try (var raw = stream;
        var audio = decoded(AudioSystem.getAudioInputStream(new BufferedInputStream(raw)))) {

      final AudioFormat format = audio.getFormat();
      final DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, format);
      line =
          mixerName != null
              ? openOnMixer(mixerName, lineInfo)
              : (SourceDataLine) AudioSystem.getLine(lineInfo);
      line.open(format);

      final boolean hardwareVolume;
      synchronized (this) {
        if (stopRequested) {
          return;
        }
        activeLine = line;
        hardwareVolume = applyVolumeHardware(line, volume);
        line.start();
      }

      writeLoop(audio, line, hardwareVolume);

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

  private void writeLoop(
      final AudioInputStream audio, final SourceDataLine line, final boolean hardwareVolume)
      throws IOException {
    final byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    while (!stopRequested && (bytesRead = audio.read(buffer, 0, buffer.length)) != -1) {
      if (!hardwareVolume) {
        scaleSamples(buffer, bytesRead, softVolumeScale);
      }
      line.write(buffer, 0, bytesRead);
    }
  }

  private static SourceDataLine openOnMixer(final String mixerName, final DataLine.Info lineInfo)
      throws LineUnavailableException {
    for (Mixer.Info info : AudioSystem.getMixerInfo()) {
      if (info.getName().equals(mixerName)) {
        try {
          return (SourceDataLine) AudioSystem.getMixer(info).getLine(lineInfo);
        } catch (LineUnavailableException e) {
          break; // mixer found but doesn't support this format — fall back to default
        }
      }
    }
    return (SourceDataLine) AudioSystem.getLine(lineInfo);
  }

  /**
   * Attempts to apply {@code volume} (0–100) via hardware controls on the line. Returns {@code
   * true} if a hardware control was found and set; {@code false} if software scaling must be used.
   */
  private static boolean applyVolumeHardware(final SourceDataLine line, final int volume) {
    if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
      var ctrl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
      float dB = volume == 0 ? ctrl.getMinimum() : 20f * (float) Math.log10(volume / 100.0);
      ctrl.setValue(Math.max(ctrl.getMinimum(), Math.min(ctrl.getMaximum(), dB)));
      return true;
    }
    if (line.isControlSupported(FloatControl.Type.VOLUME)) {
      var ctrl = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
      ctrl.setValue(Math.max(0f, Math.min(1f, volume / 100f)));
      return true;
    }
    LOG.fine("No hardware volume control; using software sample scaling");
    return false;
  }

  /**
   * Scales 16-bit signed little-endian PCM samples in-place by {@code scale}. At full volume (scale
   * == 1.0) the buffer is left untouched.
   */
  private static void scaleSamples(final byte[] buf, final int len, final float scale) {
    if (scale == 1.0f) return;
    for (int i = 0; i + 1 < len; i += 2) {
      int sample = (buf[i + 1] << 8) | (buf[i] & 0xFF);
      sample = Math.round(sample * scale);
      if (sample > 32767) sample = 32767;
      else if (sample < -32768) sample = -32768;
      buf[i] = (byte) (sample & 0xFF);
      buf[i + 1] = (byte) (sample >> 8);
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
