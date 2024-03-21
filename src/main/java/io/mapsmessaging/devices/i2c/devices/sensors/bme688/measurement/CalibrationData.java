package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.CalibrationData1Register;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.CalibrationData2Register;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.CalibrationData3Register;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CalibrationData {
  private final CalibrationData1Register calibrationData1Register;
  private final CalibrationData2Register calibrationData2Register;
  private final CalibrationData3Register calibrationData3Register;

  @Getter
  private final GasCalibrationData gasCalibrationData;
  @Getter
  private final HumidityCalibrationData humidityCalibrationData;
  @Getter
  private final PressureCalibrationData pressureCalibrationData;
  @Getter
  private final TemperatureCalibrationData temperatureCalibrationData;

  private final byte[] buffer;

  public CalibrationData(I2CDevice sensor) throws IOException {
    calibrationData1Register = new CalibrationData1Register(sensor);
    calibrationData2Register = new CalibrationData2Register(sensor);
    calibrationData3Register = new CalibrationData3Register(sensor);
    byte[] buf1 = calibrationData1Register.getBuffer();
    byte[] buf2 = calibrationData2Register.getBuffer();
    byte[] buf3 = calibrationData3Register.getBuffer();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(100);
    byteArrayOutputStream.write(buf1);
    byteArrayOutputStream.write(buf2);
    byteArrayOutputStream.write(buf3);
    buffer = byteArrayOutputStream.toByteArray();
    gasCalibrationData = new GasCalibrationData(this);
    humidityCalibrationData = new HumidityCalibrationData(this);
    temperatureCalibrationData = new TemperatureCalibrationData(this);
    pressureCalibrationData = new PressureCalibrationData(this);
  }

  public int getByte(int idx){
    return buffer[idx] & 0xff;
  }

  public int getShort(int idx){
    return ((buffer[idx+1] & 0xff)<< 8) | (buffer[idx] & 0xff);
  }
}
