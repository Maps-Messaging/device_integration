package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.Calibration2ByteRegister;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class TemperatureCalibrationData {

  private final Calibration2ByteRegister parameterT1;
  private final Calibration2ByteRegister parameterT2;
  private final SingleByteRegister parameterT3;


  @Getter
  @Setter
  private long tFine;

  @Getter
  private int parT1;
  @Getter
  private int parT2;
  @Getter
  private int parT3;
  private boolean loaded;

  public TemperatureCalibrationData(BME688Sensor sensor) throws IOException {
    parameterT1 = new Calibration2ByteRegister(sensor, 0xE9, "par_t1");
    parameterT2 = new Calibration2ByteRegister(sensor, 0x8A, "par_t2");
    parameterT3 = new SingleByteRegister(sensor, 0x8C, "par_t3");
    loaded = false;
  }

  public void load() throws IOException {
    if (!loaded) {
      loaded = true;
      parT1 = parameterT1.getValue();
      parT2 = parameterT2.getValue();
      parT3 = parameterT3.getRegisterValue() & 0xff;
    }
  }

}
