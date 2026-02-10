package org.gui4j;

import java.util.List;

@FunctionalInterface
public interface Gui4jGuiIdValidator {
  /**
   * @param controllerClass the concrete controller type used for reflective method resolution
   * @param resourceName resource path relative to the controller package (or absolute with leading
   *     slash)
   * @param ids gui ids to check in order
   * @return {@code -1} if all ids are defined, otherwise the index of the first missing id
   */
  int validateExistenceOfGuiIDs(Class<?> controllerClass, String resourceName, List<String> ids);
}
