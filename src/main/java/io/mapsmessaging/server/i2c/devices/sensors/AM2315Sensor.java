package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import io.mapsmessaging.server.i2c.I2CDevice;
import java.io.IOException;

public class AM2315Sensor extends I2CDevice {

  private final Logger logger = LoggerFactory.getLogger("AM2315");

  //
  // Command Codes
  //
  private final static byte READ_REGISTER =  0x03; // Read data from one or more registers
  private final static byte WRITE_REGISTER = 0x10; // Multiple sets of binary data is written to mutliple registers


  //
  // Registers
  //
  private final static byte HIGH_RH    = 0x00;
  private final static byte LOW_RH     = 0x01;
  private final static byte HIGH_TEMP  = 0x02;
  private final static byte LOW_TEMP   = 0x03;
  private final static byte Retention1 = 0x04;
  private final static byte Retention2 = 0x05;
  private final static byte Retention3 = 0x06;
  private final static byte Retention4 = 0x07;

  private final static byte MODEL_HIGH = 0x08;
  private final static byte MODEL_LOW  = 0x09;
  private final static byte VERSION    = 0x0A;
  private final static byte ID_24_31   = 0x0B;
  private final static byte ID_16_23   = 0x0C;
  private final static byte ID_8_15    = 0x0D;
  private final static byte ID_0_7     = 0x0E;
  private final static byte STATUS     = 0x0F;

  private final static byte USER_REGISTER1_HIGH = 0x10;
  private final static byte USER_REGISTER1_LOW  = 0x11;
  private final static byte USER_REGISTER2_HIGH = 0x12;
  private final static byte USER_REGISTER2_LOW  = 0x13;
  private final static byte Retention5     = 0x14;
  private final static byte Retention6     = 0x15;
  private final static byte Retention7     = 0x16;
  private final static byte Retention8     = 0x17;


  private final static byte Retention9     = 0x18;
  private final static byte RetentionA     = 0x19;
  private final static byte RetentionB     = 0x1A;
  private final static byte RetentionC     = 0x1B;
  private final static byte RetentionD     = 0x1C;
  private final static byte RetentionE     = 0x1D;
  private final static byte RetentionF     = 0x1E;
  private final static byte Retention10     = 0x1F;

  private byte[] sensor_readings = new byte[4];
  private long lastRead;


  public AM2315Sensor(I2C device) throws IOException {
    super(device);
    lastRead = 0;
    loadValues();
    //logger.debug("Created new AM2315 device");
  }

  private void loadValues()throws IOException {
    sensor_readings = retryReads(HIGH_RH, Retention1);
  }

  public float getTemperature(){
    int val = (sensor_readings[2]&0xff) << 8 | (sensor_readings[3] &0xff)+ 10;
    return val/10.0f;
  }

  public float getHumidity(){
    int val = (sensor_readings[0]&0xff) << 8 | (sensor_readings[1]&0xff);
    return val/10.0f;
  }

  public long getId(){
    try {
      byte[] ret = retryReads(ID_24_31, (byte)0x4);
      long res = 0;
      for(int x=0;x<4;x++){
        res = res<<8 |  (ret[x]&0xff);
      }
      return res;
    } catch (IOException e) {
      //logger.debug(e.getMessage());
    }
    return 0;
  }

  public int getModel(){
    try {
      byte[] ret = retryReads(MODEL_HIGH, (byte)0x2);
      return (ret[0]&0xff)<<8 | (ret[1]&0xff);
    } catch (IOException e) {
    //  logger.debug(e.getMessage());
    }
    return 0;
  }

  public int getVersion(){
    try {
      byte[] ret = retryReads(VERSION, (byte)0x1);
      return (ret[0]&0xff);
    } catch (IOException e) {
      //logger.debug(e.getMessage());
    }
    return 0;
  }

  public int getStatus(){
    try {
      byte[] ret = retryReads(STATUS, (byte)0x1);
      return (ret[0]&0xff);
    } catch (IOException e) {
      //logger.debug(e.getMessage());
    }
    return 0;
  }

  public void scanForChange() {
    try {
      if(lastRead +1000 < System.currentTimeMillis()) {
        lastRead = System.currentTimeMillis();
        byte[] val = retryReads(HIGH_RH, Retention1);
        boolean changed = false;
        for (int x = 0; x < val.length; x++) {
          if (val[x] != sensor_readings[x]) {
            changed = true;
            sensor_readings = val;
            break;
          }
        }
      }
    } catch (IOException e) {
      //logger.debug(e.getMessage());
    }
  }


  private byte[] retryReads(byte start, byte end) throws IOException {
    int count = 0;
    while (count < 100) {
      try {
        return readRegisters(start, end);
      } catch (Throwable th){
        count++;
        delay(10);
      }
    }
    return null;
  }

  private byte[] readRegisters(byte startReg, byte endReg) throws IOException {
    byte[] sendPacket = new byte[3];
    sendPacket[0] = READ_REGISTER;
    sendPacket[1] = startReg;
    sendPacket[2] = endReg;
    write(sendPacket);
    delay(10);
    byte[] header = new byte[32];
    int received = read(header);
    if(header[0] != 3){
      throw new IOException("Expected read");
    }
    int len = received-4;
    if(len > 0){
      byte[] res = new byte[len];
      System.arraycopy(header, 2, res, 0, res.length);
      return res;
    }
    return new byte[0];
  }
}