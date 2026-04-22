package eu.noxone.phoniebox.media.domain.model;

import java.util.Objects;

/**
 * Immutable value object capturing file-level metadata supplied at upload time.
 *
 * <p>The domain does not validate MIME types against any external registry; that
 * responsibility belongs to the infrastructure layer or the web adapter.
 */
public final class MediaFileMetadata {

    private final String originalFileName;
    private final String mimeType;
    private final long sizeInBytes;

    public MediaFileMetadata(final String originalFileName, final String mimeType, final long sizeInBytes) {
        this.originalFileName = Objects.requireNonNull(originalFileName, "originalFileName must not be null");
        this.mimeType = Objects.requireNonNull(mimeType, "mimeType must not be null");
        if (sizeInBytes < 0) {
            throw new IllegalArgumentException("sizeInBytes must not be negative");
        }
        this.sizeInBytes = sizeInBytes;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSizeInBytes() {
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
        return sizeInBytes == that.sizeInBytes
                && Objects.equals(originalFileName, that.originalFileName)
                && Objects.equals(mimeType, that.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalFileName, mimeType, sizeInBytes);
    }

    @Override
    public String toString() {
        return "MediaFileMetadata{"
                + "originalFileName='" + originalFileName + '\''
                + ", mimeType='" + mimeType + '\''
                + ", sizeInBytes=" + sizeInBytes
                + '}';
    }
}
