package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class HCLModule extends SensorModule {

  public HCLModule() {
    super();
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    if (temperature > -20 && temperature <= 0) {
      return rawConcentration - (-0.0075f * temperature - 0.1f);
    }
    if (temperature > 0 && temperature <= 20) {
      return rawConcentration - (-0.1f);
    }
    if (temperature > 20 && temperature <= 50) {
      return rawConcentration - (-0.01f * temperature + 0.1f);
    }
    return rawConcentration;
  }
}