package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.mediafile.UploadedAt;
import eu.noxone.phoniebox.shared.persistence.DomainAttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;

/**
 * Converts {@link UploadedAt} ↔ {@link String} (ISO-8601) for SQLite storage.
 *
 * <p>SQLite has no native TIMESTAMP type; ISO-8601 strings sort and compare correctly. The column
 * value is an ISO-8601 string, so the factory parses it via {@link Instant#parse} before wrapping
 * it in {@link UploadedAt}.
 */
@Converter(autoApply = true)
public class UploadedAtConverter extends DomainAttributeConverter<UploadedAt, String> {
  public UploadedAtConverter() {
    super(s -> UploadedAt.of(Instant.parse(s)), at -> at.getValue().toString());
  }
}
