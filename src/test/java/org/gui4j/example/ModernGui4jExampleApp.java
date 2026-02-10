package org.gui4j.example;

import java.awt.GraphicsEnvironment;
import java.net.URL;
import org.gui4j.Gui4j;
import org.gui4j.Gui4jFactory;

/** Standalone example that can be started from the command line. */
public final class ModernGui4jExampleApp {
  private ModernGui4jExampleApp() {}

  public static void main(String[] args) {
    URL configUrl =
        ModernGui4jExampleApp.class.getResource("/org/gui4j/example/gui4j-components-minimal.properties");
    if (configUrl == null) {
      throw new IllegalStateException("Missing gui4j component config for example");
    }

    Gui4j gui4j = Gui4jFactory.createGui4j(false, false, 1, configUrl);
    try {
      ModernExampleController controller =
          new ModernExampleController(
              gui4j,
              null,
              () -> true,
              () -> System.out.println("Window closed"),
              () -> "Hello from modern gui4j controller");

      if (GraphicsEnvironment.isHeadless()) {
        gui4j.createValidator().validateResourceFile(ModernExampleController.class, "modern-view.xml");
        System.out.println("Headless mode: XML view validated successfully.");
        return;
      }

      Gui4jFactory.v2(gui4j)
          .loadView("modern-view.xml")
          .withController(controller)
          .show();
    } finally {
      gui4j.shutdown();
    }
  }
}
