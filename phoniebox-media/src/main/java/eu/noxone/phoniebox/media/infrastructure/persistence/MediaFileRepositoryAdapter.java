package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.application.port.out.MediaFileRepository;
import eu.noxone.phoniebox.media.domain.model.FileSize;
import eu.noxone.phoniebox.media.domain.model.MediaFile;
import eu.noxone.phoniebox.media.domain.model.MediaFileId;
import eu.noxone.phoniebox.media.domain.model.MediaFileMetadata;
import eu.noxone.phoniebox.media.domain.model.MimeType;
import eu.noxone.phoniebox.media.domain.model.OriginalFileName;
import eu.noxone.phoniebox.media.domain.model.UploadedAt;
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
        entity.originalFileName = domain.getMetadata().getOriginalFileName().getValue();
        entity.mimeType = domain.getMetadata().getMimeType().getValue();
        entity.sizeInBytes = domain.getMetadata().getSizeInBytes().getValue();
        entity.uploadedAt = domain.getUploadedAt().getValue();
        return entity;
    }

    private MediaFile toDomain(final MediaFileEntity entity) {
        return MediaFile.reconstitute(
                MediaFileId.of(entity.id),
                new MediaFileMetadata(
                        OriginalFileName.of(entity.originalFileName),
                        MimeType.of(entity.mimeType),
                        FileSize.of(entity.sizeInBytes)),
                UploadedAt.of(entity.uploadedAt));
    }
}
