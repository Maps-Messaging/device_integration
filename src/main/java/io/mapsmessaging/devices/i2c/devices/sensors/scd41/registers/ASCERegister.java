package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.GetASCERequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SetASCERequest;

public class ASCERegister extends RequestRegister {

  public ASCERegister(I2CDevice sensor) {
    // Since the initial state requires just checking, we'll associate it with getting the ASCE state.
    super(sensor, "ASCE", new GetASCERequest(sensor.getDevice()));
  }

  public boolean isASCEEnabled() {
    // Ensure the request is of the correct type and execute its specific method.
    if (request instanceof GetASCERequest) {
      return ((GetASCERequest) request).isASCEEnabled();
    }
    // In case the request instance is not of expected type, consider ASCE disabled or handle appropriately.
    return false;
  }

  public void setASCEState(boolean flag) {
    // Create a new SetASCERequest instance to change the ASCE state.
    SetASCERequest setRequest = new SetASCERequest(this.request.getDevice());
    setRequest.setASCEState(flag);
    // Optionally, update the 'request' member to reflect the new state if required for subsequent operations.
  }
}
