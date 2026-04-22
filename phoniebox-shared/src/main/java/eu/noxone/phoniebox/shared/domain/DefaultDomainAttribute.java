package eu.noxone.phoniebox.shared.domain;

import java.util.Objects;

/**
 * Convenient base class for single-value {@link DomainAttribute} implementations.
 *
 * <p>Wraps an immutable value of type {@code T} and provides correct
 * {@link #equals}, {@link #hashCode}, and {@link #toString} based on that
 * value.  Equality is class-exact: two attributes of different concrete types
 * that happen to wrap the same value are <em>not</em> equal.
 *
 * <p>Typical usage:
 * <pre>{@code
 * public final class MimeType extends DefaultDomainAttribute<String> {
 *     private MimeType(String value) { super(value); }
 *     public static MimeType of(String value) { return new MimeType(value); }
 * }
 * }</pre>
 *
 * @param <T> the type of the wrapped value
 */
public abstract class DefaultDomainAttribute<T> implements DomainAttribute {

    private final T value;

    protected DefaultDomainAttribute(final T value) {
        this.value = Objects.requireNonNull(value, "Attribute value must not be null");
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefaultDomainAttribute<?> that = (DefaultDomainAttribute<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
