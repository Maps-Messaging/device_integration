package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class H2Module extends SensorModule {

  public H2Module() {
    super();
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    if (temperature > -20 && temperature <= 20) {
      return rawConcentration / (0.0074f * temperature + 0.7f) - 5;
    }
    if (temperature > 20 && temperature <= 40) {
      return rawConcentration / (0.025f * temperature + 0.3f) - 5;
    }
    if (temperature > 40 && temperature <= 60) {
      return (rawConcentration / (0.001f * temperature + 0.9f)) - (0.75f * temperature - 25f);
    }
    return 0;
  }
}