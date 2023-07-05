package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class PH3Module extends SensorModule {

  public PH3Module() {
    super();
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    if (temperature > 20 && temperature < 40) {
      return rawConcentration / (0.005f * temperature + 0.9f);
    }

    return rawConcentration;
  }

}