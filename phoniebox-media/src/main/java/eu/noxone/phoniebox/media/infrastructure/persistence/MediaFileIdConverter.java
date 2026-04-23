package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.MediaFileId;
import eu.noxone.phoniebox.shared.persistence.DomainAttributeConverter;
import jakarta.persistence.Converter;

/** Converts {@link MediaFileId} ↔ {@link String} (UUID text) for JPA persistence. */
@Converter(autoApply = true)
public class MediaFileIdConverter extends DomainAttributeConverter<MediaFileId, String> {
    public MediaFileIdConverter() { super(MediaFileId::of, MediaFileId::asString); }
}
