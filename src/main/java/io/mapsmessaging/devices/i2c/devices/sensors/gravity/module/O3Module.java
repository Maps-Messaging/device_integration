package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class O3Module extends SensorModule{

  public O3Module(){
    super(SensorType.O3);
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    return 0;
  }
}