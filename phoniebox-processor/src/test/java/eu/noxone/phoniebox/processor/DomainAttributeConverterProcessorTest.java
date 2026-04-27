package eu.noxone.phoniebox.processor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link DomainAttributeConverterProcessor}.
 *
 * <p>Each test compiles a small fixture source string in-process using {@link JavaCompiler}, with
 * the processor explicitly registered, then asserts on the generated source files and/or compiler
 * diagnostics.
 *
 * <p>The test classpath (including {@code phoniebox-shared} and {@code jakarta.persistence-api}) is
 * forwarded to the in-process compilation so that generated converter sources compile cleanly.
 */
class DomainAttributeConverterProcessorTest {

  @TempDir Path workDir;

  // ── Fixtures ──────────────────────────────────────────────────────────────

  /** A concrete DefaultDomainAttribute wrapping String — should generate a converter. */
  private static final String TITLE_SOURCE =
      """
      package eu.noxone.test.domain.model;
      import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;
      public final class Title extends DefaultDomainAttribute<String> {
          private Title(String v) { super(v); }
          public static Title of(String v) { return new Title(v); }
      }
      """;

  /**
   * A concrete DefaultDomainAttribute wrapping Long — should generate a converter with boxed Long.
   */
  private static final String FILE_SIZE_SOURCE =
      """
      package eu.noxone.test.domain.model;
      import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;
      public final class FileSize extends DefaultDomainAttribute<Long> {
          private FileSize(long v) { super(v); }
          public static FileSize of(long v) { return new FileSize(v); }
      }
      """;

  /** Abstract class — must NOT generate a converter. */
  private static final String ABSTRACT_ATTR_SOURCE =
      """
      package eu.noxone.test.domain.model;
      import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;
      public abstract class AbstractAttr extends DefaultDomainAttribute<String> {
          protected AbstractAttr(String v) { super(v); }
      }
      """;

  /** Wraps UUID — complex type, must NOT generate a converter; should emit a NOTE. */
  private static final String ENTITY_ID_SOURCE =
      """
      package eu.noxone.test.domain.model;
      import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;
      import java.util.UUID;
      public final class EntityId extends DefaultDomainAttribute<UUID> {
          private EntityId(UUID v) { super(v); }
          public static EntityId of(UUID v) { return new EntityId(v); }
      }
      """;

  /** Lives outside *.domain.* — must NOT generate a converter. */
  private static final String WRONG_PACKAGE_SOURCE =
      """
      package eu.noxone.test.util;
      import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;
      public final class Helper extends DefaultDomainAttribute<String> {
          private Helper(String v) { super(v); }
          public static Helper of(String v) { return new Helper(v); }
      }
      """;

  // ── Tests ─────────────────────────────────────────────────────────────────

  @Test
  void generatesConverter_forStringAttribute() throws IOException {
    CompilationResult result = compile(TITLE_SOURCE);

    assertTrue(result.success(), "Compilation should succeed");
    assertTrue(
        Files.exists(
            result.generatedSource("eu.noxone.test.infrastructure.persistence", "TitleConverter")),
        "TitleConverter.java should be generated");
  }

  @Test
  void generatesConverter_forLongAttribute_withBoxedType() throws IOException {
    CompilationResult result = compile(FILE_SIZE_SOURCE);

    assertTrue(result.success(), "Compilation should succeed");
    Path generated =
        result.generatedSource("eu.noxone.test.infrastructure.persistence", "FileSizeConverter");
    assertTrue(Files.exists(generated), "FileSizeConverter.java should be generated");
    assertTrue(
        Files.readString(generated).contains("ReflectiveDomainAttributeConverter<FileSize, Long>"),
        "Should use boxed Long as the DB type parameter");
  }

  @Test
  void skipsAbstractClass() throws IOException {
    CompilationResult result = compile(ABSTRACT_ATTR_SOURCE);

    assertTrue(result.success(), "Compilation should succeed");
    assertFalse(
        Files.exists(
            result.generatedSource(
                "eu.noxone.test.infrastructure.persistence", "AbstractAttrConverter")),
        "No converter should be generated for an abstract class");
  }

