package org.gui4j;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.LNSAXReader;
import org.gui4j.core.Gui4jComponentContainer;
import org.gui4j.core.Gui4jComponentContainerManager;
import org.gui4j.core.Gui4jComponentManager;
import org.gui4j.core.Gui4jInternal;
import org.gui4j.core.Gui4jReflectionManager;

/**
 * Utility for validating gui4j XML resources without creating Swing windows.
 *
 * <p>The validator performs two independent checks:
 *
 * <ol>
 *   <li>Schema/DTD and top-level structure checks via the gui4j parsing pipeline.
 *   <li>Reflection checks for referenced controller methods.
 * </ol>
 */
public final class Gui4jXmlValidator {
  private static final Pattern METHOD_CALL_PATTERN =
      Pattern.compile("([A-Za-z_$][A-Za-z0-9_$]*)\\s*\\(");
  private static final Pattern SIMPLE_IDENTIFIER_PATTERN =
      Pattern.compile("^[A-Za-z_$][A-Za-z0-9_$]*$");

  private Gui4jXmlValidator() {}

  /**
   * Validates the given resources by creating a temporary gui4j runtime.
   *
   * @param componentConfigUrl URL to the gui4j component registration file
   * @param controllerClass controller class referenced by the XML resources
   * @param resourceNames resource names (relative to the controller package or absolute with '/').
   * @return combined validation report
   */
  public static ValidationReport validate(
      URL componentConfigUrl, Class<?> controllerClass, String... resourceNames) {
    return validate(componentConfigUrl, false, controllerClass, resourceNames);
  }

  /**
   * Validates the given resources by creating a temporary gui4j runtime.
   *
   * @param componentConfigUrl URL to the gui4j component registration file
   * @param strictXmlValidation if {@code true}, XML input is additionally validated against the
   *     configured grammar (DTD)
   * @param controllerClass controller class referenced by the XML resources
   * @param resourceNames resource names (relative to the controller package or absolute with '/').
   * @return combined validation report
   */
  public static ValidationReport validate(
      URL componentConfigUrl,
      boolean strictXmlValidation,
      Class<?> controllerClass,
      String... resourceNames) {
    Gui4j gui4j = Gui4jFactory.createGui4j(strictXmlValidation, false, 0, componentConfigUrl);
    try {
      return validate(gui4j, controllerClass, resourceNames);
    } finally {
      gui4j.shutdown();
    }
  }

  /**
   * Validates the given resources using an existing gui4j runtime.
   *
   * @param gui4j existing gui4j instance
   * @param controllerClass controller class referenced by the XML resources
   * @param resourceNames resource names (relative to the controller package or absolute with '/').
   * @return combined validation report
   */
  public static ValidationReport validate(
      Gui4j gui4j, Class<?> controllerClass, String... resourceNames) {
    if (gui4j == null) {
      throw new NullPointerException("gui4j must not be null");
    }
    if (controllerClass == null) {
      throw new NullPointerException("controllerClass must not be null");
    }
    if (resourceNames == null || resourceNames.length == 0) {
      throw new IllegalArgumentException("At least one resourceName is required");
    }

    Gui4jInternal internal = asInternal(gui4j);
    Gui4jReflectionManager reflectionManager = internal.getGui4jReflectionManager();

    List<ResourceValidation> validations = new ArrayList<ResourceValidation>();
    for (int i = 0; i < resourceNames.length; i++) {
      String resourceName = resourceNames[i];
      validations.add(validateSingle(gui4j, internal, reflectionManager, controllerClass, resourceName));
    }

    return new ValidationReport(validations);
  }

  private static Gui4jInternal asInternal(Gui4j gui4j) {
    if (gui4j instanceof Gui4jInternal) {
      return (Gui4jInternal) gui4j;
    }
    throw new IllegalArgumentException(
        "Unsupported Gui4j implementation; expected Gui4jInternal-compatible instance");
  }

  private static ResourceValidation validateSingle(
      Gui4j gui4j,
      Gui4jInternal internal,
      Gui4jReflectionManager reflectionManager,
      Class<?> controllerClass,
      String resourceName) {
    List<String> schemaErrors = new ArrayList<String>();
    List<String> reflectionErrors = new ArrayList<String>();

    validateSchema(internal, controllerClass, resourceName, schemaErrors);
    validateReflection(gui4j, reflectionManager, controllerClass, resourceName, reflectionErrors);

    return new ResourceValidation(resourceName, schemaErrors, reflectionErrors);
  }

