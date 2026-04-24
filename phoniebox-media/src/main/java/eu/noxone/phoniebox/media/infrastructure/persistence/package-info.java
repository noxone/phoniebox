/**
 * JPA/Hibernate persistence adapters for the media feature module.
 *
 * <p>{@link MediaFileIdUserType} is registered here so that Hibernate maps
 * {@link eu.noxone.phoniebox.media.domain.model.MediaFileId} to {@code VARCHAR}
 * for every field — including {@code @Id} fields where a JPA {@code AttributeConverter}
 * would be silently ignored per the JPA specification.
 */
@TypeRegistration(basicClass = MediaFileId.class, userType = MediaFileIdUserType.class)
package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.MediaFileId;
import org.hibernate.annotations.TypeRegistration;
