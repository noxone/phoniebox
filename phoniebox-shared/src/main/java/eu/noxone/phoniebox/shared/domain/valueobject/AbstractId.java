package eu.noxone.phoniebox.shared.domain.valueobject;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

import java.util.UUID;

/**
 * Base class for strongly-typed UUID-based identity value objects.
 *
 * <p>Extends {@link DefaultDomainAttribute}{@code <UUID>} to inherit value
 * equality, hashing, and {@code toString}.  Subclasses gain type-safety over
 * raw {@link UUID}s while satisfying the {@code DomainAttribute} contract
 * required on entity fields.
 *
 * <pre>{@code
 * public final class MediaFileId extends AbstractId {
 *     private MediaFileId(UUID value) { super(value); }
 *     public static MediaFileId newId()          { return new MediaFileId(UUID.randomUUID()); }
 *     public static MediaFileId of(UUID value)   { return new MediaFileId(value); }
 *     public static MediaFileId of(String value) { return new MediaFileId(UUID.fromString(value)); }
 * }
 * }</pre>
 */
public abstract class AbstractId extends DefaultDomainAttribute<UUID> {

    protected AbstractId(final UUID value) {
        super(value);
    }

    /** Returns the UUID as a lower-case hyphenated string. */
    public String asString() {
        return getValue().toString();
    }
}
