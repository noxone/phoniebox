package eu.noxone.phoniebox.media;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import eu.noxone.phoniebox.arch.OnionArchitectureRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the {@code phoniebox-media} module respects onion-architecture layer boundaries and
 * domain model conventions as defined by {@link OnionArchitectureRules}.
 *
 * <p>These tests are intentionally free of Quarkus / CDI: they only inspect compiled bytecode and
 * run fast without a container.
 */
class ArchitectureTest {

  private static final String BASE_PACKAGE = "eu.noxone.phoniebox.media";

  private static JavaClasses classes;

  @BeforeAll
  static void importClasses() {
    classes = new ClassFileImporter().importPackages(BASE_PACKAGE);
  }

  // ── Layer boundary rules ──────────────────────────────────────────────────

  @Test
  void domainDoesNotDependOnOtherLayers() {
    OnionArchitectureRules.domainDoesNotDependOnOtherLayers(BASE_PACKAGE).check(classes);
  }

  @Test
  void applicationDoesNotDependOnInfrastructureOrWeb() {
    OnionArchitectureRules.applicationDoesNotDependOnInfrastructureOrWeb(BASE_PACKAGE)
        .check(classes);
  }

  @Test
  void webDoesNotDependOnInfrastructure() {
    OnionArchitectureRules.webDoesNotDependOnInfrastructure(BASE_PACKAGE).check(classes);
  }

  // ── Domain model rules ────────────────────────────────────────────────────

  @Test
  void domainModelClassesMustBeEntityOrAttribute() {
    OnionArchitectureRules.domainModelClassesMustBeEntityOrAttribute(BASE_PACKAGE).check(classes);
  }

  @Test
  void entityFieldsMustBeDomainAttributes() {
    OnionArchitectureRules.entityFieldsMustBeDomainAttributes().check(classes);
  }

  // ── Web API contract rules ────────────────────────────────────────────────

  @Test
  void restEndpointsMustNotExposeDomainTypes() {
    OnionArchitectureRules.restEndpointsMustNotExposeDomainTypes(BASE_PACKAGE).check(classes);
  }
}
