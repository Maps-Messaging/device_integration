package io.mapsmessaging.devices.i2c.devices.sensors.bme688.values;

public enum HeaterStep {

  NONE(0),
  STEP_1(1),
  STEP_2(2),
  STEP_3(3),
  STEP_4(4),
  STEP_5(5),
  STEP_6(6),
  STEP_7(7),
  STEP_8(9),
  STEP_9(9);


  private final int value;

  HeaterStep(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
