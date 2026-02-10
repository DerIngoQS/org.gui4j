package org.gui4j.example;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import org.gui4j.Gui4j;
import org.gui4j.Gui4jFactory;
import org.gui4j.Gui4jView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModernGui4jExampleTest {
  private Gui4j gui4j;

  @BeforeEach
  void setUp() {
    URL configUrl =
        ModernGui4jExampleTest.class.getResource("/org/gui4j/example/gui4j-components-minimal.properties");
    assertNotNull(configUrl, "Expected minimal gui4j component config in test resources");
    gui4j = Gui4jFactory.createGui4j(false, false, 1, configUrl);
  }

  @AfterEach
  void tearDown() {
    if (gui4j != null) {
      gui4j.shutdown();
    }
  }

  @Test
  void validatesAndBuildsViewWithModernController() {
    ModernExampleController controller =
        new ModernExampleController(gui4j, null, () -> true, () -> {}, () -> "Hello from test");

    assertTrue(gui4j.createValidator().validateResourceFile(ModernExampleController.class, "modern-view.xml"));

    Gui4jView view =
        Gui4jFactory.v2(gui4j)
            .loadView("modern-view.xml")
            .withController(controller)
            .withTitle("Test Title")
            .create();

    assertNotNull(view);
  }
}
