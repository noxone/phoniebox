package eu.noxone.phoniebox.media.domain.model.audio;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackBitrate extends DefaultDomainAttribute<Integer> {
  private TrackBitrate(final int value) {
    super(value);
  }

  public static TrackBitrate of(final int value) {
    return new TrackBitrate(value);
  }
}
