package eu.noxone.phoniebox.media.domain.model;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

/** The IANA media type of a file (e.g. {@code audio/mpeg}). */
public final class MimeType extends DefaultDomainAttribute<String> {

    private MimeType(final String value) {
        super(value);
    }

    public static MimeType of(final String value) {
        return new MimeType(value);
    }
}
