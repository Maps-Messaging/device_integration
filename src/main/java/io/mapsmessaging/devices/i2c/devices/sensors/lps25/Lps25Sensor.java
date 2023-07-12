package io.mapsmessaging.devices.i2c.devices.sensors.lps25;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers.*;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lps25Sensor extends I2CDevice {

  public static final byte REF_P_XL = 0x08;
  public static final byte WHO_AM_I = 0x0F;
  public static final byte RES_CONF = 0x10;

  public static final byte CTRL_REG1 = 0x20;
  public static final byte CTRL_REG2 = 0x21;
  public static final byte CTRL_REG3 = 0x22;
  public static final byte CTRL_REG4 = 0x23;
  public static final byte INTERRUPT_CFG = 0x24;
  public static final byte INT_SOURCE = 0x25;
  public static final byte STATUS = 0x27;
  public static final byte PRESS_OUT_XL = 0x28;
  public static final byte TEMP_OUT_L = 0x2B;
  public static final byte FIFO_CTRL = 0x2E;
  public static final byte FIFO_STATUS = 0x2F;
  public static final byte THS_P_L = 0x30;
  public static final byte THS_P_H = 0x31;

  public static int getId(I2C device) throws IOException {
    return device.readRegister(WHO_AM_I);
  }

  public Lps25Sensor(I2C device) {
    super(device, LoggerFactory.getLogger(Lps25Sensor.class));
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public String getName() {
    return "LPS25";
  }

  @Override
  public String getDescription() {
    return "Pressure sensor: 260-1260 hPa";
  }

  //region Interrupt controlregister
  public void latchInterruptToSource(boolean flag) throws IOException {
    int value = flag ? 0b100 : 0;
    setControlRegister(INTERRUPT_CFG, 0b11111011, value);
  }

  public boolean isLatchInterruptToSource() throws IOException {
    return (readRegister(INTERRUPT_CFG) & 0b100) != 0;
  }

  public void latchInterruptToPressureLow(boolean flag) throws IOException {
    int value = flag ? 0b10 : 0;
    setControlRegister(INTERRUPT_CFG, 0b11111101, value);
  }

  public boolean isLatchInterruptToPressureLow() throws IOException {
    return (readRegister(INTERRUPT_CFG) & 0b10) != 0;
  }

  public void latchInterruptToPressureHigh(boolean flag) throws IOException {
    int value = flag ? 0b1 : 0;
    setControlRegister(INTERRUPT_CFG, 0b11111110, value);
  }

  public boolean isLatchInterruptToPressureHigh() throws IOException {
    return (readRegister(INTERRUPT_CFG) & 0b1) != 0;
  }
  //endregion

  public float getThresholdPressure() throws IOException {
    byte[] b = new byte[2];
    readRegister(THS_P_L, b, 0, 2);
    return ((b[0] * 0xff) | ((b[1] << 8) & 0xff)) / 16f;
  }

  //region Pressure Threshold register
  public void setThresholdPressure(float thresholdPressure) throws IOException {
    int val = Math.round(thresholdPressure * 16);
    write(THS_P_L, (byte) (val & 0xff));
    write(THS_P_H, (byte) ((val >> 8) & 0xff));
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
  public void powerDown() throws IOException {
    setControlRegister(CTRL_REG1, (byte)0b01111111, (byte)0b10000000);
  }

  public void setDataRate(DataRate rate) throws IOException {
    setControlRegister(CTRL_REG1, 0b0001111, (rate.getMask() << 4));
  }

  public void setInterruptGeneration(boolean flag) throws IOException {
    int value = flag ? 0b1000 : 0;
    setControlRegister(CTRL_REG1, 0b11110111, value);
  }

  public boolean isInterruptGenerationEnabled() throws IOException {
    return (readRegister(CTRL_REG1) & 0b1000) != 0;
  }

  public void setBlockUpdate(boolean flag) throws IOException {
    int value = flag ? 0b100 : 0;
    setControlRegister(CTRL_REG1, 0b11111011, value);
  }

  public boolean isBlockUpdateSet() throws IOException {
    return (readRegister(CTRL_REG1) & 0b100) != 0;
  }

  public void resetAutoZero(boolean flag) throws IOException {
    int value = flag ? 0b10 : 0;
    setControlRegister(CTRL_REG1, 0b11111101, value);
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

  public void reset() throws IOException {
    setControlRegister(CTRL_REG2, 0b11111011, 0b100);
    delay(50);
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

  public void setSignalOnInterrupt(DataReadyInterrupt flag) throws IOException {
    int value = flag.getMask();
    setControlRegister(CTRL_REG3, 0b11111100, value);
  }

  public DataReadyInterrupt isSignalOnInterrupt() throws IOException {
    int mask = (readRegister(CTRL_REG3) & 0b11);
    for (DataReadyInterrupt dataReadyInterrupt : DataReadyInterrupt.values()) {
      if (mask == dataReadyInterrupt.getMask()) {
        return dataReadyInterrupt;
      }
    }
    return DataReadyInterrupt.ORDER_OF_PRIORITY;
  }
  //endregion

  //region Control Register 4

  public void enabledFiFoEmptyInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b001000 : 0;
    setControlRegister(CTRL_REG4, 0b11110111, value);
  }

  public boolean isFiFoEmptyEnabled() throws IOException {
    return (readRegister(CTRL_REG4) & 0b001000) != 0;
  }

  public void enableFiFoWatermarkInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b00100 : 0;
    setControlRegister(CTRL_REG4, 0b11111011, value);
  }

  public boolean isFiFoWatermarkInterruptEnabled() throws IOException {
    return (readRegister(CTRL_REG4) & 0b00100) != 0;
  }

  public void enableFiFoOverrunInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b0010 : 0;
    setControlRegister(CTRL_REG4, 0b11111101, value);
  }

  public boolean isFiFoOverrunInterruptEnabled() throws IOException {
    return (readRegister(CTRL_REG4) & 0b0010) != 0;
  }

  public void setDataReadyInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b001 : 0;
    setControlRegister(CTRL_REG4, 0b11111110, value);
  }
  public boolean isDataReadyInterrupt() throws IOException {
    return (readRegister(CTRL_REG4) & 0b001) != 0;

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

  public int getReferencePressure() throws IOException {
    byte[] data = new byte[3];
    readRegister(REF_P_XL, data);
    return (data[2] << 16 | ((data[1] & 0xff) << 8) | (data[0] & 0xff));
  }

  //region Reference Pressure Registers
  public void setReferencePressure(int value) throws IOException {
    byte[] data = new byte[3];
    data[0] = (byte) (value & 0xff);
    data[1] = (byte) (value >> 8 & 0xff);
    data[2] = (byte) (value >> 16 & 0xff);
    write(REF_P_XL, data);
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

  //region Interrupt Source Register
  public InterruptSource[] getInterruptSource() throws IOException {
    int val = readRegister(INT_SOURCE);
    List<InterruptSource> sourceList = new ArrayList<>();
    if ((val & 0b100000000) != 0) {
      sourceList.add(InterruptSource.BOOT);
    }
    if ((val & 0b1) != 0) {
      sourceList.add(InterruptSource.PRESSURE_HIGH);
    }
    if ((val & 0b10) != 0) {
      sourceList.add(InterruptSource.PRESSURE_LOW);
    }
    if ((val & 0b100) != 0) {
      sourceList.add(InterruptSource.INTERRUPT_ACTIVE);
    }

    return sourceList.toArray(new InterruptSource[]{});
  }
  //endregion

  //region FiFo Status Register
  public FiFoStatus getFiFoStatus() throws IOException {
    int val = readRegister(FIFO_STATUS);
    return new FiFoStatus((val & 0b10000000) != 0, ((val & 0b1000000) != 0), val & 0b11111);
  }
  //endregion

  //region Device Status Register
  public Status[] getStatus() throws IOException {
    int val = readRegister(STATUS);
    List<Status> sourceList = new ArrayList<>();
    if ((val & 0b100000) != 0) {
      sourceList.add(Status.TEMPERATURE_OVERRUN);
    }
    if ((val & 0b10000) != 0) {
      sourceList.add(Status.PRESSURE_OVERRUN);
    }
    if ((val & 0b10) != 0) {
      sourceList.add(Status.TEMPERATURE_DATA_AVAILABLE);
    }
    if ((val & 0b1) != 0) {
      sourceList.add(Status.PRESSURE_DATA_AVAILABLE);
    }
    return sourceList.toArray(new Status[]{});
  }
  //endregion

  //region Pressure Out Registers
  public float getPressure() throws IOException {
    byte[] pressureBuffer = new byte[3];
    readRegister(PRESS_OUT_XL, pressureBuffer, 0, pressureBuffer.length);
    int rawPressure = (pressureBuffer[2] << 16 | ((pressureBuffer[1] & 0xff) << 8) | (pressureBuffer[0] & 0xff));
    if ((rawPressure & 0x800000) != 0) {
      rawPressure = (0xff000000) | rawPressure; // It's now negative
    }
    return rawPressure / 4096.0f;
  }
  //endregion

  //region Temperature Out Registers
  public float getTemperature() throws IOException {
    byte[] temperatureBuffer = new byte[2];
    readRegister(TEMP_OUT_L, temperatureBuffer, 0, temperatureBuffer.length);
    int rawTemperature = ((temperatureBuffer[1] & 0xff) << 8) | (temperatureBuffer[0] & 0xff);
    return rawTemperature / 100.0f;
  }
  //endregion


  private void setControlRegister(int register, int mask, int value) throws IOException {
    int ctl1 = readRegister(register) & 0xff;
    ctl1 = (ctl1 & mask) | value;
    write(register, (byte) ctl1);
  }
}
