package eu.noxone.phoniebox.media.application.port.out;

import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStreamId;
import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.paging.PageResponse;
import java.util.Optional;

public interface AudioStreamRepository {

  void save(AudioStream stream);

  Optional<AudioStream> findById(AudioStreamId id);

  PageResponse<AudioStream> findAll(PageRequest pageRequest);

  boolean deleteById(AudioStreamId id);
}
