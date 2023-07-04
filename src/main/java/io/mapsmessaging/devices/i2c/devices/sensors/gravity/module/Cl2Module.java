package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class Cl2Module extends SensorModule{

  public Cl2Module(){
    super();
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    if(temperature > -20 && temperature <= 0){
      return rawConcentration/ (0.015f * temperature + 1.1f) - 0.0025f;
    }
    if(temperature > 0 && temperature <= 20){
      return rawConcentration / ( 1.1f - 0.005f * temperature);
    }
    if(temperature > 20 && temperature < 40){
      return rawConcentration - ( -0.005f * temperature +0.3f);
    }
    return 0;
  }
}