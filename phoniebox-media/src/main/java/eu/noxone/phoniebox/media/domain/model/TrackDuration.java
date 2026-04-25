package eu.noxone.phoniebox.media.domain.model;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackDuration extends DefaultDomainAttribute<Integer> {
    private TrackDuration(final int value) { super(value); }
    public static TrackDuration of(final int value) { return new TrackDuration(value); }
}
