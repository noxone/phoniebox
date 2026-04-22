package eu.noxone.phoniebox.media.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Panache ORM entity mapped to the {@code media_files} table.
 *
 * <p>Uses public fields (idiomatic Panache style). The ID is a UUID stored as
 * a TEXT string because SQLite has no native UUID column type.
 *
 * <p>This class is purely an infrastructure concern and must never leak into
 * the domain or application layers.
 */
@Entity
@Table(name = "media_files")
public class MediaFileEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public String id;

    @Column(name = "original_file_name", nullable = false)
    public String originalFileName;

    @Column(name = "mime_type", nullable = false)
    public String mimeType;

    @Column(name = "size_in_bytes", nullable = false)
    public long sizeInBytes;

    /** Stored as ISO-8601 text via {@link InstantConverter}. */
    @Column(name = "uploaded_at", nullable = false)
    public Instant uploadedAt;
}
