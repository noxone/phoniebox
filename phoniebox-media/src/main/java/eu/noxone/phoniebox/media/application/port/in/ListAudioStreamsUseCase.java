package eu.noxone.phoniebox.media.application.port.in;

import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;
import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.paging.PageResponse;

public interface ListAudioStreamsUseCase {

    PageResponse<AudioStream> list(PageRequest pageRequest);
}
