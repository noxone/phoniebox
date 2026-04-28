package eu.noxone.phoniebox.settings.application.service;

import eu.noxone.phoniebox.settings.application.port.in.GetSettingUseCase;
import eu.noxone.phoniebox.settings.application.port.in.ListSettingsUseCase;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingCommand;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingUseCase;
import eu.noxone.phoniebox.settings.application.port.out.SettingsRepository;
import eu.noxone.phoniebox.settings.domain.model.Setting;
import eu.noxone.phoniebox.settings.domain.model.SettingKey;
import eu.noxone.phoniebox.settings.domain.model.SettingValue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SettingsApplicationService
    implements GetSettingUseCase, SetSettingUseCase, ListSettingsUseCase {

  private final SettingsRepository repository;

  @Inject
  public SettingsApplicationService(final SettingsRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<String> getSetting(final String key) {
    return repository.findByKey(SettingKey.of(key)).flatMap(Setting::getValueAsString);
  }

  @Override
  @Transactional
  public void setSetting(final SetSettingCommand command) {
    SettingKey key = SettingKey.of(command.key());
    SettingValue value = command.value() != null ? SettingValue.of(command.value()) : null;
    repository
        .findByKey(key)
        .ifPresentOrElse(
            existing -> existing.setValue(value),
            () -> repository.save(Setting.create(key, value)));
  }

  @Override
  public List<Setting> listSettings() {
    return repository.findAll();
  }
}
