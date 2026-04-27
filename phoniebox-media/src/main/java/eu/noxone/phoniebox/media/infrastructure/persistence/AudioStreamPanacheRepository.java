package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStreamId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AudioStreamPanacheRepository implements PanacheRepositoryBase<AudioStream, AudioStreamId> {
}
