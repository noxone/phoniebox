package eu.noxone.phoniebox.media.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root representing an uploaded sound file.
 *
 * <p>The physical bytes are stored separately (managed by {@code FileStoragePort}).
 * This entity holds only identity and descriptive metadata.
 *
 * <p>Two factory methods keep construction intent explicit:
 * <ul>
 *   <li>{@link #create} – mints a new identity and records the current time.
 *   <li>{@link #reconstitute} – rebuilds the aggregate from persisted state.
 * </ul>
 */
public final class MediaFile {

    private final MediaFileId id;
    private final MediaFileMetadata metadata;
    private final Instant uploadedAt;

    private MediaFile(final MediaFileId id, final MediaFileMetadata metadata, final Instant uploadedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
        this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt must not be null");
    }

    /** Creates a brand-new {@code MediaFile} with a fresh {@link MediaFileId} and the current timestamp. */
    public static MediaFile create(final MediaFileMetadata metadata) {
        return new MediaFile(MediaFileId.newId(), metadata, Instant.now());
    }

    /** Rebuilds an existing {@code MediaFile} from persisted values. Never generates a new ID or timestamp. */
    public static MediaFile reconstitute(final MediaFileId id, final MediaFileMetadata metadata, final Instant uploadedAt) {
        return new MediaFile(id, metadata, uploadedAt);
    }

    public MediaFileId getId() {
        return id;
    }

    public MediaFileMetadata getMetadata() {
        return metadata;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
