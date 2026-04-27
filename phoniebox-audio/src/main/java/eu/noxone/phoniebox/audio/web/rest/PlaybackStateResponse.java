package eu.noxone.phoniebox.audio.web.rest;

import eu.noxone.phoniebox.audio.application.PlaybackState;

import java.util.UUID;

public record PlaybackStateResponse(String status, String currentTrackKind, UUID currentTrackId) {

    public static PlaybackStateResponse from(final PlaybackState state) {
        return new PlaybackStateResponse(state.status().name(), state.currentTrackKind(), state.currentTrackId());
    }
}
