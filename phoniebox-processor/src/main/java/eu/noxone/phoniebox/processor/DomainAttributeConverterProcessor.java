package eu.noxone.phoniebox.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotation processor that automatically generates JPA {@code @Converter} classes
 * for concrete {@code DefaultDomainAttribute<T>} subclasses whose wrapped type
 * maps directly to a standard JDBC column type.
 *
 * <h2>What gets generated</h2>
 * <p>For every non-abstract class {@code Foo extends DefaultDomainAttribute<T>}
 * found in a {@code *.domain.model*} package where {@code T} is a known simple
 * type ({@code String}, {@code Long}, {@code Integer}, …), the processor emits:
 * <pre>
 * {@literal @}Converter(autoApply = true)
 * public class FooConverter
 *         extends ReflectiveDomainAttributeConverter&lt;Foo, T&gt; {}
 * </pre>
 * into the corresponding {@code *.infrastructure.persistence} package.
 *
 * <h2>What is skipped</h2>
 * <p>Types whose wrapped type is not a simple JDBC type (e.g. {@code UUID},
 * {@code Instant}) are skipped.  A {@code NOTE} diagnostic is emitted for each
 * skipped type as a reminder to write a manual converter.
 *
 * <h2>Activation</h2>
 * <p>Declare {@code phoniebox-processor} as a {@code provided} dependency in any
 * feature module.  javac discovers the processor automatically via the SPI entry
 * in {@code META-INF/services/javax.annotation.processing.Processor}.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class DomainAttributeConverterProcessor extends AbstractProcessor {

    private static final String DEFAULT_DOMAIN_ATTRIBUTE =
            "eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute";
    private static final String REFLECTIVE_CONVERTER =
            "eu.noxone.phoniebox.shared.persistence.ReflectiveDomainAttributeConverter";

    /**
     * Wrapped types that map directly to a JDBC column type.
     * The value is the unqualified boxed type name used in the generated source.
     * All other wrapped types (UUID, Instant, …) are skipped.
     */
    private static final Map<String, String> SIMPLE_TYPES = Map.ofEntries(
            Map.entry("java.lang.String",  "String"),
            Map.entry("java.lang.Long",    "Long"),
            Map.entry("long",              "Long"),
            Map.entry("java.lang.Integer", "Integer"),
            Map.entry("int",               "Integer"),
            Map.entry("java.lang.Double",  "Double"),
            Map.entry("double",            "Double"),
            Map.entry("java.lang.Float",   "Float"),
            Map.entry("float",             "Float"),
            Map.entry("java.lang.Boolean", "Boolean"),
            Map.entry("boolean",           "Boolean"),
            Map.entry("java.lang.Short",   "Short"),
            Map.entry("short",             "Short"),
            Map.entry("java.lang.Byte",    "Byte"),
            Map.entry("byte",              "Byte")
    );

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        final TypeElement defaultDomainAttribute =
                processingEnv.getElementUtils().getTypeElement(DEFAULT_DOMAIN_ATTRIBUTE);
        if (defaultDomainAttribute == null) {
            // phoniebox-shared not on classpath — nothing to do in this module
            return false;
        }

        final TypeMirror erasedDDA =
                processingEnv.getTypeUtils().erasure(defaultDomainAttribute.asType());

        for (final Element element : roundEnv.getRootElements()) {
            if (element.getKind() != ElementKind.CLASS) continue;
            final TypeElement typeElement = (TypeElement) element;
            if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) continue;

            final TypeMirror erased =
                    processingEnv.getTypeUtils().erasure(typeElement.asType());
            if (!processingEnv.getTypeUtils().isSubtype(erased, erasedDDA)) continue;

            final TypeMirror wrappedType = findWrappedType(typeElement, defaultDomainAttribute);
            if (wrappedType == null) continue;

            final String wrappedTypeName = wrappedType.toString();
            final String boxedTypeName = SIMPLE_TYPES.get(wrappedTypeName);
            if (boxedTypeName == null) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "Skipping converter generation for " + typeElement.getQualifiedName()
                                + ": wrapped type '" + wrappedTypeName + "' is not a simple JDBC type."
                                + " Write a DomainAttributeConverter subclass manually.",
                        typeElement);
                continue;
            }

            generateConverter(typeElement, boxedTypeName);
        }

        return false;
    }

    /**
     * Walks the supertype chain until it finds {@code DefaultDomainAttribute<T>}
     * and returns the resolved type argument {@code T}.
     * Returns {@code null} if the chain does not reach {@code DefaultDomainAttribute}.
     */
    private TypeMirror findWrappedType(final TypeElement typeElement,
                                        final TypeElement target) {
        TypeMirror superType = typeElement.getSuperclass();
        while (superType != null && superType.getKind() == TypeKind.DECLARED) {
            final DeclaredType declaredType = (DeclaredType) superType;
            final TypeElement superTypeElement = (TypeElement) declaredType.asElement();

            if (superTypeElement.getQualifiedName().contentEquals(target.getQualifiedName())) {
                final List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
                return typeArgs.isEmpty() ? null : typeArgs.get(0);
            }

            superType = superTypeElement.getSuperclass();
        }
        return null;
    }

    private void generateConverter(final TypeElement typeElement, final String boxedDbTypeName) {
        final Element enclosing = typeElement.getEnclosingElement();
        if (enclosing.getKind() != ElementKind.PACKAGE) {
            return; // nested / inner class — skip
        }
        final String originalPackage =
                ((PackageElement) enclosing).getQualifiedName().toString();

        // eu.noxone.phoniebox.media.domain.model  →  eu.noxone.phoniebox.media.infrastructure.persistence
        final String infraPackage =
                originalPackage.replaceFirst("\\.domain(\\..+)?$", ".infrastructure.persistence");
        if (infraPackage.equals(originalPackage)) {
            return; // package doesn't follow the *.domain[.*] convention — skip
        }

        final String simpleName  = typeElement.getSimpleName().toString();
        final String qualifiedName = typeElement.getQualifiedName().toString();
        final String converterName = simpleName + "Converter";

        try {
            final JavaFileObject file = processingEnv.getFiler()
                    .createSourceFile(infraPackage + "." + converterName, typeElement);
            try (final Writer writer = file.openWriter()) {
                writer.write(renderSource(infraPackage, simpleName, qualifiedName,
                        boxedDbTypeName, converterName));
            }
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Generated " + infraPackage + "." + converterName,
                    typeElement);
        } catch (final IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to generate " + converterName + ": " + e.getMessage(),
                    typeElement);
        }
    }

    private static String renderSource(final String infraPackage,
                                        final String simpleName,
                                        final String qualifiedName,
                                        final String dbTypeName,
                                        final String converterName) {
        return "package " + infraPackage + ";\n"
                + "\n"
                + "import " + qualifiedName + ";\n"
                + "import " + REFLECTIVE_CONVERTER + ";\n"
                + "import jakarta.persistence.Converter;\n"
                + "\n"
                + "/**\n"
                + " * Generated JPA converter for {@link " + simpleName + "}.\n"
                + " *\n"
                + " * <p>Do not edit — regenerated automatically from\n"
                + " * {@link " + qualifiedName + "} on every build.\n"
                + " * To customise conversion logic, delete this class and write a\n"
                + " * {@link eu.noxone.phoniebox.shared.persistence.DomainAttributeConverter}\n"
                + " * subclass manually.\n"
                + " */\n"
                + "@Converter(autoApply = true)\n"
                + "public class " + converterName + "\n"
                + "        extends ReflectiveDomainAttributeConverter<"
                + simpleName + ", " + dbTypeName + "> {}\n";
    }
}
