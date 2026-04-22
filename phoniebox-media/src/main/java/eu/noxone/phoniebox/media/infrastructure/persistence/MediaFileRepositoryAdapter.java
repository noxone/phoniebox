package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.application.port.out.MediaFileRepository;
import eu.noxone.phoniebox.media.domain.model.MediaFile;
import eu.noxone.phoniebox.media.domain.model.MediaFileId;
import eu.noxone.phoniebox.media.domain.model.MediaFileMetadata;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that bridges {@link MediaFileRepository} (application port) to
 * Panache / Hibernate ORM (infrastructure).
 *
 * <p>All mapping between the domain model and the persistence entity is kept
 * here; neither the domain nor the application service knows about JPA.
 */
@ApplicationScoped
public class MediaFileRepositoryAdapter implements MediaFileRepository {

    @Override
    public void save(final MediaFile mediaFile) {
        toEntity(mediaFile).persist();
    }

    @Override
    public Optional<MediaFile> findById(final MediaFileId id) {
        return MediaFileEntity.<MediaFileEntity>findByIdOptional(id.asString())
                .map(this::toDomain);
    }

    @Override
    public List<MediaFile> findAll() {
        return MediaFileEntity.<MediaFileEntity>listAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean deleteById(final MediaFileId id) {
        return MediaFileEntity.deleteById(id.asString());
    }

    // ── Mapping ─────────────────────────────────────────────────────────────

    private MediaFileEntity toEntity(final MediaFile domain) {
        final var entity = new MediaFileEntity();
        entity.id = domain.getId().asString();
        entity.originalFileName = domain.getMetadata().getOriginalFileName();
        entity.mimeType = domain.getMetadata().getMimeType();
        entity.sizeInBytes = domain.getMetadata().getSizeInBytes();
        entity.uploadedAt = domain.getUploadedAt();
        return entity;
    }

    private MediaFile toDomain(final MediaFileEntity entity) {
        return MediaFile.reconstitute(
                MediaFileId.of(entity.id),
                new MediaFileMetadata(entity.originalFileName, entity.mimeType, entity.sizeInBytes),
                entity.uploadedAt
        );
    }
}
