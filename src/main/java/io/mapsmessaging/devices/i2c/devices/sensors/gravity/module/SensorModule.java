package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public abstract class SensorModule {

  protected SensorModule() {
  }

  public float computeGasConcentration(float temperature, float computed) {

    return calculateSensorConcentration(temperature, computed);
  }

  protected abstract float calculateSensorConcentration(float temperature, float rawConcentration);
}
