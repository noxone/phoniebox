package eu.noxone.phoniebox.media.application.port.in;

import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;

/** Primary port: persist a new internet radio stream. */
public interface AddAudioStreamUseCase {

  AudioStream add(AddAudioStreamCommand command);
}
