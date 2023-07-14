package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientationStatus;

import java.io.IOException;

public class OrientationRegister  extends Register {

  public OrientationRegister(I2CDevice sensor) {
    super(sensor, 0xC);
  }

  public OrientationStatus getOrientation() throws IOException {
    reload();
    int val = registerValue >> 4;
    for(OrientationStatus orientation:OrientationStatus.values()){
      if(orientation.getMask() == val){
        return orientation;
      }
    }
    return OrientationStatus.Z_UP_PORTRAIT_UPRIGHT;
  }

}
