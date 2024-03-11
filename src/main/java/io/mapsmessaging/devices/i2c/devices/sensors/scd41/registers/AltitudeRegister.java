package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.GetAltitudeRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SetAltitudeRequest;

public class AltitudeRegister extends RequestRegister {
  private GetAltitudeRequest getAltitudeRequest;
  private SetAltitudeRequest setAltitudeRequest;

  public AltitudeRegister(I2CDevice sensor) {
    // Initialize both request types upon creation
    super(sensor, "Altitude", null); // Initially, there's no default request to associate
    this.getAltitudeRequest = new GetAltitudeRequest(sensor.getDevice());
    this.setAltitudeRequest = new SetAltitudeRequest(sensor.getDevice());
  }

  public int getAltitude() {
    // Use the getAltitudeRequest to read the altitude
    return getAltitudeRequest.getAltitude();
  }

  public void setAltitude(int val) {
    // Use the setAltitudeRequest to update the altitude
    setAltitudeRequest.setAlititude(val); // Note: There's a typo in "setAlititude", should be "setAltitude"
  }
}