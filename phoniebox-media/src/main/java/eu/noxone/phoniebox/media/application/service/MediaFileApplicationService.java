package eu.noxone.phoniebox.media.application.service;

import eu.noxone.phoniebox.media.application.port.in.DeleteMediaFileUseCase;
import eu.noxone.phoniebox.media.application.port.in.GetMediaFileUseCase;
import eu.noxone.phoniebox.media.application.port.in.ListMediaFilesUseCase;
import eu.noxone.phoniebox.media.application.port.in.UpdateTagsCommand;
import eu.noxone.phoniebox.media.application.port.in.UpdateTagsUseCase;
import eu.noxone.phoniebox.media.application.port.in.UploadMediaFileCommand;
import eu.noxone.phoniebox.media.application.port.in.UploadMediaFileUseCase;
import eu.noxone.phoniebox.media.application.port.out.AudioMetadataExtractor;
import eu.noxone.phoniebox.media.application.port.out.FileStoragePort;
import eu.noxone.phoniebox.media.application.port.out.MediaFileRepository;
import eu.noxone.phoniebox.media.domain.model.mediafile.FileSize;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFile;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileMetadata;
import eu.noxone.phoniebox.media.domain.model.shared.MimeType;
import eu.noxone.phoniebox.media.domain.model.mediafile.OriginalFileName;
import eu.noxone.phoniebox.media.domain.model.audio.TrackAlbum;
import eu.noxone.phoniebox.media.domain.model.audio.TrackArtist;
import eu.noxone.phoniebox.media.domain.model.audio.TrackGenre;
import eu.noxone.phoniebox.media.domain.model.audio.TrackTitle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.paging.PageResponse;

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
        implements UploadMediaFileUseCase, GetMediaFileUseCase, ListMediaFilesUseCase, DeleteMediaFileUseCase, UpdateTagsUseCase {

    private final MediaFileRepository repository;
    private final FileStoragePort storage;
    private final AudioMetadataExtractor audioMetadataExtractor;

    @Inject
    public MediaFileApplicationService(final MediaFileRepository repository,
                                       final FileStoragePort storage,
                                       final AudioMetadataExtractor audioMetadataExtractor) {
        this.repository = repository;
        this.storage = storage;
        this.audioMetadataExtractor = audioMetadataExtractor;
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
        audioMetadataExtractor.extract(storage.resolve(mediaFile.getId()))
                .ifPresent(mediaFile::applyAudioMetadata);
        repository.save(mediaFile);
        return mediaFile;
    }

    @Override
    public Optional<MediaFile> findById(final UUID id) {
        return repository.findById(MediaFileId.of(id));
    }

    @Override
    public PageResponse<MediaFile> list(final PageRequest pageRequest) {
        return repository.findAll(pageRequest);
    }

    @Override
    @Transactional
    public Optional<MediaFile> updateTags(final UpdateTagsCommand command) {
        return repository.findById(MediaFileId.of(command.id()))
                .map(file -> {
                    file.updateTagMetadata(
                            command.title()  != null ? TrackTitle.of(command.title())   : null,
                            command.artist() != null ? TrackArtist.of(command.artist()) : null,
                            command.album()  != null ? TrackAlbum.of(command.album())   : null,
                            command.genre()  != null ? TrackGenre.of(command.genre())   : null
                    );
                    return file;
                });
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
