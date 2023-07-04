package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class O2Module  extends SensorModule{

  public O2Module(){
    super(SensorType.O2);
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    return rawConcentration;
  }
}
