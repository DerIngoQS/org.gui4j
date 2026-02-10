package org.gui4j;

import java.util.Objects;

/** Optional fluent entrypoint for loading Gui4j views. */
public final class Gui4jFactoryV2 {
  private final Gui4j gui4j;

  private Gui4jFactoryV2(Gui4j gui4j) {
    this.gui4j = Objects.requireNonNull(gui4j, "gui4j");
  }

  public static Gui4jFactoryV2 of(Gui4j gui4j) {
    return new Gui4jFactoryV2(gui4j);
  }

  public ViewLoader loadView(String viewResourceName) {
    return new ViewLoader(gui4j, viewResourceName);
  }

  public static final class ViewLoader {
    private final Gui4j gui4j;
    private final String viewResourceName;
    private Gui4jController controller;
    private String title;
    private boolean readOnlyMode;

    private ViewLoader(Gui4j gui4j, String viewResourceName) {
      this.gui4j = gui4j;
      this.viewResourceName = Objects.requireNonNull(viewResourceName, "viewResourceName");
    }

    public ViewLoader withController(Gui4jController controller) {
      this.controller = Objects.requireNonNull(controller, "controller");
      return this;
    }

    public ViewLoader withController(Gui4jWindowClosingHandler onWindowClosing) {
      this.controller = Gui4jController.of(onWindowClosing);
      return this;
    }

    public ViewLoader withController(
        Gui4jWindowClosingHandler onWindowClosing, Runnable onWindowClosed) {
      this.controller = Gui4jController.of(onWindowClosing, onWindowClosed);
      return this;
    }

    public ViewLoader withTitle(String title) {
      this.title = title;
      return this;
    }

    public ViewLoader readOnly() {
      this.readOnlyMode = true;
      return this;
    }

    public ViewLoader editable() {
      this.readOnlyMode = false;
      return this;
    }

    public ViewLoader readOnly(boolean readOnlyMode) {
      this.readOnlyMode = readOnlyMode;
      return this;
    }

    public Gui4jView create() {
      if (controller == null) {
        throw new IllegalStateException("No controller configured. Use withController(...)");
      }
      return gui4j.createView(viewResourceName, controller, title, readOnlyMode);
    }

    public Gui4jView show() {
      Gui4jView view = create();
      view.prepare();
      view.setVisible(true);
      return view;
    }
  }
}
