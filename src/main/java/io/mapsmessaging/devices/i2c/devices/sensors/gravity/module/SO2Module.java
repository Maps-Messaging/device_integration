package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class SO2Module extends SensorModule{

  public SO2Module(){
    super(SensorType.SO2);
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    return 0;
  }
}