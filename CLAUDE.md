# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build entire project (Java + frontend)
mvn clean install

# Build skipping tests
mvn clean install -DskipTests

# Build native image (GraalVM)
mvn clean install -Pnative

# Run backend in dev mode (hot reload, port 8080)
# Always run a full install first so that annotation-processor-generated classes
# (e.g. JPA converters) and Jandex indexes are present in the local Maven repo.
mvn clean install -DskipTests && cd phoniebox-app && mvn quarkus:dev

# Frontend only (dev server on port 5173)
cd phoniebox-frontend && npm install && npm run dev

# Frontend production build (outputs to phoniebox-app/src/main/resources/META-INF/resources)
cd phoniebox-frontend && npm run build
```

## Test Commands

```bash
# All unit tests
mvn clean test

# Unit + integration tests
mvn clean verify

# Single test class
mvn test -Dtest=ArchitectureTest

# Tests in a specific module
mvn test -pl phoniebox-media
```

## Architecture

This is a **Maven multi-module Quarkus application** with a **Vue 3 SPA frontend**, following strict **onion (hexagonal) architecture** enforced by ArchUnit tests.

### Module Layout

| Module | Role |
|--------|------|
| `phoniebox-shared` | Pure Java base abstractions (e.g. `AbstractId` UUID wrapper); zero framework deps |
| `phoniebox-arch` | Reusable ArchUnit rules that enforce layer boundaries across all feature modules |
| `phoniebox-media` | Fully implemented media file CRUD feature |
| `phoniebox-audio` | Audio playback via `javax.sound.sampled` |
| `phoniebox-gpio` | Placeholder — future pi4j GPIO/button control |
| `phoniebox-playlist` | Placeholder — future playlist management |
| `phoniebox-app` | **Only runnable module.** Assembles all feature modules, configures SQLite + Flyway, serves the built Vue SPA as static files |
| `phoniebox-frontend` | Vue 3 + TypeScript SPA; built by Maven via `frontend-maven-plugin` |

### Onion Architecture Layers

Each feature module (e.g. `phoniebox-media`) is internally structured as:

```
domain/        → Pure business logic. No framework or infrastructure dependencies.
application/   → Use cases (interfaces + implementations) and ports (outbound interfaces).
               → Must not depend on infrastructure or web layers.
infrastructure/ → Adapters implementing ports (Panache repositories, filesystem I/O).
web/           → REST endpoints and DTOs. Calls application layer only, never infrastructure.
```

Layer rules are defined in `phoniebox-arch` and applied as ArchUnit tests. Violating them will cause test failures.

### Key Patterns

- **Use case interfaces** (e.g. `UploadMediaFileUseCase`) defined in `application/`, implemented by an `ApplicationService`, injected into `web/` resources.
- **Port interfaces** (e.g. `FileStoragePort`, `MediaFileRepository`) defined in `application/`, implemented by adapters in `infrastructure/`.
- **Strongly-typed IDs**: Domain IDs extend `AbstractId` from `phoniebox-shared` (UUID wrappers that also implement `DomainAttribute`).

### Domain Entity / Attribute Model

All domain classes in `domain/model` must be tagged with one of two marker interfaces from `phoniebox-shared`:

| Interface | Base class | Purpose |
|-----------|-----------|---------|
| `DomainEntity` | `DefaultDomainEntity<ID>` | Aggregate roots and child entities. Provides ID-based `equals`/`hashCode`/`toString`. |
| `DomainAttribute` | `DefaultDomainAttribute<T>` | Single-value or compound value objects. Provides value-based `equals`/`hashCode`/`toString`. |

**Rule**: every instance field declared on a `DomainEntity` must be of a type that implements `DomainAttribute`. Plain Java types (`String`, `int`, `boolean`, `Instant`, …) are not permitted directly on entities — each value must be wrapped in a named domain attribute class. This rule is enforced by ArchUnit.

Example — adding a field to an entity:
```java
// ✗ forbidden — raw type on an entity
private final String title;

