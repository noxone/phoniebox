package eu.noxone.phoniebox.media.application.service;

import eu.noxone.phoniebox.media.application.port.in.AddAudioStreamCommand;
import eu.noxone.phoniebox.media.application.port.in.AddAudioStreamUseCase;
import eu.noxone.phoniebox.media.application.port.in.DeleteAudioStreamUseCase;
import eu.noxone.phoniebox.media.application.port.in.GetAudioStreamUseCase;
import eu.noxone.phoniebox.media.application.port.in.ListAudioStreamsUseCase;
import eu.noxone.phoniebox.media.application.port.out.AudioStreamRepository;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStreamId;
import eu.noxone.phoniebox.media.domain.model.audiostream.StreamName;
import eu.noxone.phoniebox.media.domain.model.audiostream.StreamUrl;
import eu.noxone.phoniebox.media.domain.model.shared.MimeType;
import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.paging.PageResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AudioStreamApplicationService
    implements AddAudioStreamUseCase,
        ListAudioStreamsUseCase,
        GetAudioStreamUseCase,
        DeleteAudioStreamUseCase {

  private final AudioStreamRepository repository;

  @Inject
  public AudioStreamApplicationService(final AudioStreamRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public AudioStream add(final AddAudioStreamCommand command) {
    AudioStream stream =
        AudioStream.create(
            StreamName.of(command.name()),
            StreamUrl.of(command.url()),
            MimeType.of(command.mimeType()));
    repository.save(stream);
    return stream;
  }

  @Override
  public PageResponse<AudioStream> list(final PageRequest pageRequest) {
    return repository.findAll(pageRequest);
  }

  @Override
  public Optional<AudioStream> get(final UUID id) {
    return repository.findById(AudioStreamId.of(id));
  }

  @Override
  @Transactional
  public boolean delete(final UUID id) {
    return repository.deleteById(AudioStreamId.of(id));
  }
}