  @Test
  void skipsComplexWrappedType_andEmitsNote() throws IOException {
    CompilationResult result = compile(ENTITY_ID_SOURCE);

    assertTrue(result.success(), "Compilation should succeed");
    assertFalse(
        Files.exists(
            result.generatedSource(
                "eu.noxone.test.infrastructure.persistence", "EntityIdConverter")),
        "No converter should be generated for a UUID-wrapped type");
    assertTrue(
        result.diagnostics().stream()
            .anyMatch(
                d ->
                    d.getKind() == Diagnostic.Kind.NOTE
                        && d.getMessage(null).contains("EntityId")
                        && d.getMessage(null).contains("java.util.UUID")),
        "A NOTE diagnostic should name the skipped type and its wrapped type");
  }

  @Test
  void skipsClassOutsideDomainPackage() throws IOException {
    CompilationResult result = compile(WRONG_PACKAGE_SOURCE);

    assertTrue(result.success(), "Compilation should succeed");
    assertFalse(
        Files.exists(
            result.generatedSource("eu.noxone.test.infrastructure.persistence", "HelperConverter")),
        "No converter should be generated for a class outside *.domain.* packages");
  }

  @Test
  void generatedConverter_hasCorrectContent() throws IOException {
    CompilationResult result = compile(TITLE_SOURCE);

    String content =
        Files.readString(
            result.generatedSource("eu.noxone.test.infrastructure.persistence", "TitleConverter"));

    assertAll(
        "generated TitleConverter content",
        () ->
            assertTrue(
                content.contains("package eu.noxone.test.infrastructure.persistence;"),
                "package declaration"),
        () ->
            assertTrue(
                content.contains("import eu.noxone.test.domain.model.Title;"),
                "import of domain attribute type"),
        () ->
            assertTrue(
                content.contains("import jakarta.persistence.Converter;"), "import of @Converter"),
        () ->
            assertTrue(
                content.contains("@Converter(autoApply = true)"),
                "@Converter(autoApply = true) annotation"),
        () -> assertTrue(content.contains("public class TitleConverter"), "class name"),
        () ->
            assertTrue(
                content.contains("extends ReflectiveDomainAttributeConverter<Title, String>"),
                "extends ReflectiveDomainAttributeConverter with correct type parameters"));
  }

  // ── Infrastructure ────────────────────────────────────────────────────────

  /**
   * Compiles the given Java source strings in a temporary directory using {@link
   * DomainAttributeConverterProcessor} as the sole annotation processor. Returns the success flag,
   * captured diagnostics, and a helper for locating generated source files.
   */
  private CompilationResult compile(String... sources) throws IOException {
    Path srcDir = workDir.resolve("src");
    Path genDir = workDir.resolve("generated-sources");
    Path classDir = workDir.resolve("classes");
    Files.createDirectories(genDir);
    Files.createDirectories(classDir);

    List<Path> sourcePaths = new ArrayList<>();
    for (String src : sources) {
      String pkg = extractPackage(src);
      String cls = extractClassName(src);
      Path pkgDir = srcDir.resolve(pkg.replace('.', '/'));
      Files.createDirectories(pkgDir);
      Path file = pkgDir.resolve(cls + ".java");
      Files.writeString(file, src, StandardCharsets.UTF_8);
      sourcePaths.add(file);
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    try (StandardJavaFileManager fm =
        compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {

      fm.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(classDir));
      fm.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(genDir));

      // Forward the current test classpath so generated sources can resolve
      // ReflectiveDomainAttributeConverter, @Converter, etc.
      List<String> options = List.of("-classpath", System.getProperty("java.class.path"));

      JavaCompiler.CompilationTask task =
          compiler.getTask(
              new StringWriter(),
              fm,
              diagnostics,
              options,
              null,
              fm.getJavaFileObjectsFromPaths(sourcePaths));

      task.setProcessors(List.of(new DomainAttributeConverterProcessor()));
      boolean success = task.call();

      return new CompilationResult(success, diagnostics.getDiagnostics(), genDir);
    }
  }

  private static String extractPackage(String source) {
    Matcher m =
        Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE).matcher(source);
    return m.find() ? m.group(1) : "";
  }

  private static String extractClassName(String source) {
    Matcher m =
        Pattern.compile("(?:public\\s+)?(?:abstract\\s+)?(?:final\\s+)?class\\s+(\\w+)")
            .matcher(source);
    return m.find() ? m.group(1) : "Unknown";
  }

  private record CompilationResult(
      boolean success,
      List<Diagnostic<? extends JavaFileObject>> diagnostics,
      Path generatedSourcesDir) {

    /** Resolves the path where the processor would write {@code pkg.className}. */
    Path generatedSource(String pkg, String className) {
      return generatedSourcesDir.resolve(pkg.replace('.', '/')).resolve(className + ".java");
    }
  }
}
