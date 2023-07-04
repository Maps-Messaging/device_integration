package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class NH3Module extends SensorModule{

  public NH3Module(){
    super(SensorType.NH3);
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    return 0;
  }
}
