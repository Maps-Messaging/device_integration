package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

public class Config {
  public static final int PERIOD_POLL = 10_000;
  public static final byte CHIP_ID = 0x61;
  public static final int PERIOD_RESET = 10_000;
  public static final byte I2C_ADDR_LOW = 0x76;
  public static final byte I2C_ADDR_HIGH = 0x77;
  public static final byte SOFT_RESET_CMD = (byte) 0xb6;
  public static final byte OK = 0;
  public static final byte ERROR_NULL_PTR = -1;
  public static final byte ERROR_COMMUNICATION_FAIL = -2;
  public static final byte ERROR_DEVICE_NOT_FOUND = -3;
  public static final byte ERROR_INVALID_LENGTH = -4;
  public static final byte ERROR_SELF_TEST = -5;
  public static final byte WARNING_DEFINE_OP_MODE = 1;
  public static final byte WARNING_NO_NEW_DATA = 2;
  public static final byte WARNING_DEFINE_SHARED_HEATER_DURATION = 3;
  public static final byte INFO_PARAMETER_CORRECTION = 1;

  // Register addresses
  public static final int REG_CHIP_ID = 0xD0;
  public static final int REG_SOFT_RESET = 0xE0;
}


