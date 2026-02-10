package org.gui4j;

import java.util.Objects;
import org.gui4j.exception.Gui4jExceptionHandler;

public interface Gui4jController extends Gui4jCallBase {
  /**
   * This method is called when the user closes the window (e.g. by clicking on the X-Button or by
   * Pressing Alt-F4).
   *
   * @return boolean if <code>false</code> the window will not close, if <code>true</code> the
   *     window will close.
   */
  boolean onWindowClosing();

  /** This method is called after the window has been closed. */
  default void windowClosed() {}

  /**
   * Creates a controller from lambda callbacks.
   *
   * @param onWindowClosing callback for the close decision
   * @return controller instance
   */
  static Gui4jController of(final Gui4jWindowClosingHandler onWindowClosing) {
    return of(
        null,
        null,
        onWindowClosing,
        new Runnable() {
          public void run() {}
        });
  }

  /**
   * Creates a controller from lambda callbacks.
   *
   * @param gui4j gui4j runtime to expose through {@link Gui4jCallBase#getGui4j()}
   * @param exceptionHandler handler to expose through {@link Gui4jCallBase#getExceptionHandler()}
   * @param onWindowClosing callback for the close decision
   * @return controller instance
   */
  static Gui4jController of(
      final Gui4j gui4j,
      final Gui4jExceptionHandler exceptionHandler,
      final Gui4jWindowClosingHandler onWindowClosing) {
    return of(
        gui4j,
        exceptionHandler,
        onWindowClosing,
        new Runnable() {
          public void run() {}
        });
  }

  /**
   * Creates a controller from lambda callbacks.
   *
   * @param onWindowClosing callback for the close decision
   * @param onWindowClosed callback after close
   * @return controller instance
   */
  static Gui4jController of(
      final Gui4jWindowClosingHandler onWindowClosing, final Runnable onWindowClosed) {
    return of(null, null, onWindowClosing, onWindowClosed);
  }

  /**
   * Creates a controller from lambda callbacks.
   *
   * @param gui4j gui4j runtime to expose through {@link Gui4jCallBase#getGui4j()}
   * @param exceptionHandler handler to expose through {@link Gui4jCallBase#getExceptionHandler()}
   * @param onWindowClosing callback for the close decision
   * @param onWindowClosed callback after close
   * @return controller instance
   */
  static Gui4jController of(
      final Gui4j gui4j,
      final Gui4jExceptionHandler exceptionHandler,
      final Gui4jWindowClosingHandler onWindowClosing,
      final Runnable onWindowClosed) {
    Objects.requireNonNull(onWindowClosing, "onWindowClosing");
    Objects.requireNonNull(onWindowClosed, "onWindowClosed");
    return new Gui4jController() {
      public Gui4j getGui4j() {
        return gui4j;
      }

      public Gui4jExceptionHandler getExceptionHandler() {
        return exceptionHandler;
      }

      public boolean onWindowClosing() {
        return onWindowClosing.onWindowClosing();
      }

      public void windowClosed() {
        onWindowClosed.run();
      }
    };
  }
}
