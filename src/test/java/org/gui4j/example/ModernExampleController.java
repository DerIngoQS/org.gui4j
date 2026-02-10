package org.gui4j.example;

import java.util.Objects;
import java.util.function.Supplier;
import org.gui4j.Gui4j;
import org.gui4j.Gui4jController;
import org.gui4j.Gui4jWindowClosingHandler;
import org.gui4j.exception.Gui4jExceptionHandler;

/**
 * Minimal controller for the XML example view using modern Java style (immutable state + lambda
 * callbacks).
 */
public final class ModernExampleController implements Gui4jController {
  private final Gui4j gui4j;
  private final Gui4jExceptionHandler exceptionHandler;
  private final Gui4jWindowClosingHandler onWindowClosing;
  private final Runnable onWindowClosed;
  private final Supplier<String> messageSupplier;

  public ModernExampleController(
      Gui4j gui4j,
      Gui4jExceptionHandler exceptionHandler,
      Gui4jWindowClosingHandler onWindowClosing,
      Runnable onWindowClosed,
      Supplier<String> messageSupplier) {
    this.gui4j = gui4j;
    this.exceptionHandler = exceptionHandler;
    this.onWindowClosing = Objects.requireNonNull(onWindowClosing, "onWindowClosing");
    this.onWindowClosed = Objects.requireNonNull(onWindowClosed, "onWindowClosed");
    this.messageSupplier = Objects.requireNonNull(messageSupplier, "messageSupplier");
  }

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

  public String getTitle() {
    return "Modern gui4j Example";
  }

  public String getMessage() {
    return messageSupplier.get();
  }
}
