package eu.noxone.phoniebox.media.domain.model.audiostream;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

/** URL of an internet radio stream. */
public final class StreamUrl extends DefaultDomainAttribute<String> {

    private StreamUrl(final String value) {
        super(value);
    }

    public static StreamUrl of(final String value) {
        return new StreamUrl(value);
    }
}
