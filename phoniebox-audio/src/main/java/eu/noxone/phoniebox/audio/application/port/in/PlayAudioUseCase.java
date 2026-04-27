package eu.noxone.phoniebox.audio.application.port.in;

import eu.noxone.phoniebox.shared.domain.Playable;

/** Primary port: play back an audio source on the default sound device. */
public interface PlayAudioUseCase {

    /**
     * Plays the given source synchronously, blocking until playback is complete.
     *
     * @throws eu.noxone.phoniebox.audio.application.AudioPlaybackException if the
     *         audio data cannot be read or the sound device is unavailable
     */
    void play(Playable source);
}
