package eu.noxone.phoniebox.audio.application.port.in;

import eu.noxone.phoniebox.audio.application.PlaybackState;

import java.util.UUID;

/** Primary port: stateful playback control — select, play, pause, stop. */
public interface PlaybackControlUseCase {

    /** Returns the current playback state without changing it. */
    PlaybackState getState();

    /** Selects a source without starting playback. Stops any current track. */
    PlaybackState setTrack(String kind, UUID trackId);

    /** Starts or resumes playback of the currently selected source. No-op if no source is set. */
    PlaybackState play();

    /** Convenience: atomically selects the given source and starts playback. */
    PlaybackState play(String kind, UUID trackId);

    /** Pauses playback. No-op if not playing. */
    PlaybackState pause();

    /** Stops playback and clears the current source. */
    PlaybackState stop();
}
