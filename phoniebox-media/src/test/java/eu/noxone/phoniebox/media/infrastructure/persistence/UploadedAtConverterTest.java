package eu.noxone.phoniebox.media.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import eu.noxone.phoniebox.media.domain.model.mediafile.UploadedAt;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UploadedAtConverterTest {

  private static final Instant FIXED_INSTANT = Instant.parse("2024-06-15T10:30:00Z");

  private final UploadedAtConverter converter = new UploadedAtConverter();

  @Test
  void convertToDatabaseColumn_returnsIso8601String() {
    assertEquals(
        "2024-06-15T10:30:00Z", converter.convertToDatabaseColumn(UploadedAt.of(FIXED_INSTANT)));
  }

  @Test
  void convertToDatabaseColumn_nullReturnsNull() {
    assertNull(converter.convertToDatabaseColumn(null));
  }

  @Test
  void convertToEntityAttribute_parsesIso8601String() {
    assertEquals(
        UploadedAt.of(FIXED_INSTANT), converter.convertToEntityAttribute("2024-06-15T10:30:00Z"));
  }

  @Test
  void convertToEntityAttribute_nullReturnsNull() {
    assertNull(converter.convertToEntityAttribute(null));
  }

  @Test
  void roundTrip() {
    UploadedAt original = UploadedAt.of(FIXED_INSTANT);
    assertEquals(
        original, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original)));
  }
}
