package eu.noxone.phoniebox.media.domain.model.audio;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackYear extends DefaultDomainAttribute<Integer> {
    private TrackYear(final int value) { super(value); }
    public static TrackYear of(final int value) { return new TrackYear(value); }
}
