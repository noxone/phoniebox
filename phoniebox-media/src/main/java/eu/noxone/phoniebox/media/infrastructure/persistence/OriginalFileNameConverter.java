package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.OriginalFileName;
import eu.noxone.phoniebox.shared.persistence.DomainAttributeConverter;
import jakarta.persistence.Converter;

/** Converts {@link OriginalFileName} ↔ {@link String} for JPA persistence. */
@Converter(autoApply = true)
public class OriginalFileNameConverter extends DomainAttributeConverter<OriginalFileName, String> {
    public OriginalFileNameConverter() { super(OriginalFileName::of, OriginalFileName::getValue); }
}
