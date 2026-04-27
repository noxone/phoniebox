package eu.noxone.phoniebox.media.application.port.in;

import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;

import java.util.Optional;
import java.util.UUID;

public interface GetAudioStreamUseCase {

    Optional<AudioStream> get(UUID id);
}
