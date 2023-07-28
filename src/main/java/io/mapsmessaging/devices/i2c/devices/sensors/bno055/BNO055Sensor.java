/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.sensors.bno055;

import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.registers.AxisRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.registers.CalibrationStatusRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.registers.ErrorStatusRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.registers.SystemStatusRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.CalibrationStatus;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.SystemErrorStatus;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.SystemStatus;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class BNO055Sensor extends I2CDevice implements Sensor {

  private final float[] myEuler = new float[3];
  private final CalibrationStatusRegister calibrationStatusRegister;
  private final SystemStatusRegister systemStatusRegister;
  private final ErrorStatusRegister errorStatusRegister;
  // Configuration Registers
  @Getter
  private final SingleByteRegister opMode;
  @Getter
  private final SingleByteRegister pwrMode;
  @Getter
  private final MultiByteRegister axisMapConfig;
  @Getter
  private final SingleByteRegister axisMapSign;
  @Getter
  private final AxisRegister accelDataX;
  @Getter
  private final AxisRegister accelDataY;
  @Getter
  private final AxisRegister accelDataZ;
  @Getter
  private final AxisRegister magDataX;
  @Getter
  private final AxisRegister magDataY;
  @Getter
  private final AxisRegister magDataZ;
  @Getter
  private final AxisRegister gyroDataX;
  @Getter
  private final AxisRegister gyroDataY;
  @Getter
  private final AxisRegister gyroDataZ;
  @Getter
  private final AxisRegister eulerH;
  @Getter
  private final AxisRegister eulerR;
  @Getter
  private final AxisRegister eulerP;
  @Getter
  private final AxisRegister quaternionW;
  @Getter
  private final AxisRegister quaternionX;
  @Getter
  private final AxisRegister quaternionY;
  @Getter
  private final AxisRegister quaternionZ;
  @Getter
  private final SingleByteRegister chipId;
  @Getter
  private final SingleByteRegister accelRevId;
  @Getter
  private final SingleByteRegister magRevId;
  @Getter
  private final SingleByteRegister gyroRevId;
  @Getter
  private final MultiByteRegister swRevId;
  @Getter
  private final SingleByteRegister blRevId;
  @Getter
  private final SingleByteRegister pageId;
  @Getter
  private final SingleByteRegister tempSource;
  @Getter
  private final SingleByteRegister unitSel;
  @Getter
  private final SingleByteRegister sysTrigger;
  @Getter
  private final SingleByteRegister sysClkStatus;

  private long lastRead;
  @Getter
  private String version;

  public BNO055Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(BNO055Sensor.class));
    calibrationStatusRegister = new CalibrationStatusRegister(this);
    systemStatusRegister = new SystemStatusRegister(this);
    errorStatusRegister = new ErrorStatusRegister(this);

    chipId = new SingleByteRegister(this, 0x00, "CHIP_ID");
    accelRevId = new SingleByteRegister(this, 0x01, "ACCEL_REV_ID");
    magRevId = new SingleByteRegister(this, 0x02, "MAG_REV_ID");
    gyroRevId = new SingleByteRegister(this, 0x03, "GYRO_REV_ID");
    swRevId = new MultiByteRegister(this, 0x04, 2, "SW_REV_ID");
    blRevId = new SingleByteRegister(this, 0x06, "BL_REV_ID");
    pageId = new SingleByteRegister(this, 0x07, "PAGE_ID");
    accelDataX = new AxisRegister(this, 0x08, "ACCEL_DATA_X");
    accelDataY = new AxisRegister(this, 0x0A, "ACCEL_DATA_Y");
    accelDataZ = new AxisRegister(this, 0x0C, "ACCEL_DATA_Z");
    magDataX = new AxisRegister(this, 0x0E, "MAG_DATA_X");
    magDataY = new AxisRegister(this, 0x10, "MAG_DATA_Y");
    magDataZ = new AxisRegister(this, 0x12, "MAG_DATA_Z");
    gyroDataX = new AxisRegister(this, 0x14, "GYRO_DATA_X");
    gyroDataY = new AxisRegister(this, 0x16, "GYRO_DATA_Y");
    gyroDataZ = new AxisRegister(this, 0x18, "GYRO_DATA_Z");
    eulerH = new AxisRegister(this, 0x1A, "EULER_H");
    eulerR = new AxisRegister(this, 0x1C, "EULER_R");
    eulerP = new AxisRegister(this, 0x1E, "EULER_P");
    quaternionW = new AxisRegister(this, 0x20, "QUATERNION_W");
    quaternionX = new AxisRegister(this, 0x22, "QUATERNION_X");
    quaternionY = new AxisRegister(this, 0x24, "QUATERNION_Y");
    quaternionZ = new AxisRegister(this, 0x26, "QUATERNION_Z");


    tempSource = new SingleByteRegister(this, 0x34, "TEMP_SOURCE");
    sysClkStatus = new SingleByteRegister(this, 0x38, "SYS_CLK_STATUS");
    unitSel = new SingleByteRegister(this, 0x3B, "UNIT_SEL");
    opMode = new SingleByteRegister(this, 0x3D, "OPERATION_MODE");
    pwrMode = new SingleByteRegister(this, 0x3E, "PWR_MODE");
    sysTrigger = new SingleByteRegister(this, 0x3F, "SYS_TRIGGER");

    axisMapConfig = new MultiByteRegister(this, 0x41, 2, "AXIS_MAP_CONFIG");
    axisMapSign = new SingleByteRegister(this, 0x42, "AXIS_MAP_SIGN");

    // Axis Registers


    // Status Registers
    initialise();
  }

  public static int getId(AddressableDevice device) {
    return device.readRegister(BNO055Constants.BNO055_CHIP_ID_ADDR);
  }

  public void initialise() throws IOException {
    write(BNO055Constants.BNO055_PAGE_ID_ADDR, (byte) 0);  // Send a NO OP to get the device ready to chat
    setConfigMode();
    write(BNO055Constants.BNO055_PAGE_ID_ADDR, (byte) 0);  // Send a NO OP to get the device ready to chat
    int bnoId = readRegister(BNO055Constants.BNO055_CHIP_ID_ADDR);
    if (bnoId != BNO055Constants.BNO055_ID) {
      throw new IOException("Detected alien device " + bnoId + " expected " + BNO055Constants.BNO055_ID);
    }

    write(BNO055Constants.BNO055_SYS_TRIGGER_ADDR, (byte) 0x20); // Command line reset instead
    delay(650);
    write(BNO055Constants.BNO055_PWR_MODE_ADDR, BNO055Constants.POWER_MODE_NORMAL);
    write(BNO055Constants.BNO055_SYS_TRIGGER_ADDR, (byte) 0x0);
    setOperationalMode();
    version = computeVersion();
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  public void initialize() throws IOException {
    int chipId = readRegister(BNO055Constants.BNO055_CHIP_ID_ADDR);
    if (chipId != BNO055Constants.BNO055_ID) {
      throw new IOException("BNO055 not detected!");
    }
    setOperationalMode();
    version = computeVersion();
  }

  public void setConfigMode() throws IOException {
    setMode(BNO055Constants.OPERATION_MODE_CONFIG);
  }

  public void setOperationalMode() throws IOException {
    setMode(BNO055Constants.OPERATION_MODE_NDOF);
  }

  private void setMode(byte mode) throws IOException {
    write(BNO055Constants.BNO055_OPR_MODE_ADDR, mode);
    delay(30);
  }


  public boolean isSystemCalibration() throws IOException {
    return calibrationStatusRegister.isCalibrated();
  }

  public CalibrationStatus getSystemCalibration() throws IOException {
    return calibrationStatusRegister.getSystem();
  }

  public CalibrationStatus getGryoscopeCalibration() throws IOException {
    return calibrationStatusRegister.getGryoscope();
  }

  public CalibrationStatus getAccelerometerCalibration() throws IOException {
    return calibrationStatusRegister.getAccelerometer();
  }

  public CalibrationStatus getMagnetometerCalibration() throws IOException {
    return calibrationStatusRegister.getMagnetometer();
  }

  public float[] readEuler() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      int[] res = readVector(BNO055Constants.BNO055_EULER_H_LSB_ADDR, 3);
      for (int x = 0; x < res.length; x++) {
        myEuler[x] = res[x] / 16.0f;
      }
      lastRead = System.currentTimeMillis() + 20;
    }
    return myEuler;
  }

  public float[] readMagnetometer() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_MAG_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 16.0f;
    }
    return ret;
  }

  public float[] readGyroscope() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_GYRO_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 900.0f;
    }
    return ret;
  }

  public float[] readAccelerometer() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_ACCEL_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 100.0f;
    }
    return ret;
  }

  public float[] readLinearAcceleration() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_LINEAR_ACCEL_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 100.0f;
    }
    return ret;
  }

  public float[] readGravity() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_GRAVITY_DATA_X_LSB_ADDR, 3);
    float[] ret = new float[res.length];
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] / 100.0f;
    }
    return ret;
  }

  public float[] readQuaternion() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_QUATERNION_DATA_W_LSB_ADDR, 4);
    float[] ret = new float[res.length];
    float scale = 1.0f / (1 << 14);
    for (int x = 0; x < res.length; x++) {
      ret[x] = res[x] * scale;
    }
    return ret;
  }


  private int[] readVector(byte address, int size) throws IOException {
    byte[] buffer = new byte[size * 2];
    readRegister(address, buffer, 0, buffer.length);
    int[] ret = new int[size];
    for (int x = 0; x < size; x++) {
      int low = (buffer[x * 2] & 0xff);
      ret[x] = low | buffer[x * 2 + 1] << 8;
    }
    return ret;
  }

  public SystemErrorStatus getErrorStatus() throws IOException {
    return errorStatusRegister.getErrorStatus();
  }

  public List<SystemStatus> getStatus(boolean selfTest) throws IOException {
    int results = 0x0f;
    if (selfTest) {
      setConfigMode();
      int sysTrigger = readRegister(BNO055Constants.BNO055_SYS_TRIGGER_ADDR);
      write(BNO055Constants.BNO055_SYS_TRIGGER_ADDR, (byte) (sysTrigger | 0x1));
      delay(1000);
      results = readRegister(BNO055Constants.BNO055_SELFTEST_RESULT_ADDR);
      setOperationalMode();
    }
    return systemStatusRegister.getStatus();
//    int status = readRegister(BNO055Constants.BNO055_SYS_STAT_ADDR);
//    int error = readRegister(BNO055Constants.BNO055_SYS_ERR_ADDR);
//    return new SystemStatusError(status, results, error);
  }

  public String computeVersion() throws IOException {
    int accel = readRegister(BNO055Constants.BNO055_ACCEL_REV_ID_ADDR);
    int mag = readRegister(BNO055Constants.BNO055_MAG_REV_ID_ADDR);
    int gyro = readRegister(BNO055Constants.BNO055_GYRO_REV_ID_ADDR);
    int bl = readRegister(BNO055Constants.BNO055_BL_REV_ID_ADDR);
    int swLsb = readRegister(BNO055Constants.BNO055_SW_REV_ID_LSB_ADDR);
    int swMsb = readRegister(BNO055Constants.BNO055_SW_REV_ID_MSB_ADDR);
    JSONObject versionObject = new JSONObject();
    versionObject.put("software", Float.parseFloat(swMsb + "." + swLsb));
    versionObject.put("bootLoader", Integer.toHexString(bl));
    versionObject.put("accelerometer", Integer.toHexString(accel));
    versionObject.put("magnetometer", Integer.toHexString(mag));
    versionObject.put("gyroscope", Integer.toHexString(gyro));
    return versionObject.toString(2);
  }

  public double[] getOrientation() throws IOException {
    byte[] buffer = new byte[6];
    readRegister(BNO055Constants.BNO055_EULER_H_LSB_ADDR, buffer, 0, 6);
    double heading = convert(buffer[0], buffer[1]) / 16.0;
    double roll = convert(buffer[2], buffer[3]) / 16.0;
    double pitch = convert(buffer[4], buffer[5]) / 16.0;
    return new double[]{heading, roll, pitch};
  }

  private int convert(byte lsb, byte msb) {
    return ((msb & 0xFF) << 8) | (lsb & 0xFF);
  }

  @Override
  public String getName() {
    return "BNO055";
  }

  @Override
  public String getDescription() {
    return "3 Axis orientation sensor";
  }
}