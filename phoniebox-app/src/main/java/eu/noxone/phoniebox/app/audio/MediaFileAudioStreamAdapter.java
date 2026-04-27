package eu.noxone.phoniebox.app.audio;

import eu.noxone.phoniebox.audio.application.port.out.AudioStreamPort;
import eu.noxone.phoniebox.media.application.port.out.FileStoragePort;
import eu.noxone.phoniebox.media.domain.model.MediaFileId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Wires the audio module's {@link AudioStreamPort} to the media module's {@link FileStoragePort}.
 *
 * <p>Lives in the assembly module ({@code phoniebox-app}) so that neither feature module
 * needs to know about the other: the audio module only sees {@link AudioStreamPort},
 * the media module only sees {@link FileStoragePort}.
 */
@ApplicationScoped
public class MediaFileAudioStreamAdapter implements AudioStreamPort {

    private final FileStoragePort storage;

    @Inject
    public MediaFileAudioStreamAdapter(final FileStoragePort storage) {
        this.storage = storage;
    }

    @Override
    public InputStream openStream(final UUID playableId) throws IOException {
        return Files.newInputStream(storage.resolve(MediaFileId.of(playableId)));
    }
}
