package io.mapsmessaging.devices.i2c.devices.sensors.lps25;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.Resetable;
import io.mapsmessaging.devices.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.*;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class Lps25Sensor extends I2CDevice implements Sensor, Resetable {
  private static final int WHO_AM_I = 0xf;

  public static int getId(I2C device) {
    return device.readRegister(WHO_AM_I);
  }

  private final ResolutionRegister resolutionRegister;
  private final Control1 control1;
  private final Control2 control2;
  private final Control3 control3;
  private final Control4 control4;
  private final InterruptControl interruptControl;
  private final InterruptSourceRegister interruptSource;
  private final FiFoControl fiFoControl;
  private final StatusRegister statusRegister;
  private final TemperatureRegister temperatureRegister;
  private final PressureRegister pressureRegister;
  private final ReferencePressureRegister referencePressureRegister;
  private final FiFoStatusRegister fiFoStatusRegister;
  private final ThresholdPressureRegister thresholdPressureRegister;
  private final WhoAmIRegister whoAmIRegister;
  private final PressureOffset pressureOffset;

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
    temperatureRegister = new TemperatureRegister(this);
    pressureRegister = new PressureRegister(this);
    referencePressureRegister = new ReferencePressureRegister(this);
    fiFoStatusRegister = new FiFoStatusRegister(this);
    thresholdPressureRegister = new ThresholdPressureRegister(this);
    whoAmIRegister = new WhoAmIRegister(this);
    pressureOffset = new PressureOffset(this);
    resolutionRegister = new ResolutionRegister(this);
  }

  @Override
  public String toString() {
    return getName() + " - " + getDescription() + "\n" + registerMap.toString();
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

  public float getThresholdPressure() {
    return thresholdPressureRegister.getThreshold();
  }

  //region Pressure Threshold register
  public void setThresholdPressure(float thresholdPressure) throws IOException {
    thresholdPressureRegister.setThreshold(thresholdPressure);
  }
  //endregion

  //region Who Am I register
  public int whoAmI() {
    return whoAmIRegister.getWhoAmI();
  }
  //endregion

  //region Control Register 1
  public DataRate getDataRate() {
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

  public boolean isInterruptGenerationEnabled() {
    return control1.isInterruptGenerationEnabled();
  }

  public void setBlockUpdate(boolean flag) throws IOException {
    control1.setBlockUpdate(flag);
  }

  public boolean isBlockUpdateSet() {
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
    control2.boot();
  }

  @Override
  public void softReset() throws IOException {
    control2.reset();
  }

  public void enableOneShot(boolean flag) throws IOException {
    control2.enableOneShot(flag);
  }

  public boolean isOneShotEnabled() {
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

  public int getReferencePressure() {
    return referencePressureRegister.getReference();
  }

  //region Reference Pressure Registers
  public void setReferencePressure(int value) throws IOException {
    referencePressureRegister.setReference(value);
  }
  //endregion

  //region Interrupt Source Register
  public InterruptSource[] getInterruptSource() throws IOException {
    return interruptSource.getInterruptSource();
  }
  //endregion

  //region FiFo Status Register
  public FiFoStatus getFiFoStatus() throws IOException {
    return fiFoStatusRegister.getFiFoStatus();
  }
  //endregion

  //region Device Status Register
  public Status[] getStatus() throws IOException {
    return statusRegister.getStatus();
  }
  //endregion

  //region Pressure Out Registers
  public float getPressure() throws IOException {
    return pressureRegister.getPressure();
  }
  //endregion

  //region Temperature Out Registers
  public float getTemperature() throws IOException {
    return temperatureRegister.getTemperature();
  }
  //endregion

  public int getPressureOffset() throws IOException {
    return pressureOffset.getPressureOffset();
  }

  public void setPressureOffset(int value) throws IOException {
    pressureOffset.setPressureOffset(value);
  }

  public TemperatureAverage getAverageTemperature(){
    return resolutionRegister.getTemperatureAverage();
  }

  public PressureAverage getAveragePressure(){
    return resolutionRegister.getPressureAverage();
  }

  public void setAverageTemperature(TemperatureAverage ave) throws IOException {
    resolutionRegister.setTemperatureAverage(ave);
  }

  public void setAveragePressure(PressureAverage ave) throws IOException {
    resolutionRegister.setPressureAverage(ave);
  }
}
