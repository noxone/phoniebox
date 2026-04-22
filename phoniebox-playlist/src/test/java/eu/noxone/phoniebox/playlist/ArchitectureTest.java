package eu.noxone.phoniebox.playlist;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import eu.noxone.phoniebox.arch.OnionArchitectureRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the {@code phoniebox-playlist} module respects onion-architecture
 * layer boundaries and domain model conventions as defined by
 * {@link OnionArchitectureRules}.
 */
class ArchitectureTest {

    private static final String BASE_PACKAGE = "eu.noxone.phoniebox.playlist";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter().importPackages(BASE_PACKAGE);
    }

    @Test
    void domainDoesNotDependOnOtherLayers() {
        OnionArchitectureRules.domainDoesNotDependOnOtherLayers(BASE_PACKAGE).check(classes);
    }

    @Test
    void applicationDoesNotDependOnInfrastructureOrWeb() {
        OnionArchitectureRules.applicationDoesNotDependOnInfrastructureOrWeb(BASE_PACKAGE).check(classes);
    }

    @Test
    void webDoesNotDependOnInfrastructure() {
        OnionArchitectureRules.webDoesNotDependOnInfrastructure(BASE_PACKAGE).check(classes);
    }

    @Test
    void domainModelClassesMustBeEntityOrAttribute() {
        OnionArchitectureRules.domainModelClassesMustBeEntityOrAttribute(BASE_PACKAGE).check(classes);
    }

    @Test
    void entityFieldsMustBeDomainAttributes() {
        OnionArchitectureRules.entityFieldsMustBeDomainAttributes().check(classes);
    }
}
