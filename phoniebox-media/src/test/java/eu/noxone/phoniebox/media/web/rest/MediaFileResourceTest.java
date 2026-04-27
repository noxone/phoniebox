package eu.noxone.phoniebox.media.web.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import eu.noxone.phoniebox.media.application.service.MediaFileApplicationService;
import eu.noxone.phoniebox.media.domain.model.mediafile.FileSize;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFile;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileMetadata;
import eu.noxone.phoniebox.media.domain.model.mediafile.OriginalFileName;
import eu.noxone.phoniebox.media.domain.model.shared.MimeType;
import eu.noxone.phoniebox.shared.paging.PageResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * REST layer tests for {@link MediaFileResource}.
 *
 * <p>Boots Quarkus with an in-memory H2 datasource (configured in {@code
 * src/test/resources/application.properties}). All application-layer calls are intercepted via
 * {@link InjectMock} on {@link MediaFileApplicationService}, so no real persistence or file I/O
 * happens during these tests.
 */
@QuarkusTest
class MediaFileResourceTest {

  private static final UUID FIXED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @InjectMock MediaFileApplicationService service;

  // ── Helpers ───────────────────────────────────────────────────────────────

  private static MediaFile sampleFile() {
    return MediaFile.create(
        new MediaFileMetadata(
            OriginalFileName.of("song.mp3"), MimeType.of("audio/mpeg"), FileSize.of(2048L)));
  }

  // ── GET /api/media ────────────────────────────────────────────────────────

  @Test
  void listAll_returns200_withEmptyArray_whenNoFiles() {
    when(service.list(any())).thenReturn(new PageResponse<>(List.of(), 0, 1000, 0));

    given()
        .when()
        .get("/api/media")
        .then()
        .statusCode(200)
        .header("X-Total-Count", "0")
        .header("X-Page", "0")
        .header("X-Page-Size", "1000")
        .header("X-Total-Pages", "0")
        .body("$", empty());
  }

  @Test
  void listAll_returns200_withAllFiles() {
    final var files = List.of(sampleFile(), sampleFile());
    when(service.list(any())).thenReturn(new PageResponse<>(files, 0, 1000, 2));

    given()
        .when()
        .get("/api/media")
        .then()
        .statusCode(200)
        .header("X-Total-Count", "2")
        .header("X-Page", "0")
        .header("X-Page-Size", "1000")
        .header("X-Total-Pages", "1")
        .body("$", hasSize(2))
        .body("[0].originalFileName", equalTo("song.mp3"))
        .body("[0].mimeType", equalTo("audio/mpeg"))
        .body("[0].sizeInBytes", equalTo(2048));
  }

  @Test
  void listAll_includesNextLink_whenMorePagesExist() {
    final var files = List.of(sampleFile());
    when(service.list(any())).thenReturn(new PageResponse<>(files, 0, 1, 3));

    given()
        .when()
        .get("/api/media?page=0&size=1")
        .then()
        .statusCode(200)
        .header("X-Total-Count", "3")
        .header("X-Total-Pages", "3")
        .header("Link", org.hamcrest.Matchers.containsString("rel=\"next\""));
  }

  // ── GET /api/media/{id} ───────────────────────────────────────────────────

  @Test
  void findById_returns200_withFile_whenFound() {
    MediaFile file = sampleFile();
    when(service.findById(file.getId().getValue())).thenReturn(Optional.of(file));

    given()
        .when()
        .get("/api/media/" + file.getId().getValue())
        .then()
        .statusCode(200)
        .body("originalFileName", equalTo("song.mp3"))
        .body("mimeType", equalTo("audio/mpeg"))
        .body("sizeInBytes", equalTo(2048));
  }

  @Test
  void findById_returns404_whenNotFound() {
    when(service.findById(FIXED_UUID)).thenReturn(Optional.empty());

    given().when().get("/api/media/" + FIXED_UUID).then().statusCode(404);
  }

  // ── POST /api/media ───────────────────────────────────────────────────────

  @Test
  void upload_returns201_withCreatedFile() {
    MediaFile file = sampleFile();
    when(service.upload(any())).thenReturn(file);

    given()
        .multiPart("file", "song.mp3", "fake audio content".getBytes(), "audio/mpeg")
        .when()
        .post("/api/media")
        .then()
        .statusCode(201)
        .body("originalFileName", equalTo("song.mp3"))
        .body("mimeType", equalTo("audio/mpeg"))
        .body("sizeInBytes", equalTo(2048));
  }

  // ── DELETE /api/media/{id} ────────────────────────────────────────────────

  @Test
  void delete_returns204_whenFileExists() {
    when(service.delete(FIXED_UUID)).thenReturn(true);

    given().when().delete("/api/media/" + FIXED_UUID).then().statusCode(204);
  }

  @Test
  void delete_returns404_whenFileNotFound() {
    when(service.delete(FIXED_UUID)).thenReturn(false);

    given().when().delete("/api/media/" + FIXED_UUID).then().statusCode(404);
  }
}
