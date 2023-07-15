package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.TapActiveStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TapActiveStatusRegister extends SingleByteRegister {

  public TapActiveStatusRegister(I2CDevice sensor) {
    super(sensor, 0xb);
  }

  public List<TapActiveStatus> getTapActiveStatus() throws IOException {
    List<TapActiveStatus> list = new ArrayList<>();
    reload();
    for (TapActiveStatus taps : TapActiveStatus.values()) {
      if ((taps.getMask() & registerValue) != 0) {
        list.add(taps);
      }
    }
    return list;
  }

}
