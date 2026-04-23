package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.MimeType;
import eu.noxone.phoniebox.shared.persistence.DomainAttributeConverter;
import jakarta.persistence.Converter;

/** Converts {@link MimeType} ↔ {@link String} for JPA persistence. */
@Converter(autoApply = true)
public class MimeTypeConverter extends DomainAttributeConverter<MimeType, String> {
    public MimeTypeConverter() { super(MimeType::of, MimeType::getValue); }
}
