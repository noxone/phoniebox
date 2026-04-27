package eu.noxone.phoniebox.shared.persistence;

import jakarta.persistence.AttributeConverter;
import java.util.function.Function;

/**
 * Generic base class for JPA {@link AttributeConverter}s that convert domain attribute types to and
 * from a single database column value.
 *
 * <p>Both conversion directions are supplied as constructor arguments, keeping each concrete
 * converter to a single-line constructor. The two-function design handles cases where the domain
 * attribute's wrapped type differs from the database column type (e.g. {@code MediaFileId} wraps
 * {@code UUID} but is stored as {@code String}):
 *
 * <pre>{@code
 * // wrapped type == column type  →  getValue() as the toDatabase function
 * @Converter(autoApply = true)
 * public class OriginalFileNameConverter
 *         extends DomainAttributeConverter<OriginalFileName, String> {
 *     public OriginalFileNameConverter() {
 *         super(OriginalFileName::of, OriginalFileName::getValue);
 *     }
 * }
 *
 * // wrapped type != column type  →  explicit conversion in toDatabase
 * @Converter(autoApply = true)
 * public class MediaFileIdConverter
 *         extends DomainAttributeConverter<MediaFileId, String> {
 *     public MediaFileIdConverter() {
 *         super(MediaFileId::of, MediaFileId::asString);
 *     }
 * }
 * }</pre>
 *
 * <p>JPA resolves the {@code autoApply} target type from the concrete type argument {@code A} in
 * the subclass declaration — type erasure does not affect this because the type is known statically
 * at the subclass level.
 *
 * @param <A> the domain attribute type handled by this converter
 * @param <T> the database column type (e.g. {@link String}, {@link Long})
 */
public abstract class DomainAttributeConverter<A, T> implements AttributeConverter<A, T> {

  private final Function<T, A> fromDatabase;
  private final Function<A, T> toDatabase;

  protected DomainAttributeConverter(
      final Function<T, A> fromDatabase, final Function<A, T> toDatabase) {
    this.fromDatabase = fromDatabase;
    this.toDatabase = toDatabase;
  }

  @Override
  public final T convertToDatabaseColumn(final A attribute) {
    return attribute == null ? null : toDatabase.apply(attribute);
  }

  @Override
  public final A convertToEntityAttribute(final T dbData) {
    return dbData == null ? null : fromDatabase.apply(dbData);
  }
}
