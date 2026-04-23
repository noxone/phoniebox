package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.FileSize;
import eu.noxone.phoniebox.shared.persistence.DomainAttributeConverter;
import jakarta.persistence.Converter;

/** Converts {@link FileSize} ↔ {@link Long} for JPA persistence. */
@Converter(autoApply = true)
public class FileSizeConverter extends DomainAttributeConverter<FileSize, Long> {
    public FileSizeConverter() { super(FileSize::of, FileSize::getValue); }
}
