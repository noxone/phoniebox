package eu.noxone.phoniebox.audio.application.port.in;

/** Primary port: change the audio output volume. */
public interface SetVolumeUseCase {

  /** Sets the volume. {@code volume} must be in the range [0, 100]. */
  void setVolume(int volume);
}
