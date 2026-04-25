package eu.noxone.phoniebox.app;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;

/**
 * Catch-all JAX-RS resource that serves index.html for any path not handled by the API.
 * Enables Vue Router's HTML5 history mode to work on full-page reloads.
 * More-specific routes (e.g. /api/**) take precedence due to JAX-RS matching rules.
 */
@Path("/")
public class SpaFallbackResource {

    @GET
    @Path("{path:.*}")
    @Produces(MediaType.TEXT_HTML)
    public Response spa() {
        final InputStream html = getClass().getClassLoader()
                .getResourceAsStream("META-INF/resources/index.html");
        if (html == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("index.html not found — run 'npm run build' first")
                    .build();
        }
        return Response.ok(html).build();
    }
}
