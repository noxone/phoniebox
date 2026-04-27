package eu.noxone.phoniebox.media.domain.model.mediafile;

import eu.noxone.phoniebox.shared.domain.valueobject.AbstractId;
import java.util.UUID;

/** Strongly-typed identity for a {@link MediaFile}. */
public final class MediaFileId extends AbstractId {

  private MediaFileId(final UUID value) {
    super(value);
  }

  public static MediaFileId newId() {
    return new MediaFileId(UUID.randomUUID());
  }

  public static MediaFileId of(final UUID value) {
    return new MediaFileId(value);
  }

  public static MediaFileId of(final String value) {
    return new MediaFileId(UUID.fromString(value));
  }
}
