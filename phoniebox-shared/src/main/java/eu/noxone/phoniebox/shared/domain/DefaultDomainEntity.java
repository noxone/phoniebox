package eu.noxone.phoniebox.shared.domain;

import java.util.Objects;

/**
 * Convenient base class for {@link DomainEntity} implementations.
 *
 * <p>Provides identity-based {@link #equals} and {@link #hashCode}: two
 * entities of the same concrete type are equal if and only if their IDs are
 * equal.  Subclasses must expose their ID via {@link #getId()}.
 *
 * <p>Typical usage:
 * <pre>{@code
 * public final class MediaFile extends DefaultDomainEntity<MediaFileId> {
 *
 *     private final MediaFileId id;
 *     // … other DomainAttribute fields …
 *
 *     @Override
 *     public MediaFileId getId() { return id; }
 * }
 * }</pre>
 *
 * @param <ID> the type of the entity's identity attribute; must itself be a
 *             {@link DomainAttribute}
 */
public abstract class DefaultDomainEntity<ID extends DomainAttribute> implements DomainEntity {

    /** Returns the unique identity of this entity. */
    public abstract ID getId();

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefaultDomainEntity<?> that = (DefaultDomainEntity<?>) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + getId() + "}";
    }
}
