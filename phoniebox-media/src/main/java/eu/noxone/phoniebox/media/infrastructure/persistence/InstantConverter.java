package eu.noxone.phoniebox.media.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;

/**
 * Converts {@link Instant} ↔ {@link String} (ISO-8601) for SQLite storage.
 *
 * <p>SQLite has no native TIMESTAMP type; storing ISO-8601 strings is the
 * recommended approach because they sort and compare correctly.
 *
 * <p>{@code autoApply = true} means Hibernate automatically uses this
 * converter for every {@code Instant} field without explicit annotation.
 */
@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, String> {

    @Override
    public String convertToDatabaseColumn(final Instant attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public Instant convertToEntityAttribute(final String dbData) {
        return dbData == null ? null : Instant.parse(dbData);
    }
}
