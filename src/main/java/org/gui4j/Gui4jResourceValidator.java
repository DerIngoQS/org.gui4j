package org.gui4j;

@FunctionalInterface
public interface Gui4jResourceValidator {
  /**
   * @param controllerClass the concrete controller type used for reflective method resolution
   * @param resourceName resource path relative to the controller package (or absolute with leading
   *     slash)
   * @return true if the resource is valid
   */
  boolean validateResourceFile(Class<?> controllerClass, String resourceName);
}
