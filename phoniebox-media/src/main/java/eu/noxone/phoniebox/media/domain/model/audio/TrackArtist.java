package eu.noxone.phoniebox.media.domain.model.audio;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackArtist extends DefaultDomainAttribute<String> {
  private TrackArtist(final String value) {
    super(value);
  }

  public static TrackArtist of(final String value) {
    return new TrackArtist(value);
  }
}
