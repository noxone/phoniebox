package eu.noxone.phoniebox.arch;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Shared, reusable ArchUnit rules that enforce the onion architecture
 * contract for every feature module.
 *
 * <h2>Layer responsibilities</h2>
 * <pre>
 *  ┌──────────────────────────────────────────────┐
 *  │  web            (REST resources, DTOs)        │
 *  ├──────────────────────────────────────────────┤
 *  │  infrastructure (DB entities, adapters, I/O)  │
 *  ├──────────────────────────────────────────────┤
 *  │  application    (use cases, ports in/out)     │
 *  ├──────────────────────────────────────────────┤
 *  │  domain         (entities, value objects)     │
 *  └──────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Allowed dependencies</h2>
 * <ul>
 *   <li>domain      → nothing (innermost ring, zero outward knowledge)
 *   <li>application → domain
 *   <li>infrastructure → domain + application
 *   <li>web          → domain + application  (never infrastructure)
 * </ul>
 *
 * <h2>Usage in a feature module test</h2>
 * <pre>{@code
 * class ArchitectureTest {
 *     private static final JavaClasses CLASSES =
 *         new ClassFileImporter().importPackages("eu.noxone.phoniebox.media");
 *
 *     @Test void domainIsIsolated() {
 *         OnionArchitectureRules.domainDoesNotDependOnOtherLayers("eu.noxone.phoniebox.media")
 *             .check(CLASSES);
 *     }
 * }
 * }</pre>
 */
public final class OnionArchitectureRules {

    private OnionArchitectureRules() {
    }

    /**
     * Domain classes must not reference application, infrastructure or web classes.
     */
    public static ArchRule domainDoesNotDependOnOtherLayers(final String basePackage) {
        return noClasses()
                .that().resideInAPackage(basePackage + ".domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        basePackage + ".application..",
                        basePackage + ".infrastructure..",
                        basePackage + ".web.."
                )
                .as("Domain must not depend on application, infrastructure or web");
    }

    /**
     * Application classes (use cases, ports) must not reference infrastructure or web classes.
     */
    public static ArchRule applicationDoesNotDependOnInfrastructureOrWeb(final String basePackage) {
        return noClasses()
                .that().resideInAPackage(basePackage + ".application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        basePackage + ".infrastructure..",
                        basePackage + ".web.."
                )
                .as("Application must not depend on infrastructure or web");
    }

    /**
     * Web (REST) classes must not directly reach into the infrastructure layer.
     * They must go through application-layer input ports instead.
     */
    public static ArchRule webDoesNotDependOnInfrastructure(final String basePackage) {
        return noClasses()
                .that().resideInAPackage(basePackage + ".web..")
                .should().dependOnClassesThat()
                .resideInAPackage(basePackage + ".infrastructure..")
                .as("Web must not depend on infrastructure");
    }

    /**
     * Convenience: checks all three onion rules at once.
     * Prefer calling the individual methods when you want descriptive test names.
     */
    public static ArchRule[] allRules(final String basePackage) {
        return new ArchRule[]{
                domainDoesNotDependOnOtherLayers(basePackage),
                applicationDoesNotDependOnInfrastructureOrWeb(basePackage),
                webDoesNotDependOnInfrastructure(basePackage)
        };
    }
}
