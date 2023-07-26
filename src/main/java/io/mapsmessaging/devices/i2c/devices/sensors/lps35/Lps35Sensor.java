package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.values.DataRate;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.values.DataReadyInterrupt;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.values.FiFoMode;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

/**
 * Original CPP source <a href="https://github.com/adafruit/Adafruit_LPS35HW/blob/master/Adafruit_LPS35HW.cpp">...</a>
 */

public class Lps35Sensor extends I2CDevice implements Sensor, Resetable {

  public static final byte WHO_AM_I = 0x0F;
  public static final byte CTRL_REG1 = 0x10;
  public static final byte CTRL_REG2 = 0x11;
  public static final byte CTRL_REG3 = 0x12;
  public static final byte FIFO_CTRL = 0x14;

  public static final byte RES_CONF = 0x1A;

  @Getter
  private final InterruptConfigRegister interruptConfigRegister;

  @Getter
  private final ReferencePressureRegister referencePressureRegister;

  @Getter
  private final PressureOffsetRegister pressureOffsetRegister;

  @Getter
  private final ThresholdPressureRegister thresholdPressureRegister;

  @Getter
  private final TemperatureRegister temperatureRegister;

  @Getter
  private final PressureRegister pressureRegister;

  @Getter
  private final InterruptSourceRegister interruptSourceRegister;

  @Getter
  private final FiFoStatusRegister fiFoStatusRegister;

  @Getter
  private final StatusRegister statusRegister;

  @Getter
  private final List<SensorReading<?>> readings;

