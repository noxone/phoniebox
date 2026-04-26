package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.application.port.out.MediaFileRepository;
import eu.noxone.phoniebox.media.domain.model.MediaFile;
import eu.noxone.phoniebox.media.domain.model.MediaFileId;
import eu.noxone.phoniebox.shared.paging.PageRequest;
import eu.noxone.phoniebox.shared.paging.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

/**
 * Adapter that implements the {@link MediaFileRepository} port by delegating to
 * {@link MediaFilePanacheRepository}.
 *
 * <p>Because {@link MediaFile} is itself a JPA {@code @Entity}, no manual
 * domain↔persistence mapping is needed.  Hibernate's dirty-checking mechanism
 * automatically persists any field changes made to a managed {@link MediaFile}
 * instance when the surrounding transaction commits.
 *
 * <p>The separation from {@link MediaFilePanacheRepository} avoids method-signature
 * conflicts: Panache's {@code findById} returns the entity directly while the port
 * declares it as {@code Optional}; similarly Panache's {@code findAll} returns a
 * {@code PanacheQuery} while the port declares a plain {@code List}.
 */
@ApplicationScoped
public class MediaFileRepositoryAdapter implements MediaFileRepository {

    private final MediaFilePanacheRepository panache;

    @Inject
    public MediaFileRepositoryAdapter(final MediaFilePanacheRepository panache) {
        this.panache = panache;
    }

    @Override
    public void save(final MediaFile mediaFile) {
        panache.persist(mediaFile);
    }

    @Override
    public Optional<MediaFile> findById(final MediaFileId id) {
        return panache.findByIdOptional(id);
    }

    @Override
    public PageResponse<MediaFile> findAll(final PageRequest pageRequest) {
        PanacheQuery<MediaFile> query = panache.findAll();
        long total = query.count();
        var content = query.page(pageRequest.page(), pageRequest.size()).list();
        return new PageResponse<>(content, pageRequest.page(), pageRequest.size(), total);
    }

    @Override
    public boolean deleteById(final MediaFileId id) {
        return panache.deleteById(id);
    }
}
