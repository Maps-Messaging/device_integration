package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

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

  protected abstract float calculateSensorConcentration (float temperature, float rawConcentration);
}
