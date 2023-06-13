package io.mapsmessaging.server.i2c.devices.drivers.servos;

import io.mapsmessaging.server.i2c.devices.drivers.PCA9685;
import java.io.IOException;

public class Servo  extends PWM_Device {

  private float myPos;

  public Servo(PCA9685 pwm, short servoId, AngleResponse response) throws IOException {
    super(pwm, servoId, response);
  }

  public float getPosition() {
    return myPos;
  }

  public void setPosition(float angle) throws IOException {
    myPos = myResponse.getResponse(angle);
    myPWMController.setPWM(myServoPort, (short) 1, (short) myPos);
  }
}