  private static void validateSchema(
      Gui4jInternal internal,
      Class<?> controllerClass,
      String resourceName,
      List<String> schemaErrors) {
    try {
      String fullyQualifiedName = toFullyQualifiedResourceName(controllerClass, resourceName);
      Gui4jComponentContainer container =
          internal
              .getGui4jComponentContainerManager()
              .getGui4jComponentContainer(controllerClass, fullyQualifiedName);

      String top =
          container.getToplevelAttrValue(Gui4jComponentManager.FIELD_Gui4jViewTopComponent);
      top = top == null ? "TOP" : top;
      if (!container.isDefined(top)) {
        schemaErrors.add("Top component '" + top + "' is not defined in " + fullyQualifiedName);
      }

      String defaultButton =
          container.getToplevelAttrValue(Gui4jComponentManager.FIELD_Gui4jViewDefaultButton);
      if (defaultButton != null && !container.isDefined(defaultButton)) {
        schemaErrors.add(
            "Default button '"
                + defaultButton
                + "' is not defined in "
                + fullyQualifiedName);
      }
    } catch (Throwable t) {
      schemaErrors.add(t.getMessage() == null ? t.getClass().getName() : t.getMessage());
    }
  }

  private static void validateReflection(
      Gui4j gui4j,
      Gui4jReflectionManager reflectionManager,
      Class<?> controllerClass,
      String resourceName,
      List<String> reflectionErrors) {
    try {
      // Reuse the production parser/validator path that resolves method calls through
      // Gui4jReflectionManager.
      gui4j.createValidator().validateResourceFile(controllerClass, resourceName);
    } catch (Throwable t) {
      reflectionErrors.add(t.getMessage() == null ? t.getClass().getName() : t.getMessage());
    }

    try {
      Set<String> referencedMethods = collectReferencedControllerMethods(controllerClass, resourceName);
      for (String methodName : referencedMethods) {
        if (!reflectionManager.hasMethodName(controllerClass, methodName)) {
          reflectionErrors.add(
              "Referenced controller method not found: "
                  + controllerClass.getName()
                  + "."
                  + methodName
                  + "() in resource "
                  + resourceName);
        }
      }
    } catch (Throwable t) {
      reflectionErrors.add(
          "Static reflection scan failed for "
              + resourceName
              + ": "
              + (t.getMessage() == null ? t.getClass().getName() : t.getMessage()));
    }
  }

  private static Set<String> collectReferencedControllerMethods(
      Class<?> controllerClass, String resourceName) throws Exception {
    String fullyQualifiedName = toFullyQualifiedResourceName(controllerClass, resourceName);
    InputStream in = Gui4jXmlValidator.class.getResourceAsStream(fullyQualifiedName);
    if (in == null) {
      throw new IllegalArgumentException("Resource not found: " + fullyQualifiedName);
    }
    try {
      LNSAXReader reader = new LNSAXReader(false);
      Document document = reader.read(in, fullyQualifiedName);
      Set<String> references = new LinkedHashSet<String>();
      collectMethodCandidates((Element) document.getRootElement(), references);
      return references;
    } finally {
      in.close();
    }
  }

  private static void collectMethodCandidates(Element element, Set<String> references) {
    List<Attribute> attributes = element.attributes();
    for (int i = 0; i < attributes.size(); i++) {
      Attribute attr = attributes.get(i);
      addMethodCandidates(attr.getValue(), references);
    }

    List<Element> children = element.elements();
    for (int i = 0; i < children.size(); i++) {
      collectMethodCandidates(children.get(i), references);
    }
  }

  private static void addMethodCandidates(String value, Set<String> references) {
    if (value == null) {
      return;
    }

    Matcher callMatcher = METHOD_CALL_PATTERN.matcher(value);
    while (callMatcher.find()) {
      String candidate = callMatcher.group(1);
      if (looksLikeMethodReference(candidate)) {
        references.add(candidate);
      }
    }

    if (SIMPLE_IDENTIFIER_PATTERN.matcher(value).matches() && looksLikeMethodReference(value)) {
      references.add(value);
    }
  }

