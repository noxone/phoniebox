package eu.noxone.phoniebox.audio.application.port.in;

/** Primary port: query the configured maximum volume limit. */
public interface GetMaxVolumeUseCase {

  /** Returns the maximum allowed volume in the range [0, 100]. */
  int getMaxVolume();
}
