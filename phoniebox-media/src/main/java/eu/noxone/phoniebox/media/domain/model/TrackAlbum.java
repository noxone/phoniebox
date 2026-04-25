package eu.noxone.phoniebox.media.domain.model;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackAlbum extends DefaultDomainAttribute<String> {
    private TrackAlbum(final String value) { super(value); }
    public static TrackAlbum of(final String value) { return new TrackAlbum(value); }
}
