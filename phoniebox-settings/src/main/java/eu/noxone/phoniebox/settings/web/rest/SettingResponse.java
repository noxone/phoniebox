package eu.noxone.phoniebox.settings.web.rest;

import eu.noxone.phoniebox.settings.domain.model.Setting;

public record SettingResponse(String key, String value) {
  public static SettingResponse from(final Setting setting) {
    return new SettingResponse(
        setting.getKey().getValue(), setting.getValueAsString().orElse(null));
  }
}