  private static boolean looksLikeMethodReference(String candidate) {
    if (candidate.length() < 2) {
      return false;
    }
    char first = candidate.charAt(0);
    if (!Character.isLowerCase(first)) {
      return false;
    }
    return candidate.startsWith("get")
        || candidate.startsWith("set")
        || candidate.startsWith("is")
        || candidate.startsWith("has")
        || candidate.startsWith("on")
        || candidate.startsWith("do")
        || candidate.startsWith("window")
        || candidate.startsWith("handle")
        || candidate.startsWith("validate")
        || candidate.startsWith("create");
  }

  private static String toFullyQualifiedResourceName(Class<?> controllerClass, String resourceName) {
    return Gui4jComponentContainerManager.getResourceNameFullyQuantified(
        Gui4jComponentContainerManager.getBaseName(controllerClass), resourceName);
  }

  /**
   * Command-line entry point.
   *
   * <p>Usage: {@code Gui4jXmlValidator <component-config-url-or-resource> <controller-class>
   * <resource1> [resource2 ...]}
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      System.err.println(
          "Usage: Gui4jXmlValidator <component-config-url-or-resource> <controller-class> <resource1> [resource2 ...]");
      System.exit(2);
      return;
    }

    URL configUrl = resolveUrl(args[0]);
    Class<?> controllerClass = Class.forName(args[1]);
    String[] resourceNames = Arrays.copyOfRange(args, 2, args.length);

    ValidationReport report = validate(configUrl, controllerClass, resourceNames);
    System.out.println(report.toHumanReadableString());
    System.exit(report.isValid() ? 0 : 1);
  }

  private static URL resolveUrl(String value) throws MalformedURLException {
    if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("file:")) {
      return new URL(value);
    }
    URL url = Gui4jXmlValidator.class.getResource(value.startsWith("/") ? value : "/" + value);
    if (url == null) {
      throw new IllegalArgumentException("Could not resolve component config URL: " + value);
    }
    return url;
  }

  public static final class ValidationReport {
    private final List<ResourceValidation> resources;

    ValidationReport(List<ResourceValidation> resources) {
      this.resources = Collections.unmodifiableList(new ArrayList<ResourceValidation>(resources));
    }

    public List<ResourceValidation> getResources() {
      return resources;
    }

    public boolean isValid() {
      for (int i = 0; i < resources.size(); i++) {
        if (!resources.get(i).isValid()) {
          return false;
        }
      }
      return true;
    }

    public String toHumanReadableString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Gui4jXmlValidator result: ").append(isValid() ? "VALID" : "INVALID").append('\n');
      for (int i = 0; i < resources.size(); i++) {
        ResourceValidation validation = resources.get(i);
        sb.append("- ").append(validation.resourceName).append(": ");
        sb.append(validation.isValid() ? "OK" : "ERROR").append('\n');
        appendIssues(sb, "schema", validation.schemaErrors);
        appendIssues(sb, "reflection", validation.reflectionErrors);
      }
      return sb.toString();
    }

    private void appendIssues(StringBuilder sb, String checkType, List<String> issues) {
      for (int i = 0; i < issues.size(); i++) {
        sb.append("  [").append(checkType).append("] ").append(issues.get(i)).append('\n');
      }
    }
  }

  public static final class ResourceValidation {
    private final String resourceName;
    private final List<String> schemaErrors;
    private final List<String> reflectionErrors;

    ResourceValidation(String resourceName, List<String> schemaErrors, List<String> reflectionErrors) {
      this.resourceName = resourceName;
      this.schemaErrors = Collections.unmodifiableList(new ArrayList<String>(schemaErrors));
      this.reflectionErrors =
          Collections.unmodifiableList(new ArrayList<String>(reflectionErrors));
    }

    public String getResourceName() {
      return resourceName;
    }

    public List<String> getSchemaErrors() {
      return schemaErrors;
    }

    public List<String> getReflectionErrors() {
      return reflectionErrors;
    }

    public boolean isSchemaValid() {
      return schemaErrors.isEmpty();
    }

    public boolean isReflectionValid() {
      return reflectionErrors.isEmpty();
    }

    public boolean isValid() {
      return isSchemaValid() && isReflectionValid();
    }
  }
}
