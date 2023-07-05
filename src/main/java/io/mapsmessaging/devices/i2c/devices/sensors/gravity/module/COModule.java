package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class COModule extends SensorModule {

  public COModule() {
    super();
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    if(rawConcentration > 0) {
      if (temperature > -40 && temperature <= 20) {
        return rawConcentration / (0.005f * temperature + 0.9f);
      }
      if (temperature > 20 && temperature < 40) {
        return rawConcentration / (0.005f * temperature + 0.9f) - (0.3f * (temperature) - 6);
      }
    }
    return 0.0f;
  }
}
