package eu.noxone.phoniebox.settings.application.port.out;

import eu.noxone.phoniebox.settings.domain.model.Setting;
import eu.noxone.phoniebox.settings.domain.model.SettingKey;
import java.util.List;
import java.util.Optional;

public interface SettingsRepository {
  Optional<Setting> findByKey(SettingKey key);

  void save(Setting setting);

  List<Setting> findAll();
}
