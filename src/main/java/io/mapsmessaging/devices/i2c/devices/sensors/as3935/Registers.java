package io.mapsmessaging.devices.i2c.devices.sensors.as3935;

import io.mapsmessaging.devices.i2c.I2CDevice;

public class Registers {
  private final I2CDevice sensor;

  public Registers(I2CDevice sensor) {
    this.sensor = sensor;
  }

  // Register addresses
  private static final int AFE_GAIN_ADDR = 0x00;
  private static final int THRESHOLD_ADDR = 0x01;
  private static final int LIGHTNING_REG_ADDR = 0x02;
  private static final int INTERRUPT_ADDR = 0x03;
  private static final int LIGHTNING_STRIKE_MSB_ADDR = 0x04;
  private static final int LIGHTNING_STRIKE_LSB_ADDR = 0x05;
  private static final int LIGHTNING_STRIKE_BITS_0_TO_4_ADDR = 0x06;
  private static final int DISTANCE_ADDR = 0x07;
  private static final int TUN_CAP_ADDR = 0x08;
  private static final int CALIB_SRCO_TRCO_ADDR = 0x3A;
  private static final int CALIB_SCRO_SRCO_ADDR = 0x3B;

  // Register bit positions
  private static final int AFE_GAIN_PD_BIT = 0;
  private static final int AFE_GAIN_BOOST_BITS = 1;
  private static final int THRESHOLD_WDTH_BITS = 0;
  private static final int THRESHOLD_NF_LEV_BITS = 4;
  private static final int LIGHTNING_REG_SREJ_BITS = 0;
  private static final int LIGHTNING_REG_MIN_NUM_LIGH_BITS = 4;
  private static final int LIGHTNING_REG_CL_STAT_BIT = 6;
  private static final int ENERGY_INT_BITS = 0;
  private static final int ENERGY_MASK_DISTURBER_BIT = 5;
  private static final int ENERGY_DIV_RATIO_BITS = 6;
  private static final int DISTANCE_EST_BITS = 0;
  private static final int TUN_CAP_CAP_BITS = 0;
  private static final int TUN_CAP_DISP_TRCO_BIT = 6;
  private static final int TUN_CAP_DISP_SRCO_BIT = 7;
  private static final int CALIB_SRCO_TRCO_CALIB_TRCO_BIT = 6;
  private static final int CALIB_SRCO_TRCO_CALIB_TRCO_DONE_BIT = 7;
  private static final int CALIB_SCRO_SRCO_CALIB_SRCO_BIT = 6;
  private static final int CALIB_SCRO_SRCO_CALIB_SRCO_DONE_BIT = 7;

  // Read register value from the sensor
  private int readRegister(int register) {
    return sensor.readRegister(register);
  }

  // Write register value to the sensor
  private void writeRegister(int register, int value) {
    sensor.write(register, (byte) value);
  }

  // Methods to get specific register values

  // AFE_GAIN Register : 0
  public boolean isAFE_PowerDown() {
    int value = readRegister(AFE_GAIN_ADDR);
    return (value & (1 << AFE_GAIN_PD_BIT)) != 0;
  }

  public int getAFE_GainBoost() {
    int value = readRegister(AFE_GAIN_ADDR);
    return (value >> AFE_GAIN_BOOST_BITS) & 0x1F;
  }

  public void setAFE_PowerDown(boolean powerDown) {
    int value = readRegister(AFE_GAIN_ADDR);
    if (powerDown) {
      value |= (1 << AFE_GAIN_PD_BIT);
    } else {
      value &= ~(1 << AFE_GAIN_PD_BIT);
    }
    writeRegister(AFE_GAIN_ADDR, value);
  }

  public void setAFE_GainBoost(int gainBoost) {
    int value = readRegister(AFE_GAIN_ADDR);
    value &= ~((0x1F) << AFE_GAIN_BOOST_BITS);
    value |= (gainBoost << AFE_GAIN_BOOST_BITS) & ((0x1F) << AFE_GAIN_BOOST_BITS);
    writeRegister(AFE_GAIN_ADDR, value);
  }

  // THRESHOLD Register : 1
  public int getWatchdogThreshold() {
    int value = readRegister(THRESHOLD_ADDR);
    return (value >> THRESHOLD_WDTH_BITS) & 0x0F;
  }

