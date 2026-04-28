package eu.noxone.phoniebox.settings.domain.model;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class SettingKey extends DefaultDomainAttribute<String> {

  private SettingKey(final String value) {
    super(value);
  }

  public static SettingKey of(final String value) {
    return new SettingKey(value);
  }
}
