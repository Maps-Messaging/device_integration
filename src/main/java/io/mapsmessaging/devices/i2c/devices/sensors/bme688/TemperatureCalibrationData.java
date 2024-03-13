package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.Calibration2ByteRegister;

import java.io.IOException;

public class TemperatureCalibrationData {

  private final Calibration2ByteRegister parameterT1;
  private final Calibration2ByteRegister parameterT2;
  private final SingleByteRegister parameterT3;

  private int parT1;
  private int parT2;
  private int parT3;
  private boolean loaded;

  public TemperatureCalibrationData(BME688Sensor sensor) throws IOException {
    parameterT1 = new Calibration2ByteRegister(sensor, 0xE9, "par_t1");
    parameterT2 = new Calibration2ByteRegister(sensor, 0x8A, "par_t2");
    parameterT3 = new SingleByteRegister(sensor, 0x8C, "par_t3");
    loaded = false;
  }

  public int getParT1() throws IOException {
    load();
    return parT1;
  }

  public int getParT2() throws IOException {
    load();
    return parT2;
  }

  public int getParT3() throws IOException {
    load();
    return parT3;
  }

  private void load() throws IOException {
    if(!loaded){
      loaded = true;
      parT1 = parameterT1.getValue();
      parT2 = parameterT2.getValue();
      parT3 = parameterT3.getRegisterValue() & 0xff;
    }
  }

}
