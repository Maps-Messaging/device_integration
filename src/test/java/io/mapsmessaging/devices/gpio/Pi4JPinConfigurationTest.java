package io.mapsmessaging.devices.gpio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputProvider;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputProvider;
import com.pi4j.io.gpio.digital.DigitalState;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class Pi4JPinConfigurationTest {

  @Test
  void legacyAddressPreservesInputPinWithoutMutatingProperties() {
    AtomicReference<DigitalInputConfig> captured = new AtomicReference<>();
    DigitalInputProvider input = (DigitalInputProvider) Proxy.newProxyInstance(DigitalInputProvider.class.getClassLoader(),
        new Class<?>[] {DigitalInputProvider.class}, (proxy, method, args) -> {
          DigitalInputConfig config = (DigitalInputConfig) args[0];
          captured.set(config);
          return pin(DigitalInput.class, config.bcm(), config.id(), config.name());
        });
    Map<String, String> properties = Map.of("address", "17", "id", "irq");
    new Pi4JPinManagement(context(input, null)).allocateInPin(properties);
    assertEquals(17, captured.get().bcm());
    assertEquals(Map.of("address", "17", "id", "irq"), properties);
  }

  @Test
  void legacyAddressPreservesOutputPinAndStates() {
    AtomicReference<DigitalOutputConfig> captured = new AtomicReference<>();
    DigitalOutputProvider output = (DigitalOutputProvider) Proxy.newProxyInstance(DigitalOutputProvider.class.getClassLoader(),
        new Class<?>[] {DigitalOutputProvider.class}, (proxy, method, args) -> {
          DigitalOutputConfig config = (DigitalOutputConfig) args[0];
          captured.set(config);
          return pin(DigitalOutput.class, config.bcm(), config.id(), config.name());
        });
    new Pi4JPinManagement(context(null, output)).allocateOutPin(Map.of("address", "25", "initial", "HIGH", "shutdown", "LOW"));
    assertEquals(25, captured.get().bcm());
    assertEquals(DigitalState.HIGH, captured.get().initialState());
    assertEquals(DigitalState.LOW, captured.get().shutdownState());
  }

  @Test
  void closeShutsDownBothProvidersUsingTheOwningContext() throws Exception {
    List<String> calls = new ArrayList<>();
    AtomicReference<Context> owningContext = new AtomicReference<>();
    DigitalInputProvider input = shutdownProvider(DigitalInputProvider.class, "input", calls, owningContext);
    DigitalOutputProvider output = shutdownProvider(DigitalOutputProvider.class, "output", calls, owningContext);
    Context context = context(input, output);
    owningContext.set(context);

    new Pi4JPinManagement(context).close();

    assertEquals(List.of("output", "input"), calls);
  }

  private <T> T shutdownProvider(Class<T> type, String name, List<String> calls, AtomicReference<Context> owningContext) {
    return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
      assertEquals("shutdownInternal", method.getName());
      assertSame(owningContext.get(), args[0]);
      calls.add(name);
      return proxy;
    }));
  }

  private Context context(DigitalInputProvider input, DigitalOutputProvider output) {
    return (Context) Proxy.newProxyInstance(Context.class.getClassLoader(), new Class<?>[] {Context.class}, (proxy, method, args) -> switch (method.getName()) {
      case "properties" -> Pi4J.newContextBuilder().noAutoDetect().build().properties();
      case "getDigitalInputProvider" -> input;
      case "getDigitalOutputProvider" -> output;
      default -> throw new AssertionError("Unexpected context operation: " + method.getName());
    });
  }

  private <T> T pin(Class<T> type, int address, String id, String name) {
    return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> switch (method.getName()) {
      case "getAddress", "bcm" -> address;
      case "id" -> id;
      case "name" -> name;
      default -> throw new AssertionError("Unexpected GPIO operation: " + method.getName());
    }));
  }
}
