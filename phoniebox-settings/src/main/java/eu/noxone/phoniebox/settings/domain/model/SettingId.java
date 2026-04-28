package eu.noxone.phoniebox.settings.domain.model;

import eu.noxone.phoniebox.shared.domain.valueobject.AbstractId;
import java.util.UUID;

public final class SettingId extends AbstractId {

  private SettingId(final UUID value) {
    super(value);
  }

  public static SettingId newId() {
    return new SettingId(UUID.randomUUID());
  }

  public static SettingId of(final UUID value) {
    return new SettingId(value);
  }

  public static SettingId of(final String value) {
    return new SettingId(UUID.fromString(value));
  }
}
