package eu.noxone.phoniebox.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import eu.noxone.phoniebox.arch.OnionArchitectureRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies global HTTP client rules across the entire {@code eu.noxone.phoniebox} codebase.
 *
 * <p>These tests are intentionally free of Quarkus / CDI: they only inspect compiled bytecode and
 * run fast without a container.
 */
class ArchitectureTest {

  private static JavaClasses classes;

  @BeforeAll
  static void importClasses() {
    classes = new ClassFileImporter().importPackages("eu.noxone.phoniebox");
  }

  @Test
  void noUrlConnectionUsage() {
    OnionArchitectureRules.noUrlConnectionUsage().check(classes);
  }
}
