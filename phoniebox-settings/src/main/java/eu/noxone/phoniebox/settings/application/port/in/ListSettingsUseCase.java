package eu.noxone.phoniebox.settings.application.port.in;

import eu.noxone.phoniebox.settings.domain.model.Setting;
import java.util.List;

public interface ListSettingsUseCase {
  List<Setting> listSettings();
}
