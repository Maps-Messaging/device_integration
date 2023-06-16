package io.mapsmessaging.devices.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import lombok.Getter;

import java.io.IOException;

public class AM2320Sensor extends I2CDevice {

  private static int  AM2320_SENSOR_VERSION = 1; ///< the sensor version
      private static int  AM2320_CMD_READREG = 0x03; ///< read register command
      private static int  AM2320_REG_TEMP_H = 0x02;  ///< temp register address
      private static int  AM2320_REG_HUM_H = 0x00;   ///< humidity register address
  
  @Getter
  private float temperature;
  @Getter
  private float humidity;

  public AM2320Sensor(I2C device) throws IOException {
    super(device);
    scanForChange();
  }
  @Override
  public boolean isConnected() {
    return false;
  }
  public void scanForChange() {
    byte[] response = new byte[8];
    try {
      write(0);
    } catch (Exception e) {
      // Wake it up.. it sleeps all the time
    }
    delay(10);
    response[0] = (byte) AM2320_CMD_READREG;
    response[1] = (byte)AM2320_REG_HUM_H;
    response[2] = (byte)4;
    write(response, 0, 3);
    delay(2);
    read(response);
    int crc = (((response[7] & 0xff) <<8) | (response[6] & 0xff));
    int computedCrc = crc16(response, 6);

    if(response[0] != 0x3 && response[1] != 4 && crc != computedCrc){
      humidity = -273;
      temperature = -273;
      return;
    }
    humidity = (((response[2] & 0xff) <<8) | (response[3] & 0xff)) / 10.0f;
    temperature = (((response[4] & 0xff) <<8) | (response[5] & 0xff)) / 10.0f;
  }

  public int crc16(byte[] data, int len) {
    int crc = 0xFFFF;
    for (int j = 0; j < len; j++) {
      crc ^= (data[j] & 0xFF); // Make sure to do bitwise AND with 0xFF, because Java doesn't have unsigned bytes
      for (int i = 0; i < 8; i++) {
        if ((crc & 0x01) != 0) {
          crc >>= 1;
          crc ^= 0xA001;
        } else {
          crc >>= 1;
        }
      }
    }
    return crc & 0xFFFF; // Return only the lower 16 bits
  }

}