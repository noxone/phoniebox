package eu.noxone.phoniebox.settings.infrastructure.persistence;

import eu.noxone.phoniebox.settings.domain.model.Setting;
import eu.noxone.phoniebox.settings.domain.model.SettingId;
import eu.noxone.phoniebox.settings.domain.model.SettingKey;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class SettingsPanacheRepository implements PanacheRepositoryBase<Setting, SettingId> {

  public Optional<Setting> findByKey(final SettingKey key) {
    return find("key", key).firstResultOptional();
  }
}
