package eu.noxone.phoniebox.media.application.service;

import eu.noxone.phoniebox.media.application.port.in.DeleteMediaFileUseCase;
import eu.noxone.phoniebox.media.application.port.in.GetMediaFileUseCase;
import eu.noxone.phoniebox.media.application.port.in.ListMediaFilesUseCase;
import eu.noxone.phoniebox.media.application.port.in.UploadMediaFileCommand;
import eu.noxone.phoniebox.media.application.port.in.UploadMediaFileUseCase;
import eu.noxone.phoniebox.media.application.port.out.FileStoragePort;
import eu.noxone.phoniebox.media.application.port.out.MediaFileRepository;
import eu.noxone.phoniebox.media.domain.model.FileSize;
import eu.noxone.phoniebox.media.domain.model.MediaFile;
import eu.noxone.phoniebox.media.domain.model.MediaFileId;
import eu.noxone.phoniebox.media.domain.model.MediaFileMetadata;
import eu.noxone.phoniebox.media.domain.model.MimeType;
import eu.noxone.phoniebox.media.domain.model.OriginalFileName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements all media-file use cases.
 *
 * <p>Orchestrates the {@link MediaFileRepository} (persistence) and the
 * {@link FileStoragePort} (binary storage) without knowing anything about
 * HTTP, SQL, or the file system.
 */
@ApplicationScoped
public class MediaFileApplicationService
        implements UploadMediaFileUseCase, GetMediaFileUseCase, ListMediaFilesUseCase, DeleteMediaFileUseCase {

    private final MediaFileRepository repository;
    private final FileStoragePort storage;

    @Inject
    public MediaFileApplicationService(final MediaFileRepository repository, final FileStoragePort storage) {
        this.repository = repository;
        this.storage = storage;
    }

    @Override
    @Transactional
    public MediaFile upload(final UploadMediaFileCommand command) {
        final var metadata = new MediaFileMetadata(
                OriginalFileName.of(command.originalFileName()),
                MimeType.of(command.mimeType()),
                FileSize.of(command.sizeInBytes())
        );
        final var mediaFile = MediaFile.create(metadata);
        // Store bytes first – if this fails the transaction rolls back and no orphan row is created
        storage.store(mediaFile.getId(), command.content());
        repository.save(mediaFile);
        return mediaFile;
    }

    @Override
    public Optional<MediaFile> findById(final UUID id) {
        return repository.findById(MediaFileId.of(id));
    }

    @Override
    public List<MediaFile> listAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public boolean delete(final UUID id) {
        final var mediaFileId = MediaFileId.of(id);
        final boolean deletedFromDb = repository.deleteById(mediaFileId);
        if (deletedFromDb) {
            storage.delete(mediaFileId);
        }
        return deletedFromDb;
    }
}
