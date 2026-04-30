package eu.noxone.phoniebox.audio.web.rest;

import eu.noxone.phoniebox.audio.application.port.in.GetVolumeUseCase;
import eu.noxone.phoniebox.audio.application.port.in.SetVolumeUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST resource for audio output volume control.
 *
 * <pre>
 * GET /api/audio/volume  → current volume (0–100)
 * PUT /api/audio/volume  → set volume (0–100)
 * </pre>
 */
@Path("/api/audio/volume")
@Produces(MediaType.APPLICATION_JSON)
public class VolumeResource {

  private final GetVolumeUseCase getVolume;
  private final SetVolumeUseCase setVolume;

  @Inject
  public VolumeResource(final GetVolumeUseCase getVolume, final SetVolumeUseCase setVolume) {
    this.getVolume = getVolume;
    this.setVolume = setVolume;
  }

  @GET
  public VolumeResponse getVolume() {
    return new VolumeResponse(getVolume.getVolume());
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public VolumeResponse setVolume(final SetVolumeRequest request) {
    setVolume.setVolume(request.volume());
    return new VolumeResponse(getVolume.getVolume());
  }
}
