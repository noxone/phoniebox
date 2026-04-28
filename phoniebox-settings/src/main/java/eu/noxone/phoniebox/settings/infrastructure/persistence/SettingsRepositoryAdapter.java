package eu.noxone.phoniebox.settings.infrastructure.persistence;

import eu.noxone.phoniebox.settings.application.port.out.SettingsRepository;
import eu.noxone.phoniebox.settings.domain.model.Setting;
import eu.noxone.phoniebox.settings.domain.model.SettingKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SettingsRepositoryAdapter implements SettingsRepository {

  private final SettingsPanacheRepository panache;

  @Inject
  public SettingsRepositoryAdapter(final SettingsPanacheRepository panache) {
    this.panache = panache;
  }

  @Override
  public Optional<Setting> findByKey(final SettingKey key) {
    return panache.findByKey(key);
  }

  @Override
  public void save(final Setting setting) {
    panache.persist(setting);
  }

  @Override
  public List<Setting> findAll() {
    return panache.listAll();
  }
}
