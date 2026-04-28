package eu.noxone.phoniebox.settings.domain.model;

import eu.noxone.phoniebox.shared.domain.DefaultDomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "settings")
public class Setting extends DefaultDomainEntity<SettingId> {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private SettingId id;

  @Column(name = "key", nullable = false, unique = true, updatable = false)
  private SettingKey key;

  @Column(name = "value")
  private SettingValue value;

  protected Setting() {}

  private Setting(final SettingId id, final SettingKey key, final SettingValue value) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.key = Objects.requireNonNull(key, "key must not be null");
    this.value = value;
  }

  public static Setting create(final SettingKey key, final SettingValue value) {
    return new Setting(SettingId.newId(), key, value);
  }

  @Override
  public SettingId getId() {
    return id;
  }

  public SettingKey getKey() {
    return key;
  }

  public Optional<String> getValueAsString() {
    return Optional.ofNullable(value).map(SettingValue::getValue);
  }

  public void setValue(final SettingValue value) {
    this.value = value;
  }
}
