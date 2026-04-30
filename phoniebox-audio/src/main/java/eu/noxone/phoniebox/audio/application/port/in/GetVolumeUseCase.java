package eu.noxone.phoniebox.audio.application.port.in;

/** Primary port: query the current audio output volume. */
public interface GetVolumeUseCase {

  /** Returns the current volume in the range [0, 100]. */
  int getVolume();
}
