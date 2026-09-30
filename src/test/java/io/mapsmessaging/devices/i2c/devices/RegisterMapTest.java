package io.mapsmessaging.devices.i2c.devices;

import static org.junit.jupiter.api.Assertions.*;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RegisterMapTest {
  @Test
  void exportReadsEachRegisterOnceAndReturnsTheSameSnapshot() throws Exception {
    AddressableDevice bus = (AddressableDevice) Proxy.newProxyInstance(
        AddressableDevice.class.getClassLoader(), new Class<?>[] {AddressableDevice.class},
        (proxy, method, args) -> method.getReturnType() == int.class ? 1 : null);
    I2CDevice sensor = new I2CDevice(bus, LoggerFactory.getLogger(RegisterMapTest.class)) {
      @Override public boolean isConnected() { return true; }
      @Override public String getName() { return "test"; }
      @Override public String getDescription() { return "test"; }
      @Override public io.mapsmessaging.devices.DeviceType getType() { return io.mapsmessaging.devices.DeviceType.SENSOR; }
    };
    AtomicInteger reads = new AtomicInteger();
    RegisterData first = new RegisterData() { };
    new Register(sensor, 1, "changing register") {
      @Override protected void reload() { }
      @Override protected void setControlRegister(int mask, int value) { }
      @Override public String toString(int maxLength) { return "test"; }
      @Override public RegisterData toData() throws IOException {
        if (reads.incrementAndGet() != 1) throw new IOException("Duplicate read");
        return first;
      }
    };
    assertSame(first, sensor.getRegisterMap().getData().get(1));
    assertEquals(1, reads.get());
  }
}
