# Migration Guide (1.2.x -> 1.3)

This guide summarizes API changes introduced for the 1.3 modernization work.

## 1. Controller and Validator API (Lambda-friendly)

### New functional types
- `org.gui4j.Gui4jWindowClosingHandler`
- `org.gui4j.Gui4jResourceValidator`
- `org.gui4j.Gui4jGuiIdValidator`

### New adapter factory methods
- `Gui4jController.of(...)`
- `Gui4jValidator.of(...)`

These APIs allow concise callback wiring while preserving compatibility with existing controller implementations.

## 2. Optional Fluent API (v2)

A new optional fluent entrypoint is available via:

- `Gui4jFactory.v2(gui4j)`

Example:

```java
Gui4jFactory.v2(gui4j)
    .loadView("modern-view.xml")
    .withController(controller)
    .show();
```

This API is additive; existing `Gui4j#createView(...)` usage remains valid.

## 3. Legacy Window Facades

The legacy convenience methods in `Gui4jWindow` now remain as explicit facades to modern calls:

- `show()` -> `setVisible(true)`
- `hide()` -> `setVisible(false)`
- `disable()` -> `setEnabled(false)`
- `enable()` -> `setEnabled(true)`

These methods stay source-compatible and are now clearly marked as deprecated in favor of the explicit Swing-style APIs.

## 4. Manual Thread Helpers

The following static helpers in `Gui4jThreadManager` are deprecated in favor of direct AWT/Swing APIs:

- `executeInSwingThreadAndWait(Runnable)`
- `executeInSwingThreadAndContinue(Runnable)`

Preferred replacements:
- `EventQueue.invokeAndWait(...)`
- `EventQueue.invokeLater(...)`
- optional explicit EDT checks via `SwingUtilities.isEventDispatchThread()`

`Gui4jWindow.waitForGUI()` is also deprecated; prefer explicit EDT coordination in calling code.

## 5. Java 8 compatibility note

The project is compiled with `--release 8`. Therefore Java 9+ deprecation annotation attributes (`since`, `forRemoval`) cannot be used in source code without breaking compilation.

To preserve Java 8 compatibility, deprecations are currently expressed as:
- `@Deprecated`
- JavaDoc `@deprecated` text with explicit "Since 1.3" guidance

## 6. Example

A runnable example is provided under:

- `src/test/java/org/gui4j/example/ModernGui4jExampleApp.java`
- `src/test/java/org/gui4j/example/ModernExampleController.java`
- `src/test/resources/org/gui4j/example/modern-view.xml`
- `src/test/resources/org/gui4j/example/gui4j-components-minimal.properties`

A validating test is provided under:

- `src/test/java/org/gui4j/example/ModernGui4jExampleTest.java`
