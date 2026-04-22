# Phoniebox

A music player application designed to run on a Raspberry Pi. 

## Prerequisites

- **Java 21**
- **Maven 3.9+**
- **Node.js 20 / npm 10** — only needed for standalone frontend work; Maven downloads them automatically during a full build

## Building

```bash
# Full build (Java + frontend), skip tests
mvn clean install -DskipTests

# Full build with tests
mvn clean install

# Native image (requires GraalVM with native-image)
mvn clean install -Pnative
```

The frontend (`phoniebox-frontend`) is compiled by Maven as part of the normal build. Its output is written directly into `phoniebox-app/src/main/resources/META-INF/resources` and bundled into the application JAR.

## Running

```bash
# Start the backend in dev mode (hot reload on port 8080)
cd phoniebox-app && mvn quarkus:dev
```

The application is then available at `http://localhost:8080`.

By default it creates:
- `./phoniebox.db` — SQLite database (override with `-Dphoniebox.database.path=…`)
- `./media-storage/` — uploaded file storage (override with `-Dphoniebox.media.storage-path=…`)

## Frontend Development

For rapid frontend iteration, run the Vite dev server alongside the Quarkus backend:

```bash
# Terminal 1 — backend
cd phoniebox-app && mvn quarkus:dev

# Terminal 2 — frontend dev server (port 5173, hot module replacement)
cd phoniebox-frontend && npm install && npm run dev
```

The Vite dev server proxies all `/api/*` requests to `http://localhost:8080`, so the frontend at `http://localhost:5173` talks to the real backend. CORS is automatically enabled in Quarkus dev mode.

When you're done with frontend changes, a `mvn clean install` will rebuild the SPA and embed it in the JAR.

## Testing

```bash
# Unit tests only
mvn clean test

# Unit + integration tests
mvn clean verify

# Single test class
mvn test -Dtest=ArchitectureTest

# Tests in a specific module
mvn test -pl phoniebox-media
```

Architecture compliance is enforced automatically: any layer-boundary violation causes the ArchUnit tests to fail.
