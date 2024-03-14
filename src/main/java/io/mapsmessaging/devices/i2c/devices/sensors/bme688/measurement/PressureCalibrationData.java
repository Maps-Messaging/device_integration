package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.Calibration2ByteRegister;
import lombok.Getter;

import java.io.IOException;

public class PressureCalibrationData {

  private final Calibration2ByteRegister parameterP1;
  private final Calibration2ByteRegister parameterP2;
  private final SingleByteRegister parameterP3;

  private final Calibration2ByteRegister parameterP4;
  private final Calibration2ByteRegister parameterP5;
  private final SingleByteRegister parameterP6;
  private final SingleByteRegister parameterP7;

  private final Calibration2ByteRegister parameterP8;
  private final Calibration2ByteRegister parameterP9;
  private final SingleByteRegister parameterP10;

  @Getter
  private int parP1;
  @Getter
  private int parP2;
  @Getter
  private int parP3;
  @Getter
  private int parP4;
  @Getter
  private int parP5;
  @Getter
  private int parP6;
  @Getter
  private int parP7;
  @Getter
  private int parP8;
  @Getter
  private int parP9;
  @Getter
  private int parP10;

  private boolean loaded;

  public PressureCalibrationData(BME688Sensor sensor) throws IOException {
    parameterP1 = new Calibration2ByteRegister(sensor, 0x8E, "par_p1");
    parameterP2 = new Calibration2ByteRegister(sensor, 0x90, "par_p2");
    parameterP3 = new SingleByteRegister(sensor, 0x92, "par_p3");

    parameterP4 = new Calibration2ByteRegister(sensor, 0x94, "par_p4");
    parameterP5 = new Calibration2ByteRegister(sensor, 0x96, "par_p5");
    parameterP6 = new SingleByteRegister(sensor, 0x99, "par_p6");
    parameterP7 = new SingleByteRegister(sensor, 0x98, "par_p7");

    parameterP8 = new Calibration2ByteRegister(sensor, 0x9C, "par_p8");
    parameterP9 = new Calibration2ByteRegister(sensor, 0x9E, "par_p9");
    parameterP10 = new SingleByteRegister(sensor, 0xA0, "par_p10");

    loaded = false;
  }

  public void load() throws IOException {
    if (!loaded) {
      loaded = true;
      parP1 = parameterP1.getValue();
      parP2 = parameterP2.getValue();
      parP3 = parameterP3.getRegisterValue() & 0xff;

      parP4 = parameterP4.getValue();
      parP5 = parameterP5.getValue();
      parP6 = parameterP6.getRegisterValue() & 0xff;
      parP7 = parameterP7.getRegisterValue() & 0xff;

      parP8 = parameterP8.getValue();
      parP9 = parameterP9.getValue();
      parP10 = parameterP10.getRegisterValue() & 0xff;

    }
  }
}
