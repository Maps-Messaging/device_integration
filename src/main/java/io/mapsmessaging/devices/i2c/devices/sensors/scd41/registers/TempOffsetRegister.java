package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.GetTempOffsetRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SetTempOffsetRequest;

public class TempOffsetRegister extends RequestRegister {

  private GetTempOffsetRequest getTempOffsetRequest;
  private SetTempOffsetRequest setTempOffsetRequest;

  public TempOffsetRegister(I2CDevice sensor) {
    super(sensor, "TempOffset", null);
    this.getTempOffsetRequest = new GetTempOffsetRequest(sensor.getDevice());
    this.setTempOffsetRequest = new SetTempOffsetRequest(sensor.getDevice());
  }

  public int getTempOffset() {
    return getTempOffsetRequest.getTempOffset();
  }

  public void setTempOffset(int val) {
    setTempOffsetRequest.setTempOffset(val);
  }
}
