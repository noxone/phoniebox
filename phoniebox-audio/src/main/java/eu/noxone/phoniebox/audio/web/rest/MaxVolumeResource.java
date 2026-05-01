package eu.noxone.phoniebox.audio.web.rest;

import eu.noxone.phoniebox.audio.application.port.in.GetMaxVolumeUseCase;
import eu.noxone.phoniebox.audio.application.port.in.SetMaxVolumeUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST resource for the maximum volume limit.
 *
 * <pre>
 * GET /api/audio/max-volume  → current limit (0–100)
 * PUT /api/audio/max-volume  → change limit; clamps active volume if needed
 * </pre>
 */
@Path("/api/audio/max-volume")
@Produces(MediaType.APPLICATION_JSON)
public class MaxVolumeResource {

  private final GetMaxVolumeUseCase getMaxVolume;
  private final SetMaxVolumeUseCase setMaxVolume;

  @Inject
  public MaxVolumeResource(
      final GetMaxVolumeUseCase getMaxVolume, final SetMaxVolumeUseCase setMaxVolume) {
    this.getMaxVolume = getMaxVolume;
    this.setMaxVolume = setMaxVolume;
  }

  @GET
  public MaxVolumeResponse getMaxVolume() {
    return new MaxVolumeResponse(getMaxVolume.getMaxVolume());
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public MaxVolumeResponse setMaxVolume(final SetMaxVolumeRequest request) {
    setMaxVolume.setMaxVolume(request.maxVolume());
    return new MaxVolumeResponse(getMaxVolume.getMaxVolume());
  }
}
