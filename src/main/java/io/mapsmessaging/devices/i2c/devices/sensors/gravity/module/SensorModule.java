package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

import static java.lang.Math.log;

public abstract class SensorModule {

  protected SensorModule(){
  }

  public float computeGasConcentration(float temperature, int sensorReading, int decimalPoint){
    float computed = sensorReading;
    switch(decimalPoint){
      case 1:
        computed = computed * 0.1f;
        break;
      case 2:
        computed = computed * 0.01f;
        break;

      default:
        break;
    }
    return calculateSensorConcentration(temperature, computed);
  }

  public float computeTemperature(int rawTemperature){
    float vpd3 = 3 * (float)rawTemperature / 1024.0f;
    float rth = vpd3 * 10000f / (3f - vpd3);
    return (float)(1 / (1 / (273.15f + 25) + 1 / 3380.13f * log(rth / 10000f)) - 273.15f);
  }

  protected abstract float calculateSensorConcentration (float temperature, float rawConcentration);
}
