package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.Calibration2ByteRegister;
import lombok.Getter;

import java.io.IOException;

public class GasCalibrationData {

  private final SingleByteRegister parameterG1;
  private final Calibration2ByteRegister parameterG2;
  private final SingleByteRegister parameterG3;
  private final SingleByteRegister heatRangeRegister;
  private final SingleByteRegister heatValRegister;


  @Getter
  private int parG1;
  @Getter
  private int parG2;
  @Getter
  private int parG3;
  @Getter
  private int heatRange;
  @Getter
  private int heatVal;
  
  private boolean loaded;

  public GasCalibrationData(BME688Sensor sensor) throws IOException {
    parameterG1 = new SingleByteRegister(sensor, 0xED, "par_g1");
    parameterG2 = new Calibration2ByteRegister(sensor, 0xEB, "par_g2");
    parameterG3 = new SingleByteRegister(sensor, 0xEE, "par_g3");
    heatRangeRegister = new SingleByteRegister(sensor, 0x02, "res_heat_range");
    heatValRegister = new SingleByteRegister(sensor, 0x0, "res_heat_val");
    loaded = false;
  }

  public void load() throws IOException {
    if (!loaded) {
      loaded = true;
      parG1 = parameterG1.getRegisterValue() & 0xff;
      parG2 = parameterG2.getValue();
      parG3 = parameterG3.getRegisterValue() & 0xff;
      heatRange = (heatRangeRegister.getRegisterValue() & 0b110000) >> 4;
      heatVal = heatValRegister.getRegisterValue() & 0xff;
    }
  }

  public int calcResHeat(int desiredTemp, int ambientTemp) {
    double var1;
    double var2;
    double var3;
    double var4;
    double var5;
    int resHeat;

    if (desiredTemp > 400) { // Cap temperature
      desiredTemp = 400;
    }

    // Assuming getters for calibration parameters (parGh1, parGh2, parGh3, ambTemp, resHeatRange, and resHeatVal) are defined in GasCalibrationData
    var1 = (((double) getParG1() / 16.0) + 49.0);
    var2 = ((((double) getParG2() / 32768.0) * 0.0005) + 0.00235);
    var3 = ((double) getParG3() / 1024.0);
    var4 = (var1 * (1.0 + (var2 * desiredTemp)));
    var5 = (var4 + (var3 * ambientTemp));
    resHeat = (int) (3.4 *
        ((var5 * (4.0 / (4 + getHeatRange())) *
            (1.0 / (1 + (getHeatVal() * 0.002)))) - 25));

    return resHeat;
  }

}