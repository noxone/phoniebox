package eu.noxone.phoniebox.shared.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

/**
 * Registers the generic {@link DomainAttributeSerializer} and {@link DomainAttributeDeserializer}
 * with Quarkus's shared {@link ObjectMapper}.
 *
 * <p>Quarkus discovers this bean automatically via CDI scanning of the {@code phoniebox-shared}
 * Jandex index. No explicit registration is needed in any consuming module — adding {@code
 * phoniebox-shared} as a dependency is sufficient.
 */
@Singleton
public class DomainAttributeJacksonCustomizer implements ObjectMapperCustomizer {

  @Override
  public void customize(final ObjectMapper mapper) {
    final SimpleModule module = new SimpleModule("DomainAttributeModule");
    module.addSerializer(new DomainAttributeSerializer());
    module.addDeserializer(DefaultDomainAttribute.class, new DomainAttributeDeserializer());
    mapper.registerModule(module);
  }
}
