package io.mapsmessaging.devices.i2c.devices.drivers.servos;

import io.mapsmessaging.devices.i2c.Delay;
import io.mapsmessaging.devices.i2c.devices.drivers.PCA9685Device;

import java.io.IOException;

public abstract class PwmDevice {

  protected final PCA9685Device myPWMController;
  protected final short myServoPort;
  protected final AngleResponse myResponse;


  protected PwmDevice(PCA9685Device pwm, short servoId, AngleResponse response) throws IOException {
    myPWMController = pwm;
    myServoPort = servoId;
    myResponse = response;
    setPosition(myResponse.getMin());
    Delay.pause(500); // allow the servo to get to min
    setPosition(myResponse.getMax());
    Delay.pause(500); // allow the servo to get to max
    setPosition(myResponse.getIdle()); // Set to idle position
  }

  public int getPort() {
    return myServoPort;
  }

  abstract void setPosition(float angle) throws IOException;

  public void close() throws IOException {
    setPosition(myResponse.getIdle());
  }
}