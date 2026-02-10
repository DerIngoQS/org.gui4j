package org.gui4j;

@FunctionalInterface
public interface Gui4jWindowClosingHandler {
  /**
   * @return <code>true</code> if the window should be closed, <code>false</code> otherwise.
   */
  boolean onWindowClosing();
}
