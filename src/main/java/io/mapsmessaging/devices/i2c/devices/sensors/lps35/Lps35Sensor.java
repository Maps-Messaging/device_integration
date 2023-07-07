package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.*;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Original CPP source <a href="https://github.com/adafruit/Adafruit_LPS35HW/blob/master/Adafruit_LPS35HW.cpp">...</a>
 */

public class Lps35Sensor extends I2CDevice {

  public static final byte INTERRUPT_CFG = 0x0B;
  public static final byte THS_P_L = 0x0C;
  public static final byte THS_P_H = 0x0D;
  public static final byte WHO_AM_I = 0x0F;
  public static final byte CTRL_REG1 = 0x10;
  public static final byte CTRL_REG2 = 0x11;
  public static final byte CTRL_REG3 = 0x12;
  public static final byte FIFO_CTRL = 0x14;
  public static final byte REF_P_XL = 0x15;

  public static final byte RES_CONF = 0x1A;
  public static final byte INT_SOURCE = 0x25;
  public static final byte FIFO_STATUS = 0x26;
  public static final byte STATUS = 0x27;
  public static final byte PRESS_OUT_XL = 0x28;
  public static final byte TEMP_OUT_L = 0x2B;


  private final Logger logger = LoggerFactory.getLogger(Lps35Sensor.class);

