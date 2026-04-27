package eu.noxone.phoniebox.audio.application;

import java.util.UUID;

/** Snapshot of the current audio playback state. */
public record PlaybackState(PlaybackStatus status, String currentTrackKind, UUID currentTrackId) {

    public static PlaybackState idle() {
        return new PlaybackState(PlaybackStatus.IDLE, null, null);
    }
}
