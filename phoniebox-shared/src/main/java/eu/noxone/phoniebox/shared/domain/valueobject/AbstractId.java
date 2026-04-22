package eu.noxone.phoniebox.shared.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Base class for strongly-typed UUID-based identity value objects.
 *
 * <p>Subclasses gain type-safety over raw UUIDs while keeping equality,
 * hashing and string representation consistent across the domain.
 *
 * <pre>{@code
 * public class MediaFileId extends AbstractId {
 *     private MediaFileId(UUID value) { super(value); }
 *     public static MediaFileId newId()          { return new MediaFileId(UUID.randomUUID()); }
 *     public static MediaFileId of(UUID value)   { return new MediaFileId(value); }
 *     public static MediaFileId of(String value) { return new MediaFileId(UUID.fromString(value)); }
 * }
 * }</pre>
 */
public abstract class AbstractId {

    private final UUID value;

    protected AbstractId(final UUID value) {
        this.value = Objects.requireNonNull(value, "ID value must not be null");
    }

    public UUID getValue() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AbstractId that = (AbstractId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
