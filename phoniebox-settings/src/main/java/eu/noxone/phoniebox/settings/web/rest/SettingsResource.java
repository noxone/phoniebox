package eu.noxone.phoniebox.settings.web.rest;

import eu.noxone.phoniebox.settings.application.port.in.GetSettingUseCase;
import eu.noxone.phoniebox.settings.application.port.in.ListSettingsUseCase;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingCommand;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * REST resource for generic application settings.
 *
 * <pre>
 * GET /api/settings        → list all stored settings
 * GET /api/settings/{key}  → get one setting by key (404 if not set)
 * PUT /api/settings/{key}  → set or update a setting value
 * </pre>
 */
@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
public class SettingsResource {

  private final ListSettingsUseCase listUseCase;
  private final GetSettingUseCase getUseCase;
  private final SetSettingUseCase setUseCase;

  @Inject
  public SettingsResource(
      final ListSettingsUseCase listUseCase,
      final GetSettingUseCase getUseCase,
      final SetSettingUseCase setUseCase) {
    this.listUseCase = listUseCase;
    this.getUseCase = getUseCase;
    this.setUseCase = setUseCase;
  }

  @GET
  public List<SettingResponse> listAll() {
    return listUseCase.listSettings().stream().map(SettingResponse::from).toList();
  }

  @GET
  @Path("/{key}")
  public Response get(@PathParam("key") final String key) {
    return getUseCase
        .getSetting(key)
        .map(value -> Response.ok(new SettingResponse(key, value)).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @PUT
  @Path("/{key}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response set(@PathParam("key") final String key, final SetSettingRequest request) {
    setUseCase.setSetting(new SetSettingCommand(key, request.value()));
    String stored = getUseCase.getSetting(key).orElse(null);
    return Response.ok(new SettingResponse(key, stored)).build();
  }
}
