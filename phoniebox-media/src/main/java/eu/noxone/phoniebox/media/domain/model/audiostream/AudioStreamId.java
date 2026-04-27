package eu.noxone.phoniebox.media.domain.model.audiostream;

import eu.noxone.phoniebox.shared.domain.valueobject.AbstractId;
import java.util.UUID;

/** Strongly-typed identity for an {@link AudioStream}. */
public final class AudioStreamId extends AbstractId {

  private AudioStreamId(final UUID value) {
    super(value);
  }

  public static AudioStreamId newId() {
    return new AudioStreamId(UUID.randomUUID());
  }

  public static AudioStreamId of(final UUID value) {
    return new AudioStreamId(value);
  }

  public static AudioStreamId of(final String value) {
    return new AudioStreamId(UUID.fromString(value));
  }
}
