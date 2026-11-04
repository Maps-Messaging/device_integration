package io.mapsmessaging.devices.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiChipSelect;
import com.pi4j.io.spi.SpiConfig;
import com.pi4j.io.spi.SpiConfigBuilder;
import com.pi4j.io.spi.SpiMode;
import io.mapsmessaging.devices.spi.devices.mcp3y0x.Mcp3y0xController;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class Pi4JProviderConfigurationTest {

  @Test
  void spiUsesFfmAndPreservesBusChipSelectAndMode() {
    AtomicReference<SpiConfig> captured = new AtomicReference<>();
    Spi handle = (Spi) Proxy.newProxyInstance(Spi.class.getClassLoader(), new Class<?>[] {Spi.class}, (proxy, method, args) -> null);
    Context context = context((argument) -> {
      captured.set(argument instanceof SpiConfig config ? config : ((SpiConfigBuilder) argument).build());
      return handle;
    });

    assertSame(handle, new Mcp3y0xController().createDevice(context, "ADC", "adc", 1, SpiChipSelect.CS_1, SpiMode.MODE_3));
    SpiConfig config = captured.get();
    assertEquals("ffm-spi", config.provider());
    assertEquals(1, config.bus().getBus());
    assertEquals(SpiChipSelect.CS_1, config.chipSelect());
    assertEquals(SpiMode.MODE_3, config.mode());
  }

  @Test
  void chipSelectOutputUsesFfmAndPreservesPinAndStates() {
    AtomicReference<DigitalOutputConfig> captured = new AtomicReference<>();
    DigitalOutput handle = (DigitalOutput) Proxy.newProxyInstance(DigitalOutput.class.getClassLoader(), new Class<?>[] {DigitalOutput.class},
        (proxy, method, args) -> null);
    Context context = context((argument) -> {
      captured.set(argument instanceof DigitalOutputConfig config ? config : ((DigitalOutputConfigBuilder) argument).build());
      return handle;
    });

    assertSame(handle, new Mcp3y0xController().createClientSelect(context, "cs", "Chip select", 25, DigitalState.HIGH, DigitalState.LOW));
    DigitalOutputConfig config = captured.get();
    assertEquals("ffm-digital-output", config.provider());
    assertEquals(25, config.bcm());
    assertEquals(DigitalState.HIGH, config.initialState());
    assertEquals(DigitalState.LOW, config.shutdownState());
  }

  private Context context(java.util.function.Function<Object, Object> create) {
    return (Context) Proxy.newProxyInstance(Context.class.getClassLoader(), new Class<?>[] {Context.class}, (proxy, method, args) -> {
      if (method.getName().equals("create") && args.length == 1) {
        return create.apply(args[0]);
      }
      if (method.getName().equals("properties")) {
        return Pi4J.newContextBuilder().noAutoDetect().build().properties();
      }
      throw new AssertionError("Unexpected context operation: " + method.getName());
    });
  }
}
