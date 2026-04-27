package eu.noxone.phoniebox.media.web.rest;

import eu.noxone.phoniebox.media.application.service.MediaFileApplicationService;
import eu.noxone.phoniebox.media.domain.model.mediafile.FileSize;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFile;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileMetadata;
import eu.noxone.phoniebox.media.domain.model.shared.MimeType;
import eu.noxone.phoniebox.media.domain.model.mediafile.OriginalFileName;
import eu.noxone.phoniebox.shared.paging.PageResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies the HTTP headers produced by {@link eu.noxone.phoniebox.shared.web.PagingResponseBuilder}
 * through a live JAX-RS endpoint. Quarkus is required here because {@code Response.ok()} needs a
 * registered {@code RuntimeDelegate}.
 */
@QuarkusTest
class PagingResponseBuilderTest {

    @InjectMock
    MediaFileApplicationService service;

    // ── X-* count headers ────────────────────────────────────────────────────

    @Test
    void headers_reflectTotalCountAndCurrentPage() {
        when(service.list(any())).thenReturn(pagedResponse(0, 10, 42));

        given()
                .when().get("/api/media?page=0&size=10")
                .then()
                .statusCode(200)
                .header("X-Total-Count", "42")
                .header("X-Page", "0")
                .header("X-Page-Size", "10")
                .header("X-Total-Pages", "5");  // ceil(42 / 10) = 5
    }

    @Test
    void headers_reflectMidPageState() {
        when(service.list(any())).thenReturn(pagedResponse(2, 10, 42));

        given()
                .when().get("/api/media?page=2&size=10")
                .then()
                .header("X-Page", "2")
                .header("X-Total-Pages", "5");
    }

    // ── Link header — always-present relations ────────────────────────────────

    @Test
    void linkHeader_alwaysContainsFirstAndLast() {
        when(service.list(any())).thenReturn(pagedResponse(1, 10, 30));

        given()
                .when().get("/api/media?page=1&size=10")
                .then()
                .header("Link", containsString("rel=\"first\""))
                .header("Link", containsString("rel=\"last\""));
    }

    // ── Link header — conditional next / prev ─────────────────────────────────

    @Test
    void linkHeader_containsNextOnly_whenOnFirstOfMultiplePages() {
        when(service.list(any())).thenReturn(pagedResponse(0, 10, 30));

        given()
                .when().get("/api/media?page=0&size=10")
                .then()
                .header("Link", containsString("rel=\"next\""))
                .header("Link", not(containsString("rel=\"prev\"")));
    }

    @Test
    void linkHeader_containsBothNextAndPrev_whenOnMiddlePage() {
        when(service.list(any())).thenReturn(pagedResponse(1, 10, 30));

        given()
                .when().get("/api/media?page=1&size=10")
                .then()
                .header("Link", containsString("rel=\"next\""))
                .header("Link", containsString("rel=\"prev\""));
    }

    @Test
    void linkHeader_containsPrevOnly_whenOnLastPage() {
        when(service.list(any())).thenReturn(pagedResponse(2, 10, 30));

        given()
                .when().get("/api/media?page=2&size=10")
                .then()
                .header("Link", not(containsString("rel=\"next\"")))
                .header("Link", containsString("rel=\"prev\""));
    }

    @Test
    void linkHeader_omitsNextAndPrev_whenSinglePage() {
        when(service.list(any())).thenReturn(pagedResponse(0, 10, 5));

        given()
                .when().get("/api/media?page=0&size=10")
                .then()
                .header("Link", not(containsString("rel=\"next\"")))
                .header("Link", not(containsString("rel=\"prev\"")));
    }

    // ── Link header — correct page numbers ───────────────────────────────────

    @Test
    void nextLink_referencesIncrementedPageNumber() {
        when(service.list(any())).thenReturn(pagedResponse(1, 10, 30));

        given()
                .when().get("/api/media?page=1&size=10")
                .then()
                .header("Link", containsString("page=2"));
    }

    @Test
    void prevLink_referencesDecrementedPageNumber() {
        when(service.list(any())).thenReturn(pagedResponse(2, 10, 30));

        given()
                .when().get("/api/media?page=2&size=10")
                .then()
                .header("Link", containsString("page=1"));
    }

    @Test
    void firstLink_alwaysReferencesPageZero() {
        when(service.list(any())).thenReturn(pagedResponse(2, 10, 30));

        given()
                .when().get("/api/media?page=2&size=10")
                .then()
                .header("Link", containsString("page=0"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static PageResponse<MediaFile> pagedResponse(final int page, final int size, final long total) {
        return new PageResponse<>(List.of(sampleFile()), page, size, total);
    }

    private static MediaFile sampleFile() {
        return MediaFile.create(new MediaFileMetadata(
                OriginalFileName.of("track.mp3"),
                MimeType.of("audio/mpeg"),
                FileSize.of(1024L)
        ));
    }
}
