package eu.noxone.phoniebox.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import eu.noxone.phoniebox.shared.domain.DomainAttribute;
import eu.noxone.phoniebox.shared.domain.DomainEntity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
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
                .as("Domain must not depend on application, infrastructure or web")
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

    // ── Web API contract rules ────────────────────────────────────────────────

    /**
     * Public methods on JAX-RS resource classes (annotated with {@code @Path})
     * must not use {@link DomainEntity} or {@link DomainAttribute} types directly
     * as parameter or return types.
     *
     * <p>Domain types are internal model classes.  Exposing them directly in the
     * HTTP API means that every internal model change automatically breaks the
     * API contract — clients would see different field names, types, or structure
     * without any deliberate versioning decision.  The web layer must map domain
     * objects to dedicated DTO/response types before returning them, and must
     * accept plain Java types (e.g. {@code UUID}, {@code String}) as parameters.
     *
     * <p>Jackson serializers, DTO factory methods (e.g. {@code from(DomainType)}),
     * and other web-layer infrastructure are intentionally excluded — only
     * {@code @Path}-annotated resource classes are checked.
     *
     * <p>Note: generic type arguments (e.g. the {@code T} in {@code List<T>}) are
     * not inspected — only the raw declared types are checked.
     */
    public static ArchRule restEndpointsMustNotExposeDomainTypes(final String basePackage) {
        return methods()
                .that().areDeclaredInClassesThat()
                    .resideInAPackage(basePackage + ".web..")
                    .and().areAnnotatedWith("jakarta.ws.rs.Path")
                .and().arePublic()
                .should(new ArchCondition<JavaMethod>(
                        "not use DomainEntity or DomainAttribute as parameter or return type") {
                    @Override
                    public void check(final JavaMethod method, final ConditionEvents events) {
                        checkType(method, method.getRawReturnType(), "return type", events);
                        method.getRawParameterTypes().forEach(paramType ->
                                checkType(method, paramType, "parameter type", events));
                    }

                    private void checkType(final JavaMethod method, final JavaClass type,
                                           final String role, final ConditionEvents events) {
                        if (type.isAssignableTo(DomainEntity.class)
                                || type.isAssignableTo(DomainAttribute.class)) {
                            events.add(SimpleConditionEvent.violated(method, String.format(
                                    "Method [%s.%s] must not use domain type [%s] as %s"
                                    + " — map it to a dedicated DTO instead",
                                    method.getOwner().getSimpleName(),
                                    method.getName(),
                                    type.getSimpleName(),
                                    role)));
                        }
                    }
                })
                .as("REST endpoints must not expose DomainEntity or DomainAttribute in their API contract")
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
                restEndpointsMustNotExposeDomainTypes(basePackage),
        };
    }
}
