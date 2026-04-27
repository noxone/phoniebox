package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * Hibernate {@link UserType} that maps {@link MediaFileId} to a {@code VARCHAR} column.
 *
 * <p>Unlike a JPA {@code AttributeConverter}, a {@code UserType} applies to {@code @Id}
 * fields as well as regular columns.  It is registered globally via
 * {@code @TypeRegistration} in {@code package-info.java} of this package, so no
 * annotation is needed on the domain entity.
 */
public class MediaFileIdUserType implements UserType<MediaFileId> {

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<MediaFileId> returnedClass() {
        return MediaFileId.class;
    }

    @Override
    public boolean equals(final MediaFileId x, final MediaFileId y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(final MediaFileId x) {
        return Objects.hashCode(x);
    }

    @Override
    public MediaFileId nullSafeGet(final ResultSet rs, final int position,
            final SharedSessionContractImplementor session, final Object owner) throws SQLException {
        String value = rs.getString(position);
        return rs.wasNull() ? null : MediaFileId.of(value);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final MediaFileId value, final int index,
            final SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, value.asString());
        }
    }

    @Override
    public MediaFileId deepCopy(final MediaFileId value) {
        return value; // immutable
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final MediaFileId value) {
        return value == null ? null : value.asString();
    }

    @Override
    public MediaFileId assemble(final Serializable cached, final Object owner) {
        return cached == null ? null : MediaFileId.of((String) cached);
    }
}