  public int getNoiseFloorLevel() {
    int value = readRegister(THRESHOLD_ADDR);
    return (value >> THRESHOLD_NF_LEV_BITS) & 0x07;
  }

  public void setWatchdogThreshold(int threshold) {
    int value = readRegister(THRESHOLD_ADDR);
    value &= ~((0x0F) << THRESHOLD_WDTH_BITS);
    value |= (threshold << THRESHOLD_WDTH_BITS) & ((0x0F) << THRESHOLD_WDTH_BITS);
    writeRegister(THRESHOLD_ADDR, value);
  }

  public void setNoiseFloorLevel(int level) {
    int value = readRegister(THRESHOLD_ADDR);
    value &= ~((0x07) << THRESHOLD_NF_LEV_BITS);
    value |= (level << THRESHOLD_NF_LEV_BITS) & ((0x07) << THRESHOLD_NF_LEV_BITS);
    writeRegister(THRESHOLD_ADDR, value);
  }

  // LIGHTNING_REG Register : 2
  public int getSpikeRejection() {
    int value = readRegister(LIGHTNING_REG_ADDR);
    return (value >> LIGHTNING_REG_SREJ_BITS) & 0x0F;
  }

  public int getMinNumLightning() {
    int value = readRegister(LIGHTNING_REG_ADDR);
    return (value >> LIGHTNING_REG_MIN_NUM_LIGH_BITS) & 0x03;
  }

  public boolean isClearStatisticsEnabled() {
    int value = readRegister(LIGHTNING_REG_ADDR);
    return (value & (1 << LIGHTNING_REG_CL_STAT_BIT)) != 0;
  }

  public void setSpikeRejection(int rejection) {
    int value = readRegister(LIGHTNING_REG_ADDR);
    value &= ~((0x0F) << LIGHTNING_REG_SREJ_BITS);
    value |= (rejection << LIGHTNING_REG_SREJ_BITS) & ((0x0F) << LIGHTNING_REG_SREJ_BITS);
    writeRegister(LIGHTNING_REG_ADDR, value);
  }

  public void setMinNumLightning(int numLightning) {
    int value = readRegister(LIGHTNING_REG_ADDR);
    value &= ~((0x03) << LIGHTNING_REG_MIN_NUM_LIGH_BITS);
    value |= (numLightning << LIGHTNING_REG_MIN_NUM_LIGH_BITS) & ((0x03) << LIGHTNING_REG_MIN_NUM_LIGH_BITS);
    writeRegister(LIGHTNING_REG_ADDR, value);
  }

  public void setClearStatisticsEnabled(boolean enabled) {
    int value = readRegister(LIGHTNING_REG_ADDR);
    if (enabled) {
      value |= (1 << LIGHTNING_REG_CL_STAT_BIT);
    } else {
      value &= ~(1 << LIGHTNING_REG_CL_STAT_BIT);
    }
    writeRegister(LIGHTNING_REG_ADDR, value);
  }

  // Interrupt Register

  public int getInterruptReason(){
    return readRegister(INTERRUPT_ADDR) & 0xf;
  }


  public boolean isMaskDisturberEnabled() {
    int value = readRegister(INTERRUPT_ADDR);
    return (value & (1 << ENERGY_MASK_DISTURBER_BIT)) != 0;
  }

  public int getEnergyDivRatio() {
    int value = readRegister(INTERRUPT_ADDR);
    return (value >> ENERGY_DIV_RATIO_BITS) & 0x03;
  }

  public void setMaskDisturberEnabled(boolean enabled) {
    int value = readRegister(INTERRUPT_ADDR);
    if (enabled) {
      value |= (1 << ENERGY_MASK_DISTURBER_BIT);
    } else {
      value &= ~(1 << ENERGY_MASK_DISTURBER_BIT);
    }
    writeRegister(INTERRUPT_ADDR, value);
  }

  public void setEnergyDivRatio(int divRatio) {
    int value = readRegister(INTERRUPT_ADDR);
    value &= ~((0x03) << ENERGY_DIV_RATIO_BITS);
    value |= (divRatio << ENERGY_DIV_RATIO_BITS) & ((0x03) << ENERGY_DIV_RATIO_BITS);
    writeRegister(INTERRUPT_ADDR, value);
  }

  // DISTANCE Register
  public int getDistanceEstimation() {
    int value = readRegister(DISTANCE_ADDR);
    value = value & 0x3F;
    if(value == 63){
      return -1;
    }
    return value;
  }

