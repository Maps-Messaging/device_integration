package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class PH3Module extends SensorModule{

  public PH3Module(){
    super(SensorType.PH3);
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    return 0;
  }
}