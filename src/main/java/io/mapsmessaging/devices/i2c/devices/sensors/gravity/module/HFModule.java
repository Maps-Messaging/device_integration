package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class HFModule extends SensorModule{

  public HFModule(){
    super(SensorType.HF);
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    return 0;
  }
}