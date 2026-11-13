package io.mapsmessaging.devices.serial;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComputedSerialControllerLifecycleTest {
  @Test
  void closedSecondaryCannotConsumeDataOrConfiguration() throws Exception {
    Secondary secondary = new Secondary();
    assertArrayEquals(new byte[]{42}, secondary.getDeviceState());
    secondary.close();
    assertTrue(secondary.isComputedClosed());
    assertThrows(IOException.class, secondary::getDeviceState);
    assertThrows(IOException.class, secondary::getDeviceConfiguration);
    assertThrows(IOException.class, () -> secondary.updateDeviceConfiguration(new byte[0]));
    assertEquals(1, secondary.readCount);
  }

  private static final class Secondary extends ComputedSerialDeviceController {
    int readCount;

    @Override protected byte[] readComputedState() {
      readCount++;
      return new byte[]{42};
    }

    @Override public String getComputedDeviceSuffix() { return "change"; }
    @Override public String getName() { return "TEST_change"; }
    @Override public String getDescription() { return "Computed test device"; }
    @Override public SchemaConfig getSchema() { return null; }
    @Override public DeviceType getType() { return DeviceType.SENSOR; }
    @Override public SerialDeviceController mount(SerialDevice device) { return this; }
  }
}
