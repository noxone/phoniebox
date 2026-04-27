package eu.noxone.phoniebox.media.domain.model.audio;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class TrackSampleRate extends DefaultDomainAttribute<Integer> {
  private TrackSampleRate(final int value) {
    super(value);
  }

  public static TrackSampleRate of(final int value) {
    return new TrackSampleRate(value);
  }
}
