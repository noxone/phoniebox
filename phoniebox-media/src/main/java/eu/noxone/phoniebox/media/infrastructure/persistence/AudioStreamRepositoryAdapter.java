package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.application.port.out.AudioStreamRepository;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;
import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStreamId;
import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.paging.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class AudioStreamRepositoryAdapter implements AudioStreamRepository {

  private final AudioStreamPanacheRepository panache;

  @Inject
  public AudioStreamRepositoryAdapter(final AudioStreamPanacheRepository panache) {
    this.panache = panache;
  }

  @Override
  public void save(final AudioStream stream) {
    panache.persist(stream);
  }

  @Override
  public Optional<AudioStream> findById(final AudioStreamId id) {
    return panache.findByIdOptional(id);
  }

  @Override
  public PageResponse<AudioStream> findAll(final PageRequest pageRequest) {
    PanacheQuery<AudioStream> query = panache.findAll();
    long total = query.count();
    var content = query.page(pageRequest.page(), pageRequest.size()).list();
    return new PageResponse<>(content, pageRequest.page(), pageRequest.size(), total);
  }

  @Override
  public boolean deleteById(final AudioStreamId id) {
    return panache.deleteById(id);
  }
}
