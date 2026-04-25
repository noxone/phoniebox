package eu.noxone.phoniebox.media.web.rest;

import eu.noxone.phoniebox.media.application.port.in.DeleteMediaFileUseCase;
import eu.noxone.phoniebox.media.application.port.in.GetMediaFileUseCase;
import eu.noxone.phoniebox.media.application.port.in.ListMediaFilesUseCase;
import eu.noxone.phoniebox.media.application.port.in.UploadMediaFileCommand;
import eu.noxone.phoniebox.media.application.port.in.UploadMediaFileUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

/**
 * REST resource for the media-file management API.
 *
 * <pre>
 * GET    /api/media          → list all files
 * GET    /api/media/{id}     → get one file by UUID
 * POST   /api/media          → upload a new file (multipart/form-data)
 * DELETE /api/media/{id}     → delete a file
 * </pre>
 *
 * <p>The resource only speaks to the application-layer input ports.
 * It has no knowledge of persistence or the file system.
 */
@Path("/api/media")
@Produces(MediaType.APPLICATION_JSON)
public class MediaFileResource {

    private final UploadMediaFileUseCase uploadUseCase;
    private final GetMediaFileUseCase getUseCase;
    private final ListMediaFilesUseCase listUseCase;
    private final DeleteMediaFileUseCase deleteUseCase;

    @Inject
    public MediaFileResource(
            final UploadMediaFileUseCase uploadUseCase,
            final GetMediaFileUseCase getUseCase,
            final ListMediaFilesUseCase listUseCase,
            final DeleteMediaFileUseCase deleteUseCase) {
        this.uploadUseCase = uploadUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @GET
    public List<MediaFileResponse> listAll() {
        return listUseCase.listAll().stream()
                .map(MediaFileResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") final UUID id) {
        return getUseCase.findById(id)
                .map(MediaFileResponse::from)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(
            @RestForm("file") final FileUpload file) {
        try(var content = new BufferedInputStream(Files.newInputStream(file.filePath()))) {
            final var command = new UploadMediaFileCommand(
                    file.fileName(),
                    resolveMimeType(file),
                    Files.size(file.filePath()),
                    content
            );
            final var uploaded = uploadUseCase.upload(command);
            return Response.status(Response.Status.CREATED)
                    .entity(MediaFileResponse.from(uploaded))
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") final UUID id) {
        final boolean deleted = deleteUseCase.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    private String resolveMimeType(final FileUpload file) throws IOException {
        final String probed = Files.probeContentType(file.filePath());
        return probed != null ? probed : MediaType.APPLICATION_OCTET_STREAM;
    }
}
