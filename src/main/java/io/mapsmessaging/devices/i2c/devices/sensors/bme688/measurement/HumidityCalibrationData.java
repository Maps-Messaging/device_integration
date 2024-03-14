package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.Calibration2ByteRegister;
import lombok.Getter;

import java.io.IOException;

public class HumidityCalibrationData {

  private final Calibration2ByteRegister parameterH1;
  private final SingleByteRegister parameterH2;
  private final SingleByteRegister parameterH3;

  private final SingleByteRegister parameterH4;
  private final SingleByteRegister parameterH5;
  private final SingleByteRegister parameterH6;
  private final SingleByteRegister parameterH7;

  @Getter
  private int parH1;
  @Getter
  private int parH2;
  @Getter
  private int parH3;
  @Getter
  private int parH4;
  @Getter
  private int parH5;
  @Getter
  private int parH6;
  @Getter
  private int parH7;

  private boolean loaded;

  public HumidityCalibrationData(BME688Sensor sensor) throws IOException {
    parameterH1 = new Calibration2ByteRegister(sensor, 0xE2, "par_h1");
    parameterH2 = new SingleByteRegister(sensor, 0xE1, "par_h2");
    parameterH3 = new SingleByteRegister(sensor, 0xE4, "par_h3");

    parameterH4 = new SingleByteRegister(sensor, 0xE5, "par_h4");
    parameterH5 = new SingleByteRegister(sensor, 0xE6, "par_h5");
    parameterH6 = new SingleByteRegister(sensor, 0xE7, "par_h6");
    parameterH7 = new SingleByteRegister(sensor, 0xE8, "par_h7");

    loaded = false;
  }

  public void load() throws IOException {
    if (!loaded) {
      loaded = true;
      parH1 = parameterH1.getValue();
      parH2 = parameterH2.getRegisterValue() & 0xff;
      parH2 = ((parH1 >> 4) & 0x0F00) | parH2;
      parH1 = parH1 & 0x0FFF;

      parH3 = parameterH3.getRegisterValue() & 0xff;

      parH4 = parameterH4.getRegisterValue() & 0xff;
      parH5 = parameterH5.getRegisterValue() & 0xff;
      parH6 = parameterH6.getRegisterValue() & 0xff;
      parH7 = parameterH7.getRegisterValue() & 0xff;

    }
  }

}
