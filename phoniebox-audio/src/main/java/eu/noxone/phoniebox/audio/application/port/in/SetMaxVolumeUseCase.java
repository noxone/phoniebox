package eu.noxone.phoniebox.audio.application.port.in;

/** Primary port: change the maximum allowed volume. */
public interface SetMaxVolumeUseCase {

  /** Sets the maximum volume limit. {@code maxVolume} must be in the range [0, 100]. */
  void setMaxVolume(int maxVolume);
}
