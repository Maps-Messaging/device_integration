package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.sen0539.Sen0539Sensor;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoiceStartupRegressionTest {
  @Test
  void voiceRejectsPartialInitialFrameAndImmediatelyRetries() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.shortRead = true;
    device.image[2] = 5;
    Sen0539Sensor sensor = new Sen0539Sensor(device);
    assertThrows(IOException.class, sensor::getCommandId);
    device.shortRead = false;
    device.image[5] = 6;
    assertEquals(6, sensor.getVolume());
  }
}
