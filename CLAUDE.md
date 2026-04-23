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
cd phoniebox-app && mvn quarkus:dev

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
| `phoniebox-audio` | Placeholder — future vlcj/libVLC audio playback |
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
- **ORM**: Hibernate Panache (repository pattern).
- **File storage**: Media files stored on local filesystem at `./media-storage` (configurable via `phoniebox.media.storage-path`).

### Tech Stack

- **Java 21**, Quarkus 3.22.3 (`quarkus-rest`, `quarkus-hibernate-orm-panache`, `quarkus-flyway`, `quarkus-jdbc-sqlite`)
- **Vue 3.4**, Vue Router 4.3, TypeScript 5.4, Vite 5.2, Tailwind CSS 3.4
- **Node v20.11.0 / npm 10.4.0** (pinned via `frontend-maven-plugin`)
- **ArchUnit 1.3.0** for architecture compliance, **JUnit 5** + **REST Assured** for testing
