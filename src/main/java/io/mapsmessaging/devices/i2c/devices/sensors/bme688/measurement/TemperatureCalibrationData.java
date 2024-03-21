package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import lombok.Getter;
import lombok.Setter;

@Getter
public class TemperatureCalibrationData {
  @Setter
  public float ambientAir;
  @Setter
  private int tFine;
  private final int parT1;
  private final int parT2;
  private final int parT3;

  public TemperatureCalibrationData(CalibrationData calibrationData) {
    parT1 = calibrationData.getShort(31);
    parT2 = calibrationData.getShort(0);
    parT3 = calibrationData.getByte(2);
  }
}
