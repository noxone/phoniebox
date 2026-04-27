/**
 * JPA/Hibernate persistence adapters for the media feature module.
 *
 * <p>{@link MediaFileIdUserType} and {@link AudioStreamIdUserType} are registered here so that
 * Hibernate maps their ID types to {@code VARCHAR} for every field — including {@code @Id} fields
 * where a JPA {@code AttributeConverter} would be silently ignored.
 */
@TypeRegistration(basicClass = MediaFileId.class, userType = MediaFileIdUserType.class)
@TypeRegistration(basicClass = AudioStreamId.class, userType = AudioStreamIdUserType.class)
package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStreamId;
import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import org.hibernate.annotations.TypeRegistration;
