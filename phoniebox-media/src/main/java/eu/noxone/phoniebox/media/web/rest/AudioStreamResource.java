package eu.noxone.phoniebox.media.web.rest;

import eu.noxone.phoniebox.media.application.port.in.AddAudioStreamCommand;
import eu.noxone.phoniebox.media.application.port.in.AddAudioStreamUseCase;
import eu.noxone.phoniebox.media.application.port.in.DeleteAudioStreamUseCase;
import eu.noxone.phoniebox.media.application.port.in.GetAudioStreamUseCase;
import eu.noxone.phoniebox.media.application.port.in.ListAudioStreamsUseCase;
import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.web.PagingResponseBuilder;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.UUID;

/**
 * REST resource for internet radio stream management.
 *
 * <pre>
 * GET    /api/streams        → list streams (paged)
 * GET    /api/streams/{id}   → get one stream by UUID
 * POST   /api/streams        → add a new stream
 * DELETE /api/streams/{id}   → delete a stream
 * </pre>
 */
@Path("/api/streams")
@Produces(MediaType.APPLICATION_JSON)
public class AudioStreamResource {

    private final AddAudioStreamUseCase addUseCase;
    private final ListAudioStreamsUseCase listUseCase;
    private final GetAudioStreamUseCase getUseCase;
    private final DeleteAudioStreamUseCase deleteUseCase;

    @Inject
    public AudioStreamResource(
            final AddAudioStreamUseCase addUseCase,
            final ListAudioStreamsUseCase listUseCase,
            final GetAudioStreamUseCase getUseCase,
            final DeleteAudioStreamUseCase deleteUseCase) {
        this.addUseCase = addUseCase;
        this.listUseCase = listUseCase;
        this.getUseCase = getUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @GET
    public Response listAll(
            @QueryParam("page") @DefaultValue("0") final int page,
            @QueryParam("size") @DefaultValue("1000") final int size,
            @Context final UriInfo uriInfo) {
        var paged = listUseCase.list(PageRequest.of(page, size)).map(AudioStreamResponse::from);
        return PagingResponseBuilder.of(paged, uriInfo).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") final UUID id) {
        return getUseCase.get(id)
                .map(AudioStreamResponse::from)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(final AddAudioStreamRequest request) {
        var command = new AddAudioStreamCommand(request.name(), request.url(), request.mimeType());
        var stream = addUseCase.add(command);
        return Response.status(Response.Status.CREATED)
                .entity(AudioStreamResponse.from(stream))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") final UUID id) {
        return deleteUseCase.delete(id)
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