  public Lps35Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Lps35Sensor.class));
    interruptConfigRegister = new InterruptConfigRegister(this);
    referencePressureRegister = new ReferencePressureRegister(this);
    thresholdPressureRegister = new ThresholdPressureRegister(this);
    pressureOffsetRegister = new PressureOffsetRegister(this);
    temperatureRegister = new TemperatureRegister(this);
    pressureRegister = new PressureRegister(this);
    interruptSourceRegister = new InterruptSourceRegister(this);
    fiFoStatusRegister = new FiFoStatusRegister(this);
    statusRegister = new StatusRegister(this);

    FloatSensorReading pressureReading = new FloatSensorReading("pressure", "hPa", 260, 1260, this::getPressure);
    FloatSensorReading temperatureReading = new FloatSensorReading("temperature", "C", -30, 70, this::getTemperature);
    readings = List.of(pressureReading, temperatureReading);
    setDataRate(DataRate.RATE_1_HZ);
  }

  public static int getId(AddressableDevice device) {
    return device.readRegister(WHO_AM_I);
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public String getName() {
    return "LPS35";
  }

  @Override
  public String getDescription() {
    return "Pressure sensor: 260-1260 hPa";
  }

  //endregion

  //region Who Am I register
  public int whoAmI() throws IOException {
    return readRegister(WHO_AM_I) & 0xff;
  }
  //endregion

  public DataRate getDataRate() throws IOException {
    int ctl1 = (readRegister(CTRL_REG1) & 0xff >> 4);
    for (DataRate rate : DataRate.values()) {
      if (rate.getMask() == ctl1) {
        return rate;
      }
    }
    return DataRate.RATE_ONE_SHOT;
  }

  //region Control Register 1
  public void setDataRate(DataRate rate) throws IOException {
    setControlRegister(CTRL_REG1, 0b0001111, (rate.getMask() << 4));
  }

  public void setLowPassFilter(boolean flag) throws IOException {
    int value = flag ? 0b1000 : 0;
    setControlRegister(CTRL_REG1, 0b11110111, value);
  }

  public boolean isLowPassFilterSet() throws IOException {
    return (readRegister(CTRL_REG1) & 0b1000) != 0;
  }

  public void setLowPassFilterConfig(boolean flag) throws IOException {
    int value = flag ? 0b100 : 0;
    setControlRegister(CTRL_REG1, 0b11111011, value);
  }

  public boolean isLowPassFilterConfigSet() throws IOException {
    return (readRegister(CTRL_REG1) & 0b100) != 0;
  }

  public void setBlockUpdate(boolean flag) throws IOException {
    int value = flag ? 0b10 : 0;
    setControlRegister(CTRL_REG1, 0b11111101, value);
  }

  public boolean isBlockUpdateSet() throws IOException {
    return (readRegister(CTRL_REG1) & 0b10) != 0;
  }
  //endregion

  //region Control Register 2
  public void boot() throws IOException {
    setControlRegister(CTRL_REG2, 0b01111111, 0b10000000);
    delay(50);
  }

  public void enableFiFo(boolean flag) throws IOException {
    int value = flag ? 0b01000000 : 0;
    setControlRegister(CTRL_REG2, 0b10111111, value);
  }

  public boolean isFiFoEnabled() throws IOException {
    return (readRegister(CTRL_REG2) & 0b01000000) != 0;
  }

  public void enableStopFiFoOnThreshold(boolean flag) throws IOException {
    int value = flag ? 0b00100000 : 0;
    setControlRegister(CTRL_REG2, 0b11011111, value);
  }

  public boolean isStopFiFoOnThresholdEnabled() throws IOException {
    return (readRegister(CTRL_REG2) & 0b00100000) != 0;
  }

  public void softReset() throws IOException {
    setControlRegister(CTRL_REG2, 0b11111011, 0b100);
    delay(50);
  }

  @Override
  public void reset() throws IOException {
    boot();
  }

  public void enableOneShot(boolean flag) throws IOException {
    int value = flag ? 0b1 : 0;
    setControlRegister(CTRL_REG2, 0b11111110, value);
  }

  public boolean isOneShotEnabled() throws IOException {
    return (readRegister(CTRL_REG2) & 0b1) != 0;
  }
  //endregion

  //region Control Register 3

  public boolean isInterruptActive() throws IOException {
    return (readRegister(CTRL_REG3) & 0b10000000) != 0;
  }

  public boolean isPushPullDrainActive() throws IOException {
    return (readRegister(CTRL_REG3) & 0b01000000) != 0;
  }

  public void enableFiFoDrainInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b00100000 : 0;
    setControlRegister(CTRL_REG3, 0b11011111, value);
  }

  public boolean isFiFoDrainInterruptEnabled() throws IOException {
    return (readRegister(CTRL_REG3) & 0b00100000) != 0;
  }

  public void enableFiFoWatermarkInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b0010000 : 0;
    setControlRegister(CTRL_REG3, 0b11101111, value);
  }

  public boolean isFiFoWatermarkInterruptEnabled() throws IOException {
    return (readRegister(CTRL_REG3) & 0b0010000) != 0;
  }

  public void enableFiFoOverrunInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b001000 : 0;
    setControlRegister(CTRL_REG3, 0b11110111, value);
  }

  public boolean isFiFoOverrunInterruptEnabled() throws IOException {
    return (readRegister(CTRL_REG3) & 0b001000) != 0;
  }

  public void setSignalOnInterrupt(DataReadyInterrupt flag) throws IOException {
    int value = flag.getMask();
    setControlRegister(CTRL_REG3, 0b11111100, value);
  }

  public DataReadyInterrupt isSignalOnInterrupr() throws IOException {
    int mask = (readRegister(CTRL_REG3) & 0b11);
    for (DataReadyInterrupt dataReadyInterrupt : DataReadyInterrupt.values()) {
      if (mask == dataReadyInterrupt.getMask()) {
        return dataReadyInterrupt;
      }
    }
    return DataReadyInterrupt.ORDER_OF_PRIORITY;
  }
  //endregion

  //region FiFo Control Register

  public FiFoMode getFifoMode() throws IOException {
    int mask = readRegister(FIFO_CTRL) >> 5;
    for (FiFoMode mode : FiFoMode.values()) {
      if (mode.getMask() == mask) {
        return mode;
      }
    }
    return FiFoMode.BYPASS;
  }

  public void setFifoMode(FiFoMode mode) throws IOException {
    setControlRegister(FIFO_CTRL, 0b11111, mode.getMask());
  }

  public int getFiFoWaterMark() throws IOException {
    return (readRegister(FIFO_CTRL) & 0b11111);
  }

  public void setFiFoWaterMark(int waterMark) throws IOException {
    setControlRegister(FIFO_CTRL, 0b11100000, (waterMark & 0b11111));
  }
  //endregion


  //region Low Power Mode Registers
  public void setLowPowerMode(boolean flag) throws IOException {
    int value = flag ? 0b1 : 0;
    setControlRegister(RES_CONF, 0b0, value);
  }

  public boolean isLowPowerModeEnabled() throws IOException {
    return (readRegister(RES_CONF) & 0b1) != 0;
  }
  //endregion


  //region Pressure Out Registers
  protected float getPressure() throws IOException {
    return pressureRegister.getPressure();
  }
  //endregion

  //region Temperature Out Registers
  protected float getTemperature() throws IOException {
    return temperatureRegister.getTemperature();
  }
  //endregion


  private void setControlRegister(int register, int mask, int value) throws IOException {
    int ctl1 = readRegister(register) & 0xff;
    ctl1 = (ctl1 & mask) | value;
    write(register, (byte) ctl1);
  }
}
