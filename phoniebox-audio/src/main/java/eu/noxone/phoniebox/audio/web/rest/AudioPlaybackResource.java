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
 * GET  /api/audio/playback                    → current playback state
 * POST /api/audio/playback/track/{kind}/{id}  → select a source (no playback change)
 * POST /api/audio/playback/play               → start or resume the current source
 * POST /api/audio/playback/play/{kind}/{id}   → select a source and start playing immediately
 * POST /api/audio/playback/pause              → pause the current source
 * POST /api/audio/playback/stop               → stop and clear the current source
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
    @Path("/track/{kind}/{id}")
    public PlaybackStateResponse setTrack(@PathParam("kind") final String kind, @PathParam("id") final UUID id) {
        return PlaybackStateResponse.from(control.setTrack(kind, id));
    }

    @POST
    @Path("/play")
    public PlaybackStateResponse play() {
        return PlaybackStateResponse.from(control.play());
    }

    @POST
    @Path("/play/{kind}/{id}")
    public PlaybackStateResponse playTrack(@PathParam("kind") final String kind, @PathParam("id") final UUID id) {
        return PlaybackStateResponse.from(control.play(kind, id));
    }

    @POST
    @Path("/pause")
    public PlaybackStateResponse pause() {
        return PlaybackStateResponse.from(control.pause());
    }

    @POST
    @Path("/stop")
    public PlaybackStateResponse stop() {
        return PlaybackStateResponse.from(control.stop());
    }
}
