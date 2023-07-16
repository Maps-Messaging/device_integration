package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.RegisterMap;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.MotionInterrupts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MotionInterruptRegister extends SingleByteRegister {

  public MotionInterruptRegister(I2CDevice sensor, RegisterMap registerMap) throws IOException {
    super(sensor, 0x9, "Motion Interrupt", registerMap);
  }

  public List<MotionInterrupts> getInterrupts() throws IOException {
    List<MotionInterrupts> list = new ArrayList<>();
    reload();
    for (MotionInterrupts interrupts : MotionInterrupts.values()) {
      if ((interrupts.getMask() & registerValue) != 0) {
        list.add(interrupts);
      }
    }
    return list;
  }

}
