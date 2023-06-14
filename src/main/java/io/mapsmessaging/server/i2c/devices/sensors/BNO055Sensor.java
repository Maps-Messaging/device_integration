package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import io.mapsmessaging.server.i2c.I2CDevice;
import lombok.Getter;

import java.io.IOException;

import static io.mapsmessaging.server.i2c.devices.sensors.BNO055Constants.*;

public class BNO055Sensor extends I2CDevice {

  private final Logger logger = LoggerFactory.getLogger(BNO055Sensor.class);

  @Getter
  private String version;

  public BNO055Sensor(I2C device) throws IOException {
    super(device);
    initialize();
  }

  public void initialize() throws IOException {
    int chipId = readRegister(BNO055_CHIP_ID_ADDR);
    if (chipId != BNO055_ID) {
      throw new IOException("BNO055 not detected!");
    }
    write(OPERATION_MODE_CONFIG, OPERATION_MODE_NDOF);
    delay(25); // Wait for changes to take effect
      version = getVersion();
  }

  public String getVersion() {
    int accel = readRegister(BNO055_ACCEL_REV_ID_ADDR);
    int mag = readRegister(BNO055_MAG_REV_ID_ADDR);
    int gyro = readRegister(BNO055_GYRO_REV_ID_ADDR);
    int bl = readRegister(BNO055_BL_REV_ID_ADDR);
    int swLsb = readRegister(BNO055_SW_REV_ID_LSB_ADDR);
    int swMsb = readRegister(BNO055_SW_REV_ID_MSB_ADDR);
    int sw = ((swMsb << 8) | swLsb) & 0xFFFF;
    return "Software Version:" +
        sw +
        "\nBootLoader:" +
        bl +
        "\nAccelerometer ID:" +
        accel +
        "\nMagnetometer ID:" +
        mag +
        "\nGyroscope ID:" +
        gyro;
  }

  public double[] getOrientation() {
    byte[] buffer = new byte[6];
    readRegister(BNO055_EULER_H_LSB_ADDR, buffer, 0, 6);
    double heading = convert(buffer[0], buffer[1]) / 16.0;
    double roll = convert(buffer[2], buffer[3]) / 16.0;
    double pitch = convert(buffer[4], buffer[5]) / 16.0;
    return new double[]{heading, roll, pitch};
  }

  private int convert(byte lsb, byte msb) {
    return ((msb & 0xFF) << 8) | (lsb & 0xFF);
  }
}