// ✓ correct — wrap it in a domain attribute
public final class Title extends DefaultDomainAttribute<String> {
    private Title(String v) { super(v); }
    public static Title of(String v) { return new Title(v); }
}
private final Title title;
```

`AbstractId` extends `DefaultDomainAttribute<UUID>`, so all ID types automatically satisfy the `DomainAttribute` contract.

Compound value objects (e.g. `MediaFileMetadata`) implement `DomainAttribute` directly, making them legal entity fields while keeping their own internal fields typed as `DomainAttribute` sub-types.

### Frontend–Backend Connection

- **Development**: Vite dev server (`localhost:5173`) proxies all `/api/*` requests to the Quarkus backend (`localhost:8080`). CORS is enabled in dev mode.
- **Production**: `npm run build` writes output to `phoniebox-app/src/main/resources/META-INF/resources`. Quarkus serves the SPA and API from the same origin — no CORS needed.

### Persistence

- **Database**: SQLite (embedded, zero-setup). Schema managed by Flyway; migrations in `phoniebox-app/src/main/resources/db/migration/`.
- **ORM**: Hibernate Panache (repository pattern). Domain entities are annotated directly with `@Entity` / `@Embeddable` — there are no separate JPA entity classes. Hibernate's dirty-checking persists field changes automatically when the transaction commits.
- **Attribute converters**: Domain attribute types are mapped to DB columns via two mechanisms in `infrastructure/persistence`:

  | Mechanism | When to use | Example |
  |---|---|---|
  | `@Converter(autoApply = true)` extending `ReflectiveDomainAttributeConverter<A, T>` | Wrapped type == DB column type (e.g. `String`, `Long`) | `OriginalFileName`, `MimeType`, `FileSize` — empty-body classes, auto-generated by `phoniebox-processor` |
  | `@Converter(autoApply = true)` extending `DomainAttributeConverter<A, T>` | Wrapped type ≠ DB column type (e.g. `Instant`→`String`) | `UploadedAtConverter` — single-line constructor with explicit lambdas |
  | Hibernate `UserType<A>` + `@TypeRegistration` in `package-info.java` | `@Id` fields — JPA spec prohibits `AttributeConverter` on identifier attributes | `MediaFileIdUserType` for `MediaFileId` (UUID stored as VARCHAR) |

  ```java
  // wrapped type == DB type → empty body, generated by annotation processor
  @Converter(autoApply = true)
  public class MimeTypeConverter extends ReflectiveDomainAttributeConverter<MimeType, String> {}

  // wrapped type != DB type → explicit fromDatabase / toDatabase functions
  @Converter(autoApply = true)
  public class UploadedAtConverter extends DomainAttributeConverter<UploadedAt, String> {
      public UploadedAtConverter() { super(s -> UploadedAt.of(Instant.parse(s)), at -> at.getValue().toString()); }
  }

  // UUID @Id field → UserType registered in package-info.java (no annotation on domain entity needed)
  public class MediaFileIdUserType implements UserType<MediaFileId> { ... }
  ```

  `ReflectiveDomainAttributeConverter` resolves type arguments at construction via `getGenericSuperclass()` and locates the `of(T)` factory method by reflection, trying the primitive equivalent when `T` is a boxed numeric (so `FileSize.of(long)` is found for `T = Long`).

  **Never use `@Convert` on `@Id` fields**: the JPA specification prohibits `AttributeConverter` (both auto-apply and explicit `@Convert`) on identifier attributes. Quarkus enforces this. Use a Hibernate `UserType` with `@TypeRegistration` in `package-info.java` instead — it applies to all fields including `@Id`, and requires no annotation in the domain entity.
- **Repository pattern**: Each feature module has a `*PanacheRepository` (a bare `PanacheRepositoryBase` implementation) and a `*RepositoryAdapter` that implements the domain port by delegating to it. The separation avoids method-signature conflicts between Panache conventions and the port interface.
- **Quarkus indexing**: Library modules (e.g. `phoniebox-media`) are not scanned by Quarkus automatically. Each module that contains JPA entities must be listed in `phoniebox-app/src/main/resources/application.properties` under `quarkus.index-dependency.<name>.*`.
- **File storage**: Media files stored on local filesystem at `./media-storage` (configurable via `phoniebox.media.storage-path`).

#### JPA annotations in the domain layer

Domain entities and embeddables carry `jakarta.persistence` annotations (`@Entity`, `@Table`, `@Column`, `@Embedded`, `@Embeddable`). This is a deliberate trade-off: eliminating manual domain↔ORM mapping is considered worth the cost of standard-API annotations in the domain. The JPA annotations are purely declarative and contain no Quarkus- or Hibernate-specific behaviour.

Consequences:
- Domain entity fields must be non-`final` (Hibernate sets them via reflection after calling the no-arg constructor).
- Domain entity classes must be non-`final` (Hibernate enhancement requires subclassability).
- A protected no-arg constructor must exist alongside the public factory methods.

### Tech Stack

- **Java 21**, Quarkus 3.22.3 (`quarkus-rest`, `quarkus-hibernate-orm-panache`, `quarkus-flyway`, `quarkus-jdbc-sqlite`)
- **`java.net.http.HttpClient`** (JDK built-in) — the only permitted HTTP client. `java.net.URLConnection`, `URL.openStream()`, and `URL.openConnection()` are banned by ArchUnit rule. Always build the client with `.connectTimeout(Duration)`. Use `BodyHandlers.ofString()` for short responses and `BodyHandlers.ofInputStream()` for streaming — the latter returns a live `InputStream` with no read-timeout set on the request, so the connection stays open for the duration of the stream.
- **Vue 3.4**, Vue Router 4.3, TypeScript 5.4, Vite 5.2, Tailwind CSS 3.4
- **Node v20.11.0 / npm 10.4.0** (pinned via `frontend-maven-plugin`)
- **ArchUnit 1.3.0** for architecture compliance, **JUnit 5** + **REST Assured** for testing
