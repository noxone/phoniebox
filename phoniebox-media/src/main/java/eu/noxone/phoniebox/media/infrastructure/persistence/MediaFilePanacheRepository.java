package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFile;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Thin Panache repository that provides JPA operations for {@link MediaFile}.
 *
 * <p>Kept package-private in intent: only {@link MediaFileRepositoryAdapter}
 * should use it.  Separating it from the adapter avoids method-signature
 * conflicts between the domain port ({@code findById} returns {@code Optional},
 * {@code findAll} returns {@code List}) and Panache's conventions ({@code findById}
 * returns the entity directly, {@code findAll} returns {@code PanacheQuery}).
 */
@ApplicationScoped
public class MediaFilePanacheRepository implements PanacheRepositoryBase<MediaFile, MediaFileId> {
}
