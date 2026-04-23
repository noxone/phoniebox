package eu.noxone.phoniebox.shared.persistence;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;
import jakarta.persistence.AttributeConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.function.Function;

/**
 * Reflective base class for JPA converters where the domain attribute's wrapped type
 * is the same as the database column type (e.g. {@code DefaultDomainAttribute<String>} stored
 * as {@code VARCHAR}, {@code DefaultDomainAttribute<Long>} stored as {@code BIGINT}).
 *
 * <p>At construction time this class resolves the concrete type arguments {@code A} and {@code T}
 * from the subclass declaration, then locates the public static {@code of(T)} factory method on
 * {@code A} via reflection (trying the primitive equivalent when {@code T} is a boxed numeric,
 * so that e.g. {@code FileSize.of(long)} is found for {@code T = Long}).
 * {@link #convertToDatabaseColumn} simply delegates to {@link DefaultDomainAttribute#getValue()}.
 *
 * <p>A concrete converter reduces to a single annotation + empty class body:
 * <pre>{@code
 * @Converter(autoApply = true)
 * public class MimeTypeConverter extends ReflectiveDomainAttributeConverter<MimeType, String> {}
 * }</pre>
 *
 * <p><strong>When NOT to use this class:</strong> if the domain attribute's wrapped type differs
 * from the database column type (e.g. {@code AbstractId} wraps {@code UUID} but is stored as
 * {@code String}; {@code UploadedAt} wraps {@code Instant} but is stored as an ISO-8601
 * {@code String}), use {@link DomainAttributeConverter} with explicit {@code fromDatabase} and
 * {@code toDatabase} functions instead.
 *
 * @param <A> the domain attribute type — must extend {@link DefaultDomainAttribute}{@code <T>}
 * @param <T> the database column type, which must equal the wrapped type of {@code A}
 */
public abstract class ReflectiveDomainAttributeConverter<A extends DefaultDomainAttribute<T>, T>
        implements AttributeConverter<A, T> {

    /** Maps boxed numeric types to their primitive counterparts for reflective method lookup. */
    private static final Map<Class<?>, Class<?>> BOXED_TO_PRIMITIVE = Map.of(
            Long.class, long.class,
            Integer.class, int.class,
            Double.class, double.class,
            Float.class, float.class,
            Boolean.class, boolean.class,
            Short.class, short.class,
            Byte.class, byte.class,
            Character.class, char.class
    );

    private final Function<T, A> fromDatabase;

    @SuppressWarnings("unchecked")
    protected ReflectiveDomainAttributeConverter() {
        final ParameterizedType superType = (ParameterizedType) getClass().getGenericSuperclass();
        final Class<A> domainType = (Class<A>) superType.getActualTypeArguments()[0];
        final Class<T> dbType = (Class<T>) superType.getActualTypeArguments()[1];

        final Method factoryMethod = resolveFactoryMethod(domainType, dbType);
        fromDatabase = dbValue -> {
            try {
                return (A) factoryMethod.invoke(null, dbValue);
            } catch (final InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(
                        "Cannot convert database value '" + dbValue + "' to " + domainType.getSimpleName(), e);
            }
        };
    }

    private static <A> Method resolveFactoryMethod(final Class<A> domainType, final Class<?> dbType) {
        try {
            return domainType.getMethod("of", dbType);
        } catch (final NoSuchMethodException boxedNotFound) {
            final Class<?> primitive = BOXED_TO_PRIMITIVE.get(dbType);
            if (primitive != null) {
                try {
                    return domainType.getMethod("of", primitive);
                } catch (final NoSuchMethodException ignored) {
                    // fall through to exception below
                }
            }
            throw new IllegalStateException(
                    domainType.getSimpleName() + " must declare a public static of("
                            + dbType.getSimpleName() + ") factory method for reflective conversion",
                    boxedNotFound);
        }
    }

    @Override
    public final T convertToDatabaseColumn(final A attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public final A convertToEntityAttribute(final T dbData) {
        return dbData == null ? null : fromDatabase.apply(dbData);
    }
}
