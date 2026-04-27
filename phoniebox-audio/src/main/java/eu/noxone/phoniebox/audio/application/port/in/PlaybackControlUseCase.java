package eu.noxone.phoniebox.audio.application.port.in;

import eu.noxone.phoniebox.audio.application.PlaybackState;

import java.util.UUID;

/** Primary port: stateful playback control — select, play, pause. */
public interface PlaybackControlUseCase {

    /** Returns the current playback state without changing it. */
    PlaybackState getState();

    /** Selects a track without starting playback. Stops any current track. */
    PlaybackState setTrack(UUID trackId);

    /** Starts or resumes playback of the currently selected track. No-op if no track is set. */
    PlaybackState play();

    /** Convenience: atomically selects the given track and starts playback. */
    PlaybackState play(UUID trackId);

    /** Pauses playback. No-op if not playing. */
    PlaybackState pause();
}
