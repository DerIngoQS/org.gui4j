package org.gui4j.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.gui4j.Gui4j;
import org.gui4j.Gui4jCallBase;
import org.gui4j.core.Gui4jCall;
import org.gui4j.core.Gui4jComponent;
import org.gui4j.core.Gui4jComponentContainer;
import org.gui4j.core.Gui4jInternal;
import org.gui4j.core.call.Gui4jCallParser;
import org.gui4j.exception.Gui4jUncheckedException;
import org.junit.jupiter.api.Test;

class Gui4jCallParserTest {

  public interface TestService {
    String build(String prefix, int value);

    TestInnerService inner();
  }

  public interface TestInnerService {
    String value();
  }

  public interface TestController extends Gui4jCallBase {
    String doSave();

    TestService service();
  }

  @Test
  void resolvesSimpleMethodAccessPath() {
    TestController controller = mock(TestController.class);
    when(controller.getGui4j()).thenReturn(mock(Gui4j.class));
    when(controller.doSave()).thenReturn("saved");

    Gui4jCallParser parser = new Gui4jCallParser();
    Gui4jCall call = parser.getInstance(mockComponent(TestController.class), 10, "doSave");

    Object result = call.getValue(controller, Collections.emptyMap(), null);

    assertEquals("saved", result);
    verify(controller).doSave();
  }

  @Test
  void resolvesComplexChainedAccessPathWithParameters() {
    TestController controller = mock(TestController.class);
    when(controller.getGui4j()).thenReturn(mock(Gui4j.class));
    TestService service = mock(TestService.class);
    when(controller.service()).thenReturn(service);
    when(service.build("abc", 2)).thenReturn("abc2");

    Gui4jCallParser parser = new Gui4jCallParser();
    Gui4jCall call =
        parser.getInstance(mockComponent(TestController.class), 20, "service().build('abc',2).toUpperCase()");

    Object result = call.getValue(controller, Collections.emptyMap(), null);

    assertEquals("ABC2", result);
    verify(controller).service();
    verify(service).build("abc", 2);
  }

  @Test
  void resolvesSequenceAndReturnsLastExpressionResult() {
    TestController controller = mock(TestController.class);
    when(controller.getGui4j()).thenReturn(mock(Gui4j.class));
    when(controller.doSave()).thenReturn("ignored");

    TestService service = mock(TestService.class);
    TestInnerService inner = mock(TestInnerService.class);
    when(controller.service()).thenReturn(service);
    when(service.inner()).thenReturn(inner);
    when(inner.value()).thenReturn("final");

    Gui4jCallParser parser = new Gui4jCallParser();
    Gui4jCall call =
        parser.getInstance(
            mockComponent(TestController.class), 30, "doSave();service().inner().value()");

    Object result = call.getValue(controller, Collections.emptyMap(), null);

    assertEquals("final", result);
    verify(controller).doSave();
    verify(controller).service();
    verify(service).inner();
    verify(inner).value();
  }

  @Test
  void throwsResourceErrorForMalformedAccessPath() {
    Gui4jCallParser parser = new Gui4jCallParser();

    assertThrows(
        Gui4jUncheckedException.ResourceError.class,
        () -> parser.getInstance(mockComponent(TestController.class), 40, "doSave("));
  }

  private Gui4jComponent mockComponent(Class controllerClass) {
    Gui4jComponent component = mock(Gui4jComponent.class);
    Gui4jComponentContainer container = mock(Gui4jComponentContainer.class);
    Gui4jInternal gui4j = mock(Gui4jInternal.class);

    when(component.getGui4jComponentContainer()).thenReturn(container);
    when(container.getGui4jControllerClass()).thenReturn(controllerClass);
    when(container.getConfigurationName()).thenReturn("/test/view.xml");
    when(container.getGui4j()).thenReturn(gui4j);
    when(gui4j.getGui4jReflectionManager())
        .thenReturn(org.gui4j.core.Gui4jReflectionManager.getNewInstance());

    return component;
  }
}
