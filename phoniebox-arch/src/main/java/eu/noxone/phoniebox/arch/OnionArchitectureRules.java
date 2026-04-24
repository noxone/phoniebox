package eu.noxone.phoniebox.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import eu.noxone.phoniebox.shared.domain.DomainAttribute;
import eu.noxone.phoniebox.shared.domain.DomainEntity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
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
 * <h2>Domain model rules</h2>
 * <ul>
 *   <li>All concrete classes in {@code domain.model} must implement
 *       {@link DomainEntity} or {@link DomainAttribute}.
 *   <li>All instance fields declared on a {@link DomainEntity} must implement
 *       {@link DomainAttribute} — plain Java types are not permitted.
 * </ul>
 */
public final class OnionArchitectureRules {

    private OnionArchitectureRules() {
    }

    // ── Layer boundary rules ──────────────────────────────────────────────────

    /**
     * Domain classes must not reference application, infrastructure or web classes.
     *
     * <p><strong>Exception:</strong> infrastructure classes annotated with
     * {@link Converter @Converter} may be referenced from domain entity {@code @Id} fields
     * via an explicit {@code @Convert} annotation.  The JPA specification prohibits
     * {@code autoApply = true} converters from being applied to identifier attributes,
     * so the converter class must be named directly on the {@code @Id} field.
     */
    public static ArchRule domainDoesNotDependOnOtherLayers(final String basePackage) {
        return noClasses()
                .that().resideInAPackage(basePackage + ".domain..")
                .should(new ArchCondition<JavaClass>(
                        "not depend on application, infrastructure or web" +
                        " (exception: @Converter-annotated classes for @Id field mapping)") {
                    @Override
                    public void check(final JavaClass javaClass, final ConditionEvents events) {
                        javaClass.getDirectDependenciesFromSelf().forEach(dep -> {
                            final JavaClass target = dep.getTargetClass();
                            final String pkg = target.getPackageName();
                            final boolean inForbiddenLayer =
                                    pkg.startsWith(basePackage + ".application") ||
                                    pkg.startsWith(basePackage + ".infrastructure") ||
                                    pkg.startsWith(basePackage + ".web");
                            if (inForbiddenLayer && !target.isAnnotatedWith("jakarta.persistence.Converter")) {
                                events.add(SimpleConditionEvent.violated(javaClass, String.format(
                                        "Domain class <%s> must not depend on <%s>",
                                        javaClass.getName(), target.getName())));
                            }
                        });
                    }
                })
                .as("Domain must not depend on application, infrastructure or web" +
                    " (@Converter classes excepted for @Id field mapping)")
                .allowEmptyShould(true);
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
                .as("Application must not depend on infrastructure or web")
                .allowEmptyShould(true);
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
                .as("Web must not depend on infrastructure")
                .allowEmptyShould(true);
    }

    // ── Domain model rules ────────────────────────────────────────────────────

    /**
     * Every concrete class in the {@code domain.model} package must implement
     * either {@link DomainEntity} or {@link DomainAttribute}.
     *
     * <p>This prevents plain POJOs or data-bags from entering the domain model
     * without being tagged with their intended role.
     */
    public static ArchRule domainModelClassesMustBeEntityOrAttribute(final String basePackage) {
        return classes()
                .that().resideInAPackage(basePackage + ".domain.model..")
                .and().areNotInterfaces()
                .and().areNotAnonymousClasses()
                .should().beAssignableTo(DomainEntity.class)
                .orShould().beAssignableTo(DomainAttribute.class)
                .as("All concrete domain model classes must implement DomainEntity or DomainAttribute")
                .allowEmptyShould(true);
    }

    /**
     * Every instance field declared on a {@link DomainEntity} class must be of
     * a type that implements {@link DomainAttribute}.
     *
     * <p>This enforces that entities never hold raw Java types ({@code String},
     * {@code int}, {@code boolean}, …) directly — every value must be expressed
     * through a named, typed domain attribute.
     */
    public static ArchRule entityFieldsMustBeDomainAttributes() {
        final ArchCondition<JavaField> beDomainAttribute =
                new ArchCondition<>("be of a type that implements DomainAttribute") {
                    @Override
                    public void check(final JavaField field, final ConditionEvents events) {
                        if (!field.getRawType().isAssignableTo(DomainAttribute.class)) {
                            events.add(SimpleConditionEvent.violated(
                                    field,
                                    String.format(
                                            "Field [%s] in [%s] has type [%s] which does not implement DomainAttribute",
                                            field.getName(),
                                            field.getOwner().getName(),
                                            field.getRawType().getName())));
                        }
                    }
                };

        return fields()
                .that().areDeclaredInClassesThat().implement(DomainEntity.class)
                .and().areNotStatic()
                .should(beDomainAttribute)
                .as("All instance fields of DomainEntity classes must implement DomainAttribute")
                .allowEmptyShould(true);
    }

    // ── Convenience ──────────────────────────────────────────────────────────

    /**
     * Returns all rules for a feature module.  Prefer calling the individual
     * methods when you want descriptive test names.
     */
    public static ArchRule[] allRules(final String basePackage) {
        return new ArchRule[]{
                domainDoesNotDependOnOtherLayers(basePackage),
                applicationDoesNotDependOnInfrastructureOrWeb(basePackage),
                webDoesNotDependOnInfrastructure(basePackage),
                domainModelClassesMustBeEntityOrAttribute(basePackage),
                entityFieldsMustBeDomainAttributes(),
        };
    }
}
