package eu.noxone.phoniebox.media.domain.model.audio;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackTitle extends DefaultDomainAttribute<String> {
    private TrackTitle(final String value) { super(value); }
    public static TrackTitle of(final String value) { return new TrackTitle(value); }
}
