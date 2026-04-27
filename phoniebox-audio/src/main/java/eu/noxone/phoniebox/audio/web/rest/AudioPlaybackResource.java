package eu.noxone.phoniebox.audio.web.rest;

import eu.noxone.phoniebox.audio.application.port.in.PlaybackControlUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

/**
 * REST resource for audio playback control.
 *
 * <pre>
 * GET  /api/audio/playback           → current playback state
 * POST /api/audio/playback/track/{id} → select a track (no playback change)
 * POST /api/audio/playback/play       → start or resume the current track
 * POST /api/audio/playback/play/{id}  → select a track and start playing immediately
 * POST /api/audio/playback/pause      → pause the current track
 * </pre>
 *
 * <p>All mutation endpoints return the updated {@link PlaybackStateResponse}.
 */
@Path("/api/audio/playback")
@Produces(MediaType.APPLICATION_JSON)
public class AudioPlaybackResource {

    private final PlaybackControlUseCase control;

    @Inject
    public AudioPlaybackResource(final PlaybackControlUseCase control) {
        this.control = control;
    }

    @GET
    public PlaybackStateResponse getState() {
        return PlaybackStateResponse.from(control.getState());
    }

    @POST
    @Path("/track/{id}")
    public PlaybackStateResponse setTrack(@PathParam("id") final UUID id) {
        return PlaybackStateResponse.from(control.setTrack(id));
    }

    @POST
    @Path("/play")
    public PlaybackStateResponse play() {
        return PlaybackStateResponse.from(control.play());
    }

    @POST
    @Path("/play/{id}")
    public PlaybackStateResponse playTrack(@PathParam("id") final UUID id) {
        return PlaybackStateResponse.from(control.play(id));
    }

    @POST
    @Path("/pause")
    public PlaybackStateResponse pause() {
        return PlaybackStateResponse.from(control.pause());
    }
}
