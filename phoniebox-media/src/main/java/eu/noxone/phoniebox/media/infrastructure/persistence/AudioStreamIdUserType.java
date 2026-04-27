package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStreamId;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public class AudioStreamIdUserType implements UserType<AudioStreamId> {

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<AudioStreamId> returnedClass() {
        return AudioStreamId.class;
    }

    @Override
    public boolean equals(final AudioStreamId x, final AudioStreamId y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(final AudioStreamId x) {
        return Objects.hashCode(x);
    }

    @Override
    public AudioStreamId nullSafeGet(final ResultSet rs, final int position,
            final SharedSessionContractImplementor session, final Object owner) throws SQLException {
        String value = rs.getString(position);
        return rs.wasNull() ? null : AudioStreamId.of(value);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final AudioStreamId value, final int index,
            final SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, value.asString());
        }
    }

    @Override
    public AudioStreamId deepCopy(final AudioStreamId value) {
        return value; // immutable
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final AudioStreamId value) {
        return value == null ? null : value.asString();
    }

    @Override
    public AudioStreamId assemble(final Serializable cached, final Object owner) {
        return cached == null ? null : AudioStreamId.of((String) cached);
    }
}
