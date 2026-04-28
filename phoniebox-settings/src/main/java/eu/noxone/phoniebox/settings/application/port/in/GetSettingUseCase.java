package eu.noxone.phoniebox.settings.application.port.in;

import java.util.Optional;

public interface GetSettingUseCase {
  Optional<String> getSetting(String key);
}