  public Lps35Sensor(I2C device) {
    super(device);
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


  //region Interrupt Config Register
  public void enableAutoRifp(boolean flag) {
    int value = flag?0b10000000:0;
    setControlRegister(INTERRUPT_CFG, 0b01111111, value);
  }

  public boolean isAutoRifpEnabled(){
    return (readRegister(INTERRUPT_CFG) & 0b01000000) != 0;
  }

  public void resetAutoRifp() {
    setControlRegister(INTERRUPT_CFG, 0b10111111, 0b01000000);
  }

  public void enableAutoZeo(boolean flag) {
    int value = flag?0b00100000:0;
    setControlRegister(INTERRUPT_CFG, 0b11011111, value);
  }

  public boolean isAutoZeroEnabled(){
    return (readRegister(INTERRUPT_CFG) & 0b00100000) != 0;
  }

  public void resetAutoZero() {
    setControlRegister(INTERRUPT_CFG, 0b11101111, 0b00010000);
  }

  public void enableInterrupt(boolean flag) {
    int value = flag?0b1000:0;
    setControlRegister(INTERRUPT_CFG, 0b11110111, value);
  }

  public boolean isInterruptEnabled(){
    return (readRegister(INTERRUPT_CFG) & 0b00001000) != 0;
  }

  public void latchInterruptToSource(boolean flag) {
    int value = flag?0b100:0;
    setControlRegister(INTERRUPT_CFG, 0b11111011, value);
  }

  public boolean isLatchInterruptToSource(){
    return (readRegister(INTERRUPT_CFG) & 0b100) != 0;
  }

  public void latchInterruptToPressureLow(boolean flag) {
    int value = flag?0b10:0;
    setControlRegister(INTERRUPT_CFG, 0b11111101, value);
  }

  public boolean isLatchInterruptToPressureLow(){
    return (readRegister(INTERRUPT_CFG) & 0b10) != 0;
  }

  public void latchInterruptToPressureHigh(boolean flag) {
    int value = flag?0b1:0;
    setControlRegister(INTERRUPT_CFG, 0b11111110, value);
  }

  public boolean isLatchInterruptToPressureHigh(){
    return (readRegister(INTERRUPT_CFG) & 0b1) != 0;
  }
  //endregion

  //region Pressure Threshold register
  public void setThresholdPressure(float thresholdPressure) {
    int val = Math.round(thresholdPressure * 16);
    write(THS_P_L, (byte)(val&0xff));
    write(THS_P_H, (byte)((val>>8)&0xff));
  }

  public float getThresholdPressure() {
    byte[] b = new byte[2];
    readRegister(THS_P_L, b, 0, 2);
    return ((b[0] *0xff) | ((b[1]<<8)&0xff)) / 16f;
  }
  //endregion

  //region Who Am I register
  public int whoAmI(){
    return readRegister(WHO_AM_I ) & 0xff;
  }
  //endregion

  //region Control Register 1
  public void setDataRate(DataRate rate) {
    setControlRegister(CTRL_REG1, 0b0001111, (rate.getMask() << 4));
  }

  public DataRate getDataRate(){
    int ctl1 = (readRegister(CTRL_REG1) & 0xff >> 4);
    for(DataRate rate:DataRate.values()){
      if(rate.getMask() == ctl1){
        return rate;
      }
    }
    return DataRate.RATE_ONE_SHOT;
  }

  public void setLowPassFilter(boolean flag){
    int value = flag?0b1000:0;
    setControlRegister(CTRL_REG1, 0b11110111, value);
  }

  public boolean isLowPassFilterSet(){
    return (readRegister(CTRL_REG1) & 0b1000) != 0;
  }

  public void setLowPassFilterConfig(boolean flag){
    int value = flag?0b100:0;
    setControlRegister(CTRL_REG1, 0b11111011, value);
  }

  public boolean isLowPassFilterConfigSet(){
    return (readRegister(CTRL_REG1) & 0b100) != 0;
  }

  public void setBlockUpdate(boolean flag){
    int value = flag?0b10:0;
    setControlRegister(CTRL_REG1, 0b11111101, value);
  }

  public boolean isBlockUpdateSet(){
    return (readRegister(CTRL_REG1) & 0b10) != 0;
  }
  //endregion

  //region Control Register 2
  public void boot() {
    setControlRegister(CTRL_REG2, 0b01111111, 0b10000000);
    delay(50);
  }

  public void enableFiFo(boolean flag) {
    int value = flag?0b01000000:0;
    setControlRegister(CTRL_REG2, 0b10111111, value);
  }

  public boolean isFiFoEnabled(){
    return (readRegister(CTRL_REG2) & 0b01000000) != 0;
  }

  public void enableStopFiFoOnThreshold(boolean flag) {
    int value = flag?0b00100000:0;
    setControlRegister(CTRL_REG2, 0b11011111, value);
  }

  public boolean isStopFiFoOnThresholdEnabled(){
    return (readRegister(CTRL_REG2) & 0b00100000) != 0;
  }

  public void reset() {
    setControlRegister(CTRL_REG2, 0b11111011, 0b100);
    delay(50);
  }

  public void enableOneShot(boolean flag) {
    int value = flag?0b1:0;
    setControlRegister(CTRL_REG2, 0b11111110, value);
  }

  public boolean isOneShotEnabled(){
    return (readRegister(CTRL_REG2) & 0b1) != 0;
  }
  //endregion

  //region Control Register 3

  public boolean isInterruptActive() {
    return (readRegister(CTRL_REG3) & 0b10000000) != 0;
  }

  public boolean isPushPullDrainActive(){
    return (readRegister(CTRL_REG3) & 0b01000000) != 0;
  }

  public void enableFiFoDrainInterrupt(boolean flag) {
    int value = flag?0b00100000:0;
    setControlRegister(CTRL_REG3, 0b11011111, value);
  }

  public boolean isFiFoDrainInterruptEnabled(){
    return (readRegister(CTRL_REG3) & 0b00100000) != 0;
  }

  public void enableFiFoWatermarkInterrupt(boolean flag) {
    int value = flag?0b0010000:0;
    setControlRegister(CTRL_REG3, 0b11101111, value);
  }

  public boolean isFiFoWatermarkInterruptEnabled(){
    return (readRegister(CTRL_REG3) & 0b0010000) != 0;
  }

  public void enableFiFoOverrunInterrupt(boolean flag) {
    int value = flag?0b001000:0;
    setControlRegister(CTRL_REG3, 0b11110111, value);
  }

  public boolean isFiFoOverrunInterruptEnabled(){
    return (readRegister(CTRL_REG3) & 0b001000) != 0;
  }


  public void setSignalOnInterrupt(DataReadyInterrupt flag) {
    int value = flag.getMask();
    setControlRegister(CTRL_REG3, 0b11111100, value);
  }

  public DataReadyInterrupt isSignalOnInterrupr(){
    int mask = (readRegister(CTRL_REG3) & 0b11);
    for(DataReadyInterrupt dataReadyInterrupt:DataReadyInterrupt.values()){
      if(mask == dataReadyInterrupt.getMask()){
        return dataReadyInterrupt;
      }
    }
    return DataReadyInterrupt.ORDER_OF_PRIORITY;
  }
  //endregion

  //region FiFo Control Register

  public void setFifoMode(FiFoMode mode) {
    setControlRegister(FIFO_CTRL, 0b11111, mode.getMask());
  }

  public FiFoMode getFifoMode() {
    int mask = readRegister(FIFO_CTRL) >> 5;
    for(FiFoMode mode:FiFoMode.values()){
      if(mode.getMask() == mask){
        return mode;
      }
    }
    return FiFoMode.BYPASS;
  }

  public void setFiFoWaterMark(int waterMark){
    setControlRegister(FIFO_CTRL, 0b11100000, (waterMark & 0b11111));
  }

  public int getFiFoWaterMark() {
    return (readRegister(FIFO_CTRL) & 0b11111);
  }
  //endregion

  //region Reference Pressure Registers
  public void setReferencePressure(int value){
    byte[] data = new byte[3];
    data[0] = (byte)(value & 0xff);
    data[1] = (byte)(value>>8 & 0xff);
    data[2] = (byte)(value>>16 & 0xff);
    write(REF_P_XL, data);
  }

  public int getReferencePressure(){
    byte[] data = new byte[3];
    readRegister(REF_P_XL, data);
    return (data[2]<<16 | ((data[1] & 0xff) <<8)| (data[0] & 0xff));
  }
  //endregion

  //region Low Power Mode Registers
  public void setLowPowerMode(boolean flag){
    int value = flag?0b1:0;
    setControlRegister(RES_CONF, 0b0, value);
  }

  public boolean isLowPowerModeEnabled(){
    return (readRegister(RES_CONF) & 0b1) != 0;
  }
  //endregion

  //region Interrupt Source Register
  public InterruptSource[] getInterruptSource(){
    int val = readRegister(INT_SOURCE);
    List<InterruptSource> sourceList= new ArrayList<>();
    if((val & 0b100000000) != 0){
      sourceList.add(InterruptSource.BOOT);
    }
    if((val&0b1) != 0){
      sourceList.add(InterruptSource.PRESSURE_HIGH);
    }
    if((val&0b10) != 0){
      sourceList.add(InterruptSource.PRESSURE_LOW);
    }
    if((val&0b100) != 0){
      sourceList.add(InterruptSource.INTERRUPT_ACTIVE);
    }

    return sourceList.toArray(new InterruptSource[]{});
  }
  //endregion

  //region FiFo Status Register
  public FiFoStatus getFiFoStatus(){
    int val = readRegister(FIFO_STATUS);
    return new FiFoStatus((val & 0b10000000) != 0, ((val & 0b1000000)!= 0), val & 0b11111);
  }
  //endregion

  //region Device Status Register
  public Status[] getStatus(){
    int val = readRegister(STATUS);
    List<Status> sourceList= new ArrayList<>();
    if((val & 0b100000) != 0){
      sourceList.add(Status.TEMPERATURE_OVERRUN);
    }
    if((val&0b10000) != 0){
      sourceList.add(Status.PRESSURE_OVERRUN);
    }
    if((val&0b10) != 0){
      sourceList.add(Status.TEMPERATURE_DATA_AVAILABLE);
    }
    if((val&0b1) != 0){
      sourceList.add(Status.PRESSURE_DATA_AVAILABLE);
    }
    return sourceList.toArray(new Status[]{});
  }
  //endregion

  //region Pressure Out Registers
  public float getPressure() {
    byte[] pressureBuffer = new byte[3];
    readRegister(PRESS_OUT_XL, pressureBuffer, 0, pressureBuffer.length);
    int rawPressure = (pressureBuffer[2] << 16 | ((pressureBuffer[1] &0xff) << 8) | (pressureBuffer[0] & 0xff));
    if((rawPressure & 0x800000) != 0){
      rawPressure = (0xff000000) | rawPressure; // It's now negative
    }
    return rawPressure/4096.0f;
  }
  //endregion

  //region Temperature Out Registers
  public float getTemperature() {
    byte[] temperatureBuffer = new byte[2];
    readRegister(TEMP_OUT_L, temperatureBuffer, 0, temperatureBuffer.length);
    int rawTemperature = ((temperatureBuffer[1] &0xff) << 8) | (temperatureBuffer[0] & 0xff);
    return rawTemperature/100.0f;
  }
  //endregion


  private void setControlRegister(int register, int mask, int value){
    int ctl1 = readRegister(register) & 0xff;
    ctl1 = (ctl1 & mask)|value;
    write(register, (byte) ctl1);

  }

}
