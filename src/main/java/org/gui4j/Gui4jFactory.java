package org.gui4j;

import java.lang.reflect.Constructor;
import java.net.URL;
import org.apache.commons.logging.LogFactory;
import org.gui4j.constants.Const;

/** Main class to construct the Gui4j instance. */
public class Gui4jFactory {
  /**
   * @param validateXML validate the xml file
   * @param logInvoke log all method invocation (very time consuming)
   * @param numberOfWorkerThreads maximal number of worker threads. Use -1, for an unrestricted
   *     count.
   * @param url the properties file containing the set of factory components. See
   *     gui4jComponent.properties in the documentation.
   * @return Gui4j
   */
  public static Gui4j createGui4j(
      boolean validateXML, boolean logInvoke, int numberOfWorkerThreads, URL url) {
    if (url == null) {
      throw new NullPointerException("URL must not be null");
    }
    try {
      Class<?> clazz = Class.forName("org.gui4j.core.impl.Gui4jImpl");
      Constructor<?> c =
          clazz.getDeclaredConstructor(Boolean.TYPE, Boolean.TYPE, Integer.TYPE, URL.class);
      // Gui4jImpl is package-private in org.gui4j.core.impl, so reflective access is required.
      c.setAccessible(true);
      Gui4j gui4j =
          (Gui4j)
              c.newInstance(
                  new Object[] {
                    Boolean.valueOf(validateXML),
                    Boolean.valueOf(logInvoke),
                    Integer.valueOf(numberOfWorkerThreads),
                    url
                  });
      LogFactory.getLog(Gui4jFactory.class)
          .info("Gui4j initialized (version " + Const.GUI4J_VERSION + ")");
      return gui4j;
    } catch (Exception e) {
      LogFactory.getLog(Gui4jFactory.class).fatal(e, e);
      throw new RuntimeException(e);
    }
  }

  public static Gui4jFactoryV2 v2(Gui4j gui4j) {
    return Gui4jFactoryV2.of(gui4j);
  }

  private Gui4jFactory() {}
}
