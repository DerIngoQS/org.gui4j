package org.gui4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface Gui4jValidator {
  /**
   * Reads the given resource file and checks if the syntax and all reflection calls are still
   * valid. This method can be used in JUnit test to ensure that XML resource files are valid.
   *
   * @param controllerClass the concrete controller type used for reflective method resolution
   * @param resourceName the name of the resource containing the Gui4j definitions
   * @return true if everything is ok
   */
  boolean validateResourceFile(Class<?> controllerClass, String resourceName);

  /**
   * Validates that every gui id in {@code ids} is present in the given resource.
   *
   * @param controllerClass the concrete controller type used for reflective method resolution
   * @param resourceName the name of the resource containing the Gui4j definitions
   * @param ids gui ids to check in order
   * @return {@code -1} if all ids are defined, otherwise the position in the given list of ids
   */
  int validateExistenceOfGuiIDs(Class<?> controllerClass, String resourceName, List<String> ids);

  /**
   * Convenience overload for validating a list of gui ids.
   *
   * @param controllerClass the controller for the resource file
   * @param resourceName the resource containing the Gui4j definitions
   * @param ids gui ids to verify
   * @return -1 if all ids are defined, otherwise the position in the given list of ids
   */
  default int validateExistenceOfGuiIDs(
      Class<?> controllerClass, String resourceName, String... ids) {
    return validateExistenceOfGuiIDs(controllerClass, resourceName, Arrays.asList(ids));
  }

  /**
   * Creates a validator from lambda callbacks.
   *
   * @param resourceValidator callback for resource validation
   * @param guiIdValidator callback for gui-id validation
   * @return validator instance
   */
  static Gui4jValidator of(
      final Gui4jResourceValidator resourceValidator, final Gui4jGuiIdValidator guiIdValidator) {
    Objects.requireNonNull(resourceValidator, "resourceValidator");
    Objects.requireNonNull(guiIdValidator, "guiIdValidator");
    return new Gui4jValidator() {
      public boolean validateResourceFile(Class<?> controllerClass, String resourceName) {
        return resourceValidator.validateResourceFile(controllerClass, resourceName);
      }

      public int validateExistenceOfGuiIDs(
          Class<?> controllerClass, String resourceName, List<String> ids) {
        return guiIdValidator.validateExistenceOfGuiIDs(controllerClass, resourceName, ids);
      }
    };
  }
}
