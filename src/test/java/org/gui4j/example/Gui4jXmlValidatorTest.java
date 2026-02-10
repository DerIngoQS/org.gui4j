package org.gui4j.example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import org.gui4j.Gui4jXmlValidator;
import org.junit.jupiter.api.Test;

class Gui4jXmlValidatorTest {

  @Test
  void validatesExistingXmlAndController() {
    URL configUrl =
        Gui4jXmlValidatorTest.class.getResource(
            "/org/gui4j/example/gui4j-components-minimal.properties");
    assertNotNull(configUrl);

    Gui4jXmlValidator.ValidationReport report =
        Gui4jXmlValidator.validate(configUrl, ModernExampleController.class, "modern-view.xml");

    assertTrue(report.isValid());
    assertTrue(report.getResources().get(0).isSchemaValid());
    assertTrue(report.getResources().get(0).isReflectionValid());
  }

  @Test
  void reportsMissingControllerMethods() {
    URL configUrl =
        Gui4jXmlValidatorTest.class.getResource(
            "/org/gui4j/example/gui4j-components-minimal.properties");
    assertNotNull(configUrl);

    Gui4jXmlValidator.ValidationReport report =
        Gui4jXmlValidator.validate(
            configUrl, ModernExampleController.class, "modern-view-missing-method.xml");

    assertFalse(report.isValid());
    assertTrue(report.getResources().get(0).isSchemaValid());
    assertFalse(report.getResources().get(0).isReflectionValid());
  }
}
