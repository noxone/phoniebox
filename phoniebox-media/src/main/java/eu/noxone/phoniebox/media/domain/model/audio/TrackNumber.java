package eu.noxone.phoniebox.media.domain.model.audio;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackNumber extends DefaultDomainAttribute<Integer> {
  private TrackNumber(final int value) {
    super(value);
  }

  public static TrackNumber of(final int value) {
    return new TrackNumber(value);
  }
}
