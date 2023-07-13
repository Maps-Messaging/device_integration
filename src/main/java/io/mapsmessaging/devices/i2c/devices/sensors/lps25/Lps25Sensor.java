package io.mapsmessaging.devices.i2c.devices.sensors.lps25;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.*;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class Lps25Sensor extends I2CDevice implements Sensor {

  public static final byte REF_P_XL = 0x08;
  public static final byte REF_P_L = 0x09;
  public static final byte REF_P_H = 0x0A;

  public static final byte WHO_AM_I = 0x0F;

  public static final byte PRESS_OUT_XL = 0x28;

  public static final byte TEMP_OUT_L = 0x2B;

  public static final byte FIFO_STATUS = 0x2F;

  public static final byte THS_P_L = 0x30;
  public static final byte THS_P_H = 0x31;

  private final Control1 control1;
  private final Control2 control2;
  private final Control3 control3;
  private final Control4 control4;
  private final InterruptControl interruptControl;
  private final InterruptSourceRegister interruptSource;
  private final FiFoControl fiFoControl;
  private final StatusRegister statusRegister;

  public Lps25Sensor(I2C device) throws IOException {
    super(device, LoggerFactory.getLogger(Lps25Sensor.class));
    control1 = new Control1(this);
    control2 = new Control2(this);
    control3 = new Control3(this);
    control4 = new Control4(this);
    interruptSource = new InterruptSourceRegister(this);
    interruptControl = new InterruptControl(this);
    fiFoControl = new FiFoControl(this);
    statusRegister = new StatusRegister(this);
  }

  public static int getId(I2C device) {
    return device.readRegister(WHO_AM_I);
  }

  public String toString() {
    return "Control1:" + control1.toString()
        + " Control2:" + control2.toString()
        + " Control3:" + control3.toString()
        + " Control4:" + control4.toString()
        + " fifo:" + fiFoControl.toString()
        + " status:" + statusRegister.toString()
        + " int Ctl:" + interruptControl.toString()
        + " int src:" + interruptSource.toString();
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
  public void enableLatchInterrupt(boolean flag) throws IOException {
    interruptControl.setLatchInterruptEnable(flag);
  }

  public boolean isLatchInterruptEnabled() throws IOException {
    return interruptControl.isLatchInterruptEnabled();
  }

  public void latchInterruptToPressureLow(boolean flag) throws IOException {
    interruptControl.setInterruptOnLow(flag);
  }

  public boolean isLatchInterruptToPressureLow() throws IOException {
    return interruptControl.isInterruptOnLowEnabled();
  }

  public void latchInterruptToPressureHigh(boolean flag) throws IOException {
    interruptControl.setInterruptOnHigh(flag);
  }

  public boolean isLatchInterruptToPressureHigh() throws IOException {
    return interruptControl.isInterruptOnHighEnabled();
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

  //region Control Register 1
  public DataRate getDataRate() throws IOException {
    return control1.getDataRate();
  }

  public void setDataRate(DataRate rate) throws IOException {
    control1.setDataRate(rate);
  }

  public boolean getPowerDownMode() {
    return control1.getPowerDownMode();
  }

  public void setPowerDownMode(boolean flag) throws IOException {
    control1.setPowerDownMode(flag);
  }

  public void setInterruptGeneration(boolean flag) throws IOException {
    control1.setInterruptGenerationEnabled(flag);
  }

  public boolean isInterruptGenerationEnabled() throws IOException {
    return control1.isInterruptGenerationEnabled();
  }

  public void setBlockUpdate(boolean flag) throws IOException {
    control1.setBlockUpdate(flag);
  }

  public boolean isBlockUpdateSet() throws IOException {
    return control1.isBlockUpdateSet();
  }

  public void resetAutoZero(boolean flag) throws IOException {
    control1.resetAutoZero(flag);
  }
  //endregion

  //region Control Register 2
  public void boot() throws IOException {
    control2.boot();
  }

  public void enableFiFo(boolean flag) throws IOException {
    control2.enableFiFo(flag);
  }

  public boolean isFiFoEnabled() throws IOException {
    return control2.isFiFoEnabled();
  }

  public void enableStopFiFoOnThreshold(boolean flag) throws IOException {
    control2.enableStopFiFoOnThreshold(flag);
  }

  public boolean isStopFiFoOnThresholdEnabled() throws IOException {
    return control2.isStopFiFoOnThresholdEnabled();
  }

  public void reset() throws IOException {
    control2.reset();
  }

  public void enableOneShot(boolean flag) throws IOException {
    control2.enableOneShot(flag);
  }

  public boolean isOneShotEnabled() throws IOException {
    return control2.isOneShotEnabled();
  }
  //endregion

  //region Control Register 3
  public boolean isInterruptActive() throws IOException {
    return control3.isInterruptActive();
  }

  public boolean isPushPullDrainActive() throws IOException {
    return control3.isPushPullDrainInterruptActive();
  }

  public void setSignalOnInterrupt(DataReadyInterrupt flag) throws IOException {
    control3.setSignalOnInterrupt(flag);
  }

  public DataReadyInterrupt isSignalOnInterrupt() throws IOException {
    return control3.isSignalOnInterrupt();
  }
  //endregion

  //region Control Register 4
  public void enabledFiFoEmptyInterrupt(boolean flag) throws IOException {
    control4.enabledFiFoEmptyInterrupt(flag);
  }

  public boolean isFiFoEmptyEnabled() throws IOException {
    return control4.isFiFoEmptyEnabled();
  }

  public void enableFiFoWatermarkInterrupt(boolean flag) throws IOException {
    control4.enableFiFoWatermarkInterrupt(flag);
  }

  public boolean isFiFoWatermarkInterruptEnabled() throws IOException {
    return control4.isFiFoWatermarkInterruptEnabled();
  }

  public void enableFiFoOverrunInterrupt(boolean flag) throws IOException {
    control4.enableFiFoOverrunInterrupt(flag);
  }

  public boolean isFiFoOverrunInterruptEnabled() throws IOException {
    return control4.isFiFoOverrunInterruptEnabled();
  }

  public boolean isDataReadyInterrupt() throws IOException {
    return control4.isDataReadyInterrupt();
  }

  public void setDataReadyInterrupt(boolean flag) throws IOException {
    control4.setDataReadyInterrupt(flag);
  }
  //endregion

  //region FiFo Control Register
  public FiFoMode getFifoMode() throws IOException {
    return fiFoControl.getFifoMode();
  }

  public void setFifoMode(FiFoMode mode) throws IOException {
    fiFoControl.setFifoMode(mode);
  }

  public int getFiFoWaterMark() throws IOException {
    return fiFoControl.getFiFoWaterMark();
  }

  public void setFiFoWaterMark(int waterMark) throws IOException {
    fiFoControl.setFiFoWaterMark(waterMark);
  }
  //endregion

  public int getReferencePressure() throws IOException {
    byte[] data = new byte[3];
    data[0] = (byte) (readRegister(REF_P_XL) & 0xff);
    data[1] = (byte) (readRegister(REF_P_L) & 0xff);
    data[2] = (byte) (readRegister(REF_P_H) & 0xff);
    return (data[2] << 16 | ((data[1] & 0xff) << 8) | (data[0] & 0xff));
  }

  //region Reference Pressure Registers
  public void setReferencePressure(int value) throws IOException {
    byte[] data = new byte[3];
    data[0] = (byte) (value & 0xff);
    data[1] = (byte) (value >> 8 & 0xff);
    data[2] = (byte) (value >> 16 & 0xff);
    write(REF_P_XL, data[0]);
    write(REF_P_XL + 1, data[1]);
    write(REF_P_XL + 2, data[2]);
  }
  //endregion

  //region Interrupt Source Register
  public InterruptSource[] getInterruptSource() throws IOException {
    return interruptSource.getInterruptSource();
  }
  //endregion

  //region FiFo Status Register
  public FiFoStatus getFiFoStatus() throws IOException {
    int val = readRegister(FIFO_STATUS);
    return new FiFoStatus(
        (val & 0b10000000) != 0,
        (val & 0b1000000) != 0,
        val & 0b11111);
  }
  //endregion

  //region Device Status Register
  public Status[] getStatus() throws IOException {
    return statusRegister.getStatus();
  }
  //endregion

  //region Pressure Out Registers
  public float getPressure() throws IOException {
    byte[] pressureBuffer = new byte[3];
    readRegister(PRESS_OUT_XL | 0x80, pressureBuffer);
    int rawPressure = (pressureBuffer[2] << 16 | ((pressureBuffer[1] & 0xff) << 8) | (pressureBuffer[0] & 0xff));
    if ((rawPressure & 0x800000) != 0) {
      rawPressure = rawPressure - 0xFFFFFF;
    }
    return rawPressure / 4096.0f;
  }
  //endregion

  //region Temperature Out Registers
  public float getTemperature() throws IOException {
    byte[] temperatureBuffer = new byte[2];
    readRegister(TEMP_OUT_L | 0x80, temperatureBuffer);
    int rawTemperature = ((temperatureBuffer[1] & 0xff) << 8) | (temperatureBuffer[0] & 0xff);
    if ((rawTemperature & 0x8000) != 0) {
      rawTemperature = rawTemperature - 0xFFFF;
    }

    return rawTemperature / 480.0f + 42.5f;
  }
  //endregion

  private void setControlRegister(int register, int mask, int value) throws IOException {
    int ctl1 = readRegister(register) & 0xff;
    ctl1 = (ctl1 & mask) | value;
    write(register, (byte) ctl1);
  }

}
