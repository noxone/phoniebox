package eu.noxone.phoniebox.media.domain.model.audio;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackGenre extends DefaultDomainAttribute<String> {
    private TrackGenre(final String value) { super(value); }
    public static TrackGenre of(final String value) { return new TrackGenre(value); }
}
