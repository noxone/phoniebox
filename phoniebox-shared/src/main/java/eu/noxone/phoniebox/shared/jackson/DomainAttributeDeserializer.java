package eu.noxone.phoniebox.shared.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Generic Jackson deserializer for all {@link DefaultDomainAttribute} subtypes.
 *
 * <p>Implements {@link ContextualDeserializer} so Jackson can specialise this
 * deserializer for each concrete attribute type it encounters.  When Jackson
 * resolves the deserializer for a field like {@code MimeType mimeType}, it
 * calls {@link #createContextual} with {@code MimeType.class} as the target
 * type.  The returned specialised instance resolves the wrapped type {@code T}
 * from the generic supertype chain and locates the {@code of(T)} factory method
 * by reflection.  Subsequent calls to {@link #deserialize} read the JSON value
 * as {@code T} and invoke the factory to produce the domain attribute.
 *
 * <p>Example — JSON {@code "audio/mpeg"} deserializes to
 * {@code MimeType.of("audio/mpeg")}; JSON {@code 1024} deserializes to
 * {@code FileSize.of(1024L)}.
 *
 * <p>Registered globally via {@link DomainAttributeJacksonCustomizer}.
 */
public class DomainAttributeDeserializer extends StdDeserializer<DefaultDomainAttribute<?>>
        implements ContextualDeserializer {

    private static final long serialVersionUID = 1L;

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

    private final Class<?> wrappedType;
    private final Method factoryMethod;

    /** Default instance registered with the Jackson module — not used for actual deserialization. */
    public DomainAttributeDeserializer() {
        super(DefaultDomainAttribute.class);
        this.wrappedType = null;
        this.factoryMethod = null;
    }

    /** Specialized instance created by {@link #createContextual} for one concrete attribute type. */
    private DomainAttributeDeserializer(final Class<? extends DefaultDomainAttribute<?>> attributeType) {
        super(attributeType);
        this.wrappedType = resolveWrappedType(attributeType);
        this.factoryMethod = resolveFactoryMethod(attributeType, wrappedType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonDeserializer<?> createContextual(final DeserializationContext ctxt,
                                                final BeanProperty property) throws JsonMappingException {
        final var javaType = property != null ? property.getType() : ctxt.getContextualType();
        if (javaType != null && DefaultDomainAttribute.class.isAssignableFrom(javaType.getRawClass())) {
            return new DomainAttributeDeserializer(
                    (Class<? extends DefaultDomainAttribute<?>>) javaType.getRawClass());
        }
        return this;
    }

    @Override
    public DefaultDomainAttribute<?> deserialize(final JsonParser p,
                                                 final DeserializationContext ctxt) throws IOException {
        if (factoryMethod == null) {
            throw new JsonMappingException(p,
                    "DomainAttributeDeserializer used without type context — this is a bug");
        }
        final Object value = ctxt.readValue(p, wrappedType);
        try {
            return (DefaultDomainAttribute<?>) factoryMethod.invoke(null, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JsonMappingException(p,
                    "Failed to construct " + handledType().getSimpleName() + " from JSON value", e);
        }
    }

    private static Class<?> resolveWrappedType(final Class<?> attributeClass) {
        Class<?> current = attributeClass;
        while (current != null && current != Object.class) {
            final Type genericSuper = current.getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType pt
                    && pt.getRawType() == DefaultDomainAttribute.class) {
                return (Class<?>) pt.getActualTypeArguments()[0];
            }
            current = current.getSuperclass();
        }
        throw new IllegalArgumentException(
                "Cannot resolve wrapped type for " + attributeClass.getName()
                + " — it does not extend DefaultDomainAttribute<T>");
    }

    private static Method resolveFactoryMethod(final Class<?> attributeClass, final Class<?> wrappedType) {
        try {
            return attributeClass.getMethod("of", wrappedType);
        } catch (final NoSuchMethodException first) {
            final Class<?> primitive = BOXED_TO_PRIMITIVE.get(wrappedType);
            if (primitive != null) {
                try {
                    return attributeClass.getMethod("of", primitive);
                } catch (final NoSuchMethodException ignored) {
                    // fall through
                }
            }
            throw new IllegalArgumentException(
                    "No public static of(" + wrappedType.getSimpleName() + ") factory method on "
                    + attributeClass.getName(), first);
        }
    }
}
