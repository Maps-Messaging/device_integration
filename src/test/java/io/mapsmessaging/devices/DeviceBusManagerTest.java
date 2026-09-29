package io.mapsmessaging.devices;

import static org.junit.jupiter.api.Assertions.*;

import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.spi.SpiBusManager;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeviceBusManagerTest {
  static class RecordingI2CManager extends I2CBusManager {
    Map<String, Object> received;

    RecordingI2CManager(int bus) {
      super(null, null, bus);
    }

    @Override
    public I2CDeviceController configureDevices(Map<String, Object> config) throws IOException {
      received = config;
      return null;
    }
  }

  static class RecordingSpiManager extends SpiBusManager {
    Map<String, Object> received;

    RecordingSpiManager() {
      super(null);
    }

    @Override
    public java.util.List<io.mapsmessaging.devices.spi.SpiDeviceController> configureDevices(Map<String, Object> config) {
      received = config;
      return java.util.List.of();
    }
  }

  @Test
  void routesI2cAndSpiConfigurationToSelectedManagersWithoutHardware() throws Exception {
    RecordingI2CManager bus0 = new RecordingI2CManager(0);
    RecordingI2CManager bus1 = new RecordingI2CManager(1);
    RecordingI2CManager mockBus = new RecordingI2CManager(255);
    RecordingSpiManager spi = new RecordingSpiManager();
    DeviceBusManager manager = new DeviceBusManager(new I2CBusManager[] {bus0, bus1, mockBus}, spi);
    Map<String, Object> i2c = Map.of("bus", 2, "98", Map.of("deviceName", "Demo SCD41"));
    Map<String, Object> spiConfig = Map.of("Mcp3y0x", Map.of("spiBus", "0"));

    manager.configureDevices(Map.of("i2c", i2c, "spi", spiConfig));

    assertSame(i2c, mockBus.received);
    assertSame(spiConfig, spi.received);
    assertNull(bus0.received);
    assertNull(bus1.received);
    manager.configureDevices(Map.of());
    assertSame(i2c, mockBus.received);
    assertSame(spiConfig, spi.received);
  }
}
