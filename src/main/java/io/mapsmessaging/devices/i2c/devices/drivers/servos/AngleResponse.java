package io.mapsmessaging.devices.i2c.devices.drivers.servos;

public interface AngleResponse {

  float getResponse(float angle);

  float getMin();

  float getMax();

  float getIdle();

}