package eu.noxone.phoniebox.media.domain.model;

import eu.noxone.phoniebox.shared.domain.DomainAttribute;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Compound value object capturing file-level metadata supplied at upload time.
 *
 * <p>Implements {@link DomainAttribute} so it can be used as a field on
 * {@link MediaFile} without violating the entity-field rule.  Each sub-value
 * is itself a typed {@link DomainAttribute} whose persistence conversion is
 * handled by an {@code @AttributeConverter(autoApply = true)} in the
 * infrastructure layer.
 *
 * <p>Marked {@code @Embeddable} so JPA maps its three columns directly into
 * the {@code media_files} table alongside the entity's own columns.
 * Fields are non-final and a no-arg constructor is provided for JPA.
 */
@Embeddable
public class MediaFileMetadata implements DomainAttribute {

    @Column(name = "original_file_name", nullable = false)
    private OriginalFileName originalFileName;

    @Column(name = "mime_type", nullable = false)
    private MimeType mimeType;

    @Column(name = "size_in_bytes", nullable = false)
    private FileSize sizeInBytes;

    /** Required by JPA. Not for use by application code. */
    protected MediaFileMetadata() {
    }

    public MediaFileMetadata(
            final OriginalFileName originalFileName,
            final MimeType mimeType,
            final FileSize sizeInBytes) {
        this.originalFileName = Objects.requireNonNull(originalFileName, "originalFileName must not be null");
        this.mimeType = Objects.requireNonNull(mimeType, "mimeType must not be null");
        this.sizeInBytes = Objects.requireNonNull(sizeInBytes, "sizeInBytes must not be null");
    }

    public OriginalFileName getOriginalFileName() {
        return originalFileName;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public FileSize getSizeInBytes() {
        return sizeInBytes;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MediaFileMetadata that)) {
            return false;
        }
        return Objects.equals(originalFileName, that.originalFileName)
                && Objects.equals(mimeType, that.mimeType)
                && Objects.equals(sizeInBytes, that.sizeInBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalFileName, mimeType, sizeInBytes);
    }

    @Override
    public String toString() {
        return "MediaFileMetadata{"
                + "originalFileName=" + originalFileName
                + ", mimeType=" + mimeType
                + ", sizeInBytes=" + sizeInBytes
                + '}';
    }
}
