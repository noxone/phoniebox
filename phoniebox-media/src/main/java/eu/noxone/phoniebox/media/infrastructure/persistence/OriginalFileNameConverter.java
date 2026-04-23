package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.OriginalFileName;
import eu.noxone.phoniebox.shared.persistence.ReflectiveDomainAttributeConverter;
import jakarta.persistence.Converter;

/** Converts {@link OriginalFileName} ↔ {@link String} for JPA persistence. */
@Converter(autoApply = true)
public class OriginalFileNameConverter extends ReflectiveDomainAttributeConverter<OriginalFileName, String> {}
