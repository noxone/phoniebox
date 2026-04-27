package eu.noxone.phoniebox.media.domain.model.audiostream;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

/** Human-readable name of an internet radio stream. */
public final class StreamName extends DefaultDomainAttribute<String> {

    private StreamName(final String value) {
        super(value);
    }

    public static StreamName of(final String value) {
        return new StreamName(value);
    }
}
