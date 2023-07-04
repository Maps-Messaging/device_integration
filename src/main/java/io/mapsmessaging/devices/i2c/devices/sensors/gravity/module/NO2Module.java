package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class NO2Module extends SensorModule{

  public NO2Module(){
    super(SensorType.NO2);
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    return 0;
  }
}