  // TUN_CAP Register
  public int getTuningCap() {
    int value = readRegister(TUN_CAP_ADDR);
    return value & 0x0F;
  }

  public boolean isDispTRCOEnabled() {
    int value = readRegister(TUN_CAP_ADDR);
    return (value & (1 << TUN_CAP_DISP_TRCO_BIT)) != 0;
  }

  public boolean isDispSRCOEnabled() {
    int value = readRegister(TUN_CAP_ADDR);
    return (value & (1 << TUN_CAP_DISP_SRCO_BIT)) != 0;
  }

  public void setTuningCap(int cap) {
    int value = readRegister(TUN_CAP_ADDR);
    value &= ~((0x0F) << TUN_CAP_CAP_BITS);
    value |= cap & ((0x0F) << TUN_CAP_CAP_BITS);
    writeRegister(TUN_CAP_ADDR, value);
  }

  public void setDispTRCOEnabled(boolean enabled) {
    int value = readRegister(TUN_CAP_ADDR);
    if (enabled) {
      value |= (1 << TUN_CAP_DISP_TRCO_BIT);
    } else {
      value &= ~(1 << TUN_CAP_DISP_TRCO_BIT);
    }
    writeRegister(TUN_CAP_ADDR, value);
  }

  public void setDispSRCOEnabled(boolean enabled) {
    int value = readRegister(TUN_CAP_ADDR);
    if (enabled) {
      value |= (1 << TUN_CAP_DISP_SRCO_BIT);
    } else {
      value &= ~(1 << TUN_CAP_DISP_SRCO_BIT);
    }
    writeRegister(TUN_CAP_ADDR, value);
  }

  // CALIB_SRCO_TRCO Register
  public boolean isTRCOCalibrationSuccessful() {
    int value = readRegister(CALIB_SRCO_TRCO_ADDR);
    return (value & (1 << CALIB_SRCO_TRCO_CALIB_TRCO_DONE_BIT)) != 0;
  }

  // CALIB_SCRO_SRCO Register
  public boolean isSRCOCalibrationSuccessful() {
    int value = readRegister(CALIB_SCRO_SRCO_ADDR);
    return (value & (1 << CALIB_SCRO_SRCO_CALIB_SRCO_DONE_BIT)) != 0;
  }

  // LIGHTNING_STRIKE Register
  public int getEnergy() {
    int msb = readRegister(LIGHTNING_STRIKE_MSB_ADDR);
    int lsb = readRegister(LIGHTNING_STRIKE_LSB_ADDR);
    int bits0to4 = readRegister(LIGHTNING_STRIKE_BITS_0_TO_4_ADDR);
    return ((bits0to4 & 0x1F) << 16) | (msb << 8) | lsb;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("AS3935 Sensor Registers:\n");
    sb.append("AFE_GAIN: Power-Down = ").append(isAFE_PowerDown()).append(", Gain Boost = ").append(getAFE_GainBoost()).append("\n");
    sb.append("THRESHOLD: Watchdog Threshold = ").append(getWatchdogThreshold()).append(", Noise Floor Level = ").append(getNoiseFloorLevel()).append("\n");
    sb.append("LIGHTNING_REG: Spike Rejection = ").append(getSpikeRejection()).append(", Min Num Lightning = ").append(getMinNumLightning()).append(", Clear Statistics = ").append(isClearStatisticsEnabled()).append("\n");
    sb.append("ENERGY: Energy = ").append(getEnergy()).append(", Mask Disturber = ").append(isMaskDisturberEnabled()).append(", Energy Div Ratio = ").append(getEnergyDivRatio()).append("\n");
    sb.append("DISTANCE: Distance Estimation = ").append(getDistanceEstimation()).append("\n");
    sb.append("TUN_CAP: Tuning Cap = ").append(getTuningCap()).append(", Disp TRCO = ").append(isDispTRCOEnabled()).append(", Disp SRCO = ").append(isDispSRCOEnabled()).append("\n");
    sb.append("CALIB_SRCO_TRCO: TRCO Calibration Successful = ").append(isTRCOCalibrationSuccessful()).append("\n");
    sb.append("CALIB_SCRO_SRCO: SRCO Calibration Successful = ").append(isSRCOCalibrationSuccessful()).append("\n");
    return sb.toString();
  }
}

