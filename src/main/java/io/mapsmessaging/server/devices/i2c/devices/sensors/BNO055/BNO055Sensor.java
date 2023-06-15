package io.mapsmessaging.server.devices.i2c.devices.sensors.BNO055;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import io.mapsmessaging.server.devices.i2c.I2CDevice;
import lombok.Getter;

import java.io.IOException;

import static io.mapsmessaging.server.devices.i2c.devices.sensors.BNO055.BNO055Constants.*;

public class BNO055Sensor extends I2CDevice {

  private final Logger logger = LoggerFactory.getLogger(BNO055Sensor.class);
  private float[] myEuler = new float[3];
  private long lastRead;
  @Getter
  private String version;

  public BNO055Sensor(I2C device) throws IOException {
    super(device);
    initialise();
  }

  public void initialise() throws IOException {
    try {
      write(BNO055_PAGE_ID_ADDR, (byte) 0);  // Send a NO OP to get the device ready to chat
      setConfigMode();
      write(BNO055_PAGE_ID_ADDR, (byte) 0);  // Send a NO OP to get the device ready to chat
      int bno_id = readRegister(BNO055_CHIP_ID_ADDR);
      if (bno_id != BNO055_ID) {
        throw new IOException("Detected alien device " + bno_id + " expected " + BNO055_ID);
      }

      write(BNO055_SYS_TRIGGER_ADDR, (byte) 0x20); // Command line reset instead
      delay(650);
      write(BNO055_PWR_MODE_ADDR, POWER_MODE_NORMAL);
      write(BNO055_SYS_TRIGGER_ADDR, (byte) 0x0);
      setOperationalMode();
      version = computeVersion();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public void initialize() throws IOException {
    int chipId = readRegister(BNO055_CHIP_ID_ADDR);
    if (chipId != BNO055_ID) {
      throw new IOException("BNO055 not detected!");
    }
    setOperationalMode();

    version = computeVersion();
  }

  public void setConfigMode() {
    setMode(OPERATION_MODE_CONFIG);
  }

  public void setOperationalMode()  {
    setMode(OPERATION_MODE_NDOF);
  }

  private void setMode(byte mode)  {
    write(BNO055_OPR_MODE_ADDR, mode);
    delay(30);
  }

  public CalibrationStatus getCalibrationStatus()  {
    return new CalibrationStatus(readRegister(BNO055_CALIB_STAT_ADDR));
  }

  public float[] readEuler() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      int[] res = readVector(BNO055_EULER_H_LSB_ADDR, 3);
      for (int x = 0; x < res.length; x++) {
        myEuler[x] = res[x] / 16.0f;
      }
      lastRead = System.currentTimeMillis() + 20;
    }
    return myEuler;
  }

  public float[] readMagnetometer() throws IOException {
    int[] res = readVector(BNO055_MAG_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 16.0f;
    }
    return ret;
  }

  public float[] readGyroscope() throws IOException {
    int[] res = readVector(BNO055_GYRO_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 900.0f;
    }
    return ret;
  }

  public float[] readAccelerometer() throws IOException {
    int[] res = readVector(BNO055_ACCEL_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 100.0f;
    }
    return ret;
  }

  public float[] readLinearAcceleration() throws IOException {
    int[] res = readVector(BNO055_LINEAR_ACCEL_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 100.0f;
    }
    return ret;
  }

  public float[] readGravity() throws IOException {
    int[] res = readVector(BNO055_GRAVITY_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 100.0f;
    }
    return ret;
  }

  public float[] readQuaternion() throws IOException {
    int[] res = readVector(BNO055_QUATERNION_DATA_W_LSB_ADDR, 4);
    float[] ret = new float[res.length];
    float scale = 1.0f / (1 << 14);
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] * scale;
    }
    return ret;
  }


  private int[] readVector(byte address, int size) {
    byte[] buffer = new byte[size * 2];
    readRegister(address, buffer, 0, buffer.length);
    int[] ret = new int[size];
    for (int x = 0; x < size; x++) {
      int low = (buffer[x * 2] & 0xff);
      ret[x] = low | buffer[x * 2 + 1] << 8;
    }
    return ret;
  }

  public SystemStatus getStatus(boolean selfTest) {
    int results = 0x0f;
    if (selfTest) {
      setConfigMode();
      int sysTrigger = readRegister(BNO055_SYS_TRIGGER_ADDR);
      write(BNO055_SYS_TRIGGER_ADDR, (byte) (sysTrigger | 0x1));
      delay(1000);
      results = readRegister(BNO055_SELFTEST_RESULT_ADDR);
      setOperationalMode();
    }
    int status = readRegister(BNO055_SYS_STAT_ADDR);
    int error = readRegister(BNO055_SYS_ERR_ADDR);
    return new SystemStatus(status, results, error);
  }

  public String computeVersion() {
    int accel = readRegister(BNO055_ACCEL_REV_ID_ADDR);
    int mag = readRegister(BNO055_MAG_REV_ID_ADDR);
    int gyro = readRegister(BNO055_GYRO_REV_ID_ADDR);
    int bl = readRegister(BNO055_BL_REV_ID_ADDR);
    int swLsb = readRegister(BNO055_SW_REV_ID_LSB_ADDR);
    int swMsb = readRegister(BNO055_SW_REV_ID_MSB_ADDR);
    return "Software Version:" +
        swMsb + "."+swLsb +
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