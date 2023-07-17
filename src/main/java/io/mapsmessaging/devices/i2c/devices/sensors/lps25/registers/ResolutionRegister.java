package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.PressureAverage;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.TemperatureAverage;

import java.io.IOException;

public class ResolutionRegister extends SingleByteRegister {

  private static final byte RESOLUTION_ADDRESS = 0x10;
  private static final byte AVE_PRESSURE_MASK = 0b00000011;
  private static final byte AVE_TEMPERATURE_MASK = 0b00001100;

  public ResolutionRegister(I2CDevice sensor) throws IOException {
    super(sensor, RESOLUTION_ADDRESS, "RES_CONF");
  }

  public PressureAverage getPressureAverage() {
    int rateVal = ((registerValue & AVE_PRESSURE_MASK));
    for (PressureAverage pressureAverage : PressureAverage.values()) {
      if (pressureAverage.getMask() == rateVal) {
        return pressureAverage;
      }
    }
    return PressureAverage.AVERAGE_8;
  }

  public void setPressureAverage(PressureAverage ave) throws IOException {
    setControlRegister(~AVE_PRESSURE_MASK, (ave.getMask()));
  }

  public TemperatureAverage getTemperatureAverage() {
    int rateVal = ((registerValue & AVE_TEMPERATURE_MASK) >> 2);
    for (TemperatureAverage average : TemperatureAverage.values()) {
      if (average.getMask() == rateVal) {
        return average;
      }
    }
    return TemperatureAverage.AVERAGE_8;
  }


  public void setTemperatureAverage(TemperatureAverage ave) throws IOException {
    setControlRegister(~AVE_TEMPERATURE_MASK, (ave.getMask() << 2));
  }

}