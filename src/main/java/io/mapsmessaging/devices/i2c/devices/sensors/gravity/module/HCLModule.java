package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class HCLModule extends SensorModule{

  public HCLModule(){
    super(SensorType.HCL);
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    return 0;
  }
}