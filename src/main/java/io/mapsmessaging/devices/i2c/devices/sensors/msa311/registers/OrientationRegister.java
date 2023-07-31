package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.OrientationData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientationStatus;

import java.io.IOException;

public class OrientationRegister extends SingleByteRegister {

  public OrientationRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0xC, "Orientation_Status");
  }

  public OrientationStatus getOrientation() throws IOException {
    reload();
    int val = registerValue >> 4;
    for (OrientationStatus orientation : OrientationStatus.values()) {
      if (orientation.getMask() == val) {
        return orientation;
      }
    }
    return OrientationStatus.Z_UP_PORTRAIT_UPRIGHT;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new OrientationData(getOrientation());
  }

}
