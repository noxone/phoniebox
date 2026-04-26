package eu.noxone.phoniebox.shared.web;

import eu.noxone.phoniebox.shared.paging.PageResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a JAX-RS {@link Response} for a paged list endpoint.
 *
 * <p>The response body is the plain content array; all pagination metadata is carried in
 * standard HTTP headers so callers do not need to unwrap an envelope:
 *
 * <ul>
 *   <li>{@code X-Total-Count}  — total number of records across all pages
 *   <li>{@code X-Page}         — current 0-based page index
 *   <li>{@code X-Page-Size}    — effective page size (may be lower than requested due to clamping)
 *   <li>{@code X-Total-Pages}  — total number of pages
 *   <li>{@code Link}           — RFC 8288 navigation links: {@code first}, {@code last},
 *                                and conditionally {@code prev} / {@code next}
 * </ul>
 */
public final class PagingResponseBuilder {

    private PagingResponseBuilder() {}

    /**
     * Returns a 200 OK {@link Response.ResponseBuilder} populated with the content array and all
     * paging headers. Call {@link Response.ResponseBuilder#build()} to finalise the response.
     */
    public static <T> Response.ResponseBuilder of(PageResponse<T> page, UriInfo uriInfo) {
        Response.ResponseBuilder builder = Response.ok(page.content())
                .header("X-Total-Count", page.totalElements())
                .header("X-Page", page.page())
                .header("X-Page-Size", page.size())
                .header("X-Total-Pages", page.totalPages());

        String linkHeader = buildLinkHeader(page, uriInfo);
        if (!linkHeader.isEmpty()) {
            builder.header("Link", linkHeader);
        }

        return builder;
    }

    private static String buildLinkHeader(PageResponse<?> page, UriInfo uriInfo) {
        UriBuilder base = uriInfo.getAbsolutePathBuilder().replaceQueryParam("size", page.size());
        List<String> links = new ArrayList<>();

        links.add(linkEntry(base.clone().replaceQueryParam("page", 0), "first"));
        if (page.hasPrevious()) {
            links.add(linkEntry(base.clone().replaceQueryParam("page", page.page() - 1), "prev"));
        }
        if (page.hasNext()) {
            links.add(linkEntry(base.clone().replaceQueryParam("page", page.page() + 1), "next"));
        }
        links.add(linkEntry(base.clone().replaceQueryParam("page", Math.max(0, page.totalPages() - 1)), "last"));

        return String.join(", ", links);
    }

    private static String linkEntry(UriBuilder builder, String rel) {
        return "<" + builder.build() + ">; rel=\"" + rel + "\"";
    }
}
