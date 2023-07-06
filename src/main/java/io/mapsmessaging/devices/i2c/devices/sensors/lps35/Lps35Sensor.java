package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.util.Delay;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;


public class Lps35Sensor extends I2CDevice {

  public static final byte I2CADDR_DEFAULT = 0x5D;
  public static final byte INTERRUPT_CFG = 0x0B;
  public static final byte THS_P_L = 0x0C;
  public static final byte THS_P_H = 0x0D;
  public static final byte WHO_AM_I = 0x0F;
  public static final byte CTRL_REG1 = 0x10;
  public static final byte CTRL_REG2 = 0x11;
  public static final byte CTRL_REG3 = 0x12;
  public static final byte FIFO_CTRL = 0x14;
  public static final byte REF_P_XL = 0x15;
  public static final byte REF_P_L = 0x16;
  public static final byte REF_P_H = 0x17;
  public static final byte RPDS_L = 0x18;
  public static final byte RPDS_H = 0x19;
  public static final byte RES_CONF = 0x1A;
  public static final byte INT_SOURCE = 0x25;
  public static final byte FIFO_STATUS = 0x26;
  public static final byte STATUS = 0x27;
  public static final byte PRESS_OUT_XL = 0x28;
  public static final byte PRESS_OUT_L = 0x29;
  public static final byte PRESS_OUT_H = 0x2A;
  public static final byte TEMP_OUT_L = 0x2B;
  public static final byte TEMP_OUT_H = 0x2C;
  public static final byte LPFP_RES = 0x33;


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

  public int whoAmI(){
    return readRegister(WHO_AM_I ) & 0xff;
  }

  public void reset() {
    write(CTRL_REG1, (byte)(1 << 2));
    Delay.pause(50);
  }

  public float getTemperature() {
    byte[] temperatureBuffer = new byte[2];
    readRegister(TEMP_OUT_L, temperatureBuffer, 0, temperatureBuffer.length);
    int rawTemperature = ((temperatureBuffer[1] &0xff) << 8) | (temperatureBuffer[0] & 0xff);
    return rawTemperature/100.0f;
  }

  public float getPressure() {
    byte[] pressureBuffer = new byte[3];
    readRegister(PRESS_OUT_XL, pressureBuffer, 0, pressureBuffer.length);
    int rawPressure = (pressureBuffer[2] << 16 | ((pressureBuffer[1] &0xff) << 8) | (pressureBuffer[0] & 0xff));
    if((rawPressure & 0x800000) != 0){
      rawPressure = (0xff000000) | rawPressure; // It's now negative
    }
    return rawPressure/4096.0f;
  }

  public void setDataRate(DataRate rate) {
    int ctl1 = readRegister(CTRL_REG1) & 0xff;
    ctl1 = ctl1 & 0b0001111; // clear out the current rate;
    switch (rate){

      case RATE_1_HZ:
        ctl1 = ctl1 | 0b00010000;
        break;

      case RATE_10_HZ:
        ctl1 = ctl1 | 0b00100000;
        break;

      case RATE_25_HZ:
        ctl1 = ctl1 | 0b00110000;
        break;

      case RATE_50_HZ:
        ctl1 = ctl1 | 0b01000000;
        break;

      case RATE_75_HZ:
        ctl1 = ctl1 | 0b01010000;
        break;

      case RATE_ONE_SHOT:
      default:
        break;
    }
    write(CTRL_REG1, (byte) ctl1);
  }

  public void takeMeasurement() {
    // TODO: Implement the takeMeasurement function
  }

  public void zeroPressure() {
    // TODO: Implement the zeroPressure function
  }

  public void resetPressure() {
    // TODO: Implement the resetPressure function
  }

  public void setThresholdPressure(float threshold_pressure) {
    // TODO: Implement the setThresholdPressure function
  }

  public void enableHighThreshold() {
    // TODO: Implement the enableHighThreshold function
  }

  public void enableLowThreshold() {
    // TODO: Implement the enableLowThreshold function
  }

  public boolean highThresholdExceeded() {
    // TODO: Implement the highThresholdExceeded function
    return false; // Placeholder return value
  }

  public boolean lowThresholdExceeded() {
    // TODO: Implement the lowThresholdExceeded function
    return false; // Placeholder return value
  }

  public void enableInterrupts(boolean active_low, boolean open_drain) {
    // TODO: Implement the enableInterrupts function
  }

  public void disableInterrupts() {
    // TODO: Implement the disableInterrupts function
  }

  public void enableLowPass(boolean extra_low_bandwidth) {
    // TODO: Implement the enableLowPass function
  }


}
