package eu.noxone.phoniebox.settings.domain.model;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

public final class SettingValue extends DefaultDomainAttribute<String> {

  private SettingValue(final String value) {
    super(value);
  }

  public static SettingValue of(final String value) {
    return new SettingValue(value);
  }
}
