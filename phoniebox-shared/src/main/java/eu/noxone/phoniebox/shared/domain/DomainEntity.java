package eu.noxone.phoniebox.shared.domain;

/**
 * Marker interface for domain entities (aggregate roots and child entities).
 *
 * <p>Every class that represents a domain entity must implement this interface. The ArchUnit rules
 * in {@code phoniebox-arch} enforce that all instance fields declared on a {@code DomainEntity} are
 * themselves {@link DomainAttribute} implementations — primitive and {@code String} fields are not
 * permitted.
 *
 * <p>Use {@link DefaultDomainEntity} as a convenient base class that provides identity-based
 * equality and a default {@code toString}.
 */
public interface DomainEntity {}
