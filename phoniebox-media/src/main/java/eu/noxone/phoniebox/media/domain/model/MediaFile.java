package eu.noxone.phoniebox.media.domain.model;

import eu.noxone.phoniebox.shared.domain.DefaultDomainEntity;

import java.util.Objects;

/**
 * Aggregate root representing an uploaded sound file.
 *
 * <p>The physical bytes are stored separately (managed by {@code FileStoragePort}).
 * This entity holds only identity and descriptive metadata.
 *
 * <p>Extends {@link DefaultDomainEntity} to inherit identity-based equality
 * and {@code toString}.  All fields implement {@link eu.noxone.phoniebox.shared.domain.DomainAttribute}
 * as required by the domain model contract.
 *
 * <p>Two factory methods keep construction intent explicit:
 * <ul>
 *   <li>{@link #create} – mints a new identity and records the current time.
 *   <li>{@link #reconstitute} – rebuilds the aggregate from persisted state.
 * </ul>
 */
public final class MediaFile extends DefaultDomainEntity<MediaFileId> {

    private final MediaFileId id;
    private final MediaFileMetadata metadata;
    private final UploadedAt uploadedAt;

    private MediaFile(final MediaFileId id, final MediaFileMetadata metadata, final UploadedAt uploadedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
        this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt must not be null");
    }

    /** Creates a brand-new {@code MediaFile} with a fresh {@link MediaFileId} and the current timestamp. */
    public static MediaFile create(final MediaFileMetadata metadata) {
        return new MediaFile(MediaFileId.newId(), metadata, UploadedAt.now());
    }

    /** Rebuilds an existing {@code MediaFile} from persisted values. Never generates a new ID or timestamp. */
    public static MediaFile reconstitute(final MediaFileId id, final MediaFileMetadata metadata, final UploadedAt uploadedAt) {
        return new MediaFile(id, metadata, uploadedAt);
    }

    @Override
    public MediaFileId getId() {
        return id;
    }

    public MediaFileMetadata getMetadata() {
        return metadata;
    }

    public UploadedAt getUploadedAt() {
        return uploadedAt;
    }
}
