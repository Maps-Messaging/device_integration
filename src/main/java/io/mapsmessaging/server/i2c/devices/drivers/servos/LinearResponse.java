package io.mapsmessaging.server.i2c.devices.drivers.servos;

public class LinearResponse implements AngleResponse {

  private final int myLowBound;
  private final int myServoRange;

  private float myMinAngle;
  private float myMaxAngle;
  private float myRange;


  public LinearResponse(short lowBound, short highBound, float minAngle, float maxAngle) {
    myLowBound = lowBound;
    myServoRange = highBound - lowBound;

    myMinAngle = minAngle;
    myMaxAngle = maxAngle;
    myRange = myMaxAngle - myMinAngle;
  }

  public float getMin() {
    return myMinAngle;
  }

  public float getMax() {
    return myMaxAngle;
  }

  public float getIdle() {
    return (myMaxAngle - myMaxAngle) / 2.0f;
  }

  @Override
  public float getResponse(float angle) {
    if (angle < myMinAngle)
      angle = myMinAngle;
    if (angle > myMaxAngle)
      angle = myMaxAngle;

    float pos = (myMaxAngle - angle) / myRange;
    pos = myServoRange * pos + myLowBound;
    return pos;
  }
}