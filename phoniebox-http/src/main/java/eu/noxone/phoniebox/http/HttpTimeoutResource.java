package eu.noxone.phoniebox.http;

import eu.noxone.phoniebox.settings.application.port.in.GetSettingUseCase;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingCommand;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST resource for HTTP client timeout configuration.
 *
 * <pre>
 * GET /api/http/timeouts → current timeout values (seconds)
 * PUT /api/http/timeouts → update timeout values
 * </pre>
 */
@Path("/api/http/timeouts")
@Produces(MediaType.APPLICATION_JSON)
public class HttpTimeoutResource {

  private final GetSettingUseCase getSetting;
  private final SetSettingUseCase setSetting;

  @Inject
  public HttpTimeoutResource(
      final GetSettingUseCase getSetting, final SetSettingUseCase setSetting) {
    this.getSetting = getSetting;
    this.setSetting = setSetting;
  }

  @GET
  public HttpTimeoutsResponse get() {
    return new HttpTimeoutsResponse(
        readLong(HttpSettingKeys.HTTP_CONNECT_TIMEOUT, HttpClientProvider.DEFAULT_CONNECT_TIMEOUT),
        readLong(HttpSettingKeys.HTTP_READ_TIMEOUT, HttpClientProvider.DEFAULT_READ_TIMEOUT),
        readLong(HttpSettingKeys.HTTP_WRITE_TIMEOUT, HttpClientProvider.DEFAULT_WRITE_TIMEOUT));
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public HttpTimeoutsResponse set(final SetHttpTimeoutsRequest request) {
    setSetting.setSetting(
        new SetSettingCommand(
            HttpSettingKeys.HTTP_CONNECT_TIMEOUT, String.valueOf(request.connectTimeoutSeconds())));
    setSetting.setSetting(
        new SetSettingCommand(
            HttpSettingKeys.HTTP_READ_TIMEOUT, String.valueOf(request.readTimeoutSeconds())));
    setSetting.setSetting(
        new SetSettingCommand(
            HttpSettingKeys.HTTP_WRITE_TIMEOUT, String.valueOf(request.writeTimeoutSeconds())));
    return get();
  }

  private long readLong(final String key, final long defaultValue) {
    return getSetting
        .getSetting(key)
        .map(
            v -> {
              try {
                return Long.parseLong(v);
              } catch (NumberFormatException e) {
                return defaultValue;
              }
            })
        .orElse(defaultValue);
  }
}
