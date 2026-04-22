package eu.noxone.phoniebox.shared.domain;

/**
 * Marker interface for all domain attribute value objects.
 *
 * <p>A domain attribute wraps a primitive or compound value and gives it a
 * meaningful, type-safe name in the ubiquitous language (e.g. {@code MimeType}
 * instead of {@code String}).
 *
 * <p>All instance fields declared on a {@link DomainEntity} must implement
 * this interface — plain Java types ({@code String}, {@code int}, …) are not
 * permitted directly on entities.  This rule is enforced by the ArchUnit suite
 * in {@code phoniebox-arch}.
 *
 * <p>Use {@link DefaultDomainAttribute} as a convenient base class for single-
 * value attributes.
 */
public interface DomainAttribute {
}
