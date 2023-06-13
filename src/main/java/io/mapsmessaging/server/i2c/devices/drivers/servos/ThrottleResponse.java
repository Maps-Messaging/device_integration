package io.mapsmessaging.server.i2c.devices.drivers.servos;

public class ThrottleResponse implements AngleResponse {

  private final int myLowBound;
  private final int myServoRange;

  public ThrottleResponse(short lowBound, short highBound) {
    myLowBound = lowBound;
    myServoRange = highBound - lowBound;
  }

  public float getMin() {
    return 0.0f;
  }

  public float getMax() {
    return 100.0f;
  }

  public float getIdle() {
    return 0.0f;
  }

  @Override
  public float getResponse(float angle) {
    if (angle < 0.0f) angle = 0.0f;
    if (angle > 100.0f) angle = 100.0f;

    float pos = (100 - angle) / 100;
    pos = myServoRange * pos + myLowBound;
    return pos;
  }
}