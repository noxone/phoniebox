package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.mediafile.UpdatedAt;
import eu.noxone.phoniebox.shared.persistence.DomainAttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;

/** Converts {@link UpdatedAt} ↔ {@link String} (ISO-8601) for SQLite storage. */
@Converter(autoApply = true)
public class UpdatedAtConverter extends DomainAttributeConverter<UpdatedAt, String> {
  public UpdatedAtConverter() {
    super(s -> UpdatedAt.of(Instant.parse(s)), at -> at.getValue().toString());
  }
}
