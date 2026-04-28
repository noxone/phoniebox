package eu.noxone.phoniebox.settings.infrastructure.persistence;

import eu.noxone.phoniebox.settings.domain.model.SettingId;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

public class SettingIdUserType implements UserType<SettingId> {

  @Override
  public int getSqlType() {
    return Types.VARCHAR;
  }

  @Override
  public Class<SettingId> returnedClass() {
    return SettingId.class;
  }

  @Override
  public boolean equals(final SettingId x, final SettingId y) {
    return Objects.equals(x, y);
  }

  @Override
  public int hashCode(final SettingId x) {
    return Objects.hashCode(x);
  }

  @Override
  public SettingId nullSafeGet(
      final ResultSet rs,
      final int position,
      final SharedSessionContractImplementor session,
      final Object owner)
      throws SQLException {
    String value = rs.getString(position);
    return rs.wasNull() ? null : SettingId.of(value);
  }

  @Override
  public void nullSafeSet(
      final PreparedStatement st,
      final SettingId value,
      final int index,
      final SharedSessionContractImplementor session)
      throws SQLException {
    if (value == null) {
      st.setNull(index, Types.VARCHAR);
    } else {
      st.setString(index, value.asString());
    }
  }

  @Override
  public SettingId deepCopy(final SettingId value) {
    return value; // immutable
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(final SettingId value) {
    return value == null ? null : value.asString();
  }

  @Override
  public SettingId assemble(final Serializable cached, final Object owner) {
    return cached == null ? null : SettingId.of((String) cached);
  }
}
