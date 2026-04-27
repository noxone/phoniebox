package eu.noxone.phoniebox.shared.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;
import java.io.IOException;

/**
 * Generic Jackson serializer for all {@link DefaultDomainAttribute} subtypes.
 *
 * <p>Unwraps the attribute value via {@link DefaultDomainAttribute#getValue()} and delegates
 * serialization to Jackson's standard pipeline for that value's type. This means {@code
 * MimeType("audio/mpeg")} serializes as {@code "audio/mpeg"}, {@code FileSize(1024)} as {@code
 * 1024}, and {@code UploadedAt(Instant)} as an ISO-8601 string — exactly as if the field were
 * declared with the primitive type directly.
 *
 * <p>Registered globally via {@link DomainAttributeJacksonCustomizer}.
 */
public class DomainAttributeSerializer extends StdSerializer<DefaultDomainAttribute<?>> {

  private static final long serialVersionUID = 1L;

  public DomainAttributeSerializer() {
    super(DefaultDomainAttribute.class, false);
  }

  @Override
  public void serialize(
      final DefaultDomainAttribute<?> value,
      final JsonGenerator gen,
      final SerializerProvider provider)
      throws IOException {
    provider.defaultSerializeValue(value.getValue(), gen);
  }
}
