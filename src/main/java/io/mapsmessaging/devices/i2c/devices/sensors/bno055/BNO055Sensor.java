/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.bno055;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.data.Version;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.CalibrationStatus;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.SystemErrorStatus;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.Orientation;
import io.mapsmessaging.devices.sensorreadings.OrientationSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class BNO055Sensor extends I2CDevice implements Sensor {

  private final CalibrationStatusRegister calibrationStatusRegister;
  private final SystemStatusRegister systemStatusRegister;
  private final ErrorStatusRegister errorStatusRegister;
  private final SingleByteRegister opMode;
  private final SingleByteRegister pwrMode;
  private final MultiByteRegister axisMapConfig;
  private final SingleByteRegister axisMapSign;
  private final AxisRegister accelDataX;
  private final AxisRegister accelDataY;
  private final AxisRegister accelDataZ;
  private final AxisRegister magDataX;
  private final AxisRegister magDataY;
  private final AxisRegister magDataZ;
  private final AxisRegister gyroDataX;
  private final AxisRegister gyroDataY;
  private final AxisRegister gyroDataZ;
  private final AxisRegister eulerH;
  private final AxisRegister eulerR;
  private final AxisRegister eulerP;
  private final AxisRegister quaternionW;
  private final AxisRegister quaternionX;
  private final AxisRegister quaternionY;
  private final AxisRegister quaternionZ;
  private final SingleByteRegister chipId;
  private final SingleByteRegister accelRevId;
  private final SingleByteRegister magRevId;
  private final SingleByteRegister gyroRevId;
  private final MultiByteRegister swRevId;
  private final SingleByteRegister blRevId;
  private final SingleByteRegister pageId;
  private final SingleByteRegister tempSource;
  private final SingleByteRegister unitSel;
  private final SingleByteRegister sysTrigger;
  private final SingleByteRegister sysClkStatus;
  private final List<SensorReading<?>> readings;

  public BNO055Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(BNO055Sensor.class));

    calibrationStatusRegister = new CalibrationStatusRegister(this);
    systemStatusRegister = new SystemStatusRegister(this);
    errorStatusRegister = new ErrorStatusRegister(this);

    chipId = new ChipIdRegister(this);
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

    initialise();

    readings = generateSensorReadings( List.of(
        new OrientationSensorReading("heading", "°", "Heading from BNO055", new Orientation(0, 0, 0), true, this::getOrientation),
        new OrientationSensorReading("euler", "°", "Euler angles from BNO055", new Orientation(0, 0, 0), true, this::getEuler),
        new OrientationSensorReading("gravity", "m/s²", "Gravity vector from BNO055", new Orientation(0, 0, 9.8), true, this::getGravity),
        new OrientationSensorReading("gyroscope", "°/s", "Gyroscopic data from BNO055", new Orientation(0, 0, 0), true, this::getGyroscope),
        new OrientationSensorReading("linear_accel", "m/s²", "Linear acceleration from BNO055", new Orientation(0, 0, 0), true, this::getLinearAcceleration),
        new OrientationSensorReading("magnetometer", "µT", "Magnetic field from BNO055", new Orientation(0, 0, 0), true, this::getMagnetometer),
        new OrientationSensorReading("accelerometer", "m/s²", "Accelerometer data from BNO055", new Orientation(0, 0, 9.8), true, this::getAccelerometer)
    ));
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
  }

  @Override
  public boolean isConnected() {
    return false;
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

  public Orientation getEuler() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_EULER_H_LSB_ADDR, 3);
    return new Orientation((res[0] / 16.0f), (res[1] / 16.0f), (res[2] / 16.0f));
  }

  public Orientation getMagnetometer() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_MAG_DATA_X_LSB_ADDR, 3);
    return new Orientation((res[0] / 16.0f), (res[1] / 16.0f), (res[2] / 16.0f));
  }

  public Orientation getGyroscope() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_GYRO_DATA_X_LSB_ADDR, 3);
    return new Orientation((res[0] / 900.0f), (res[1] / 900.0f), (res[2] / 900.0f));
  }

  public Orientation getAccelerometer() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_ACCEL_DATA_X_LSB_ADDR, 3);
    return new Orientation((res[0] / 100.0f), (res[1] / 100.0f), (res[2] / 100.0f));
  }

  public Orientation getLinearAcceleration() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_LINEAR_ACCEL_DATA_X_LSB_ADDR, 3);
    return new Orientation((res[0] / 100.0f), (res[1] / 100.0f), (res[2] / 100.0f));
  }

  public Orientation getGravity() throws IOException {
    int[] res = readVector(BNO055Constants.BNO055_GRAVITY_DATA_X_LSB_ADDR, 3);
    return new Orientation((res[0] / 100.0f), (res[1] / 100.0f), (res[2] / 100.0f));
  }

  public float[] getQuaternion() throws IOException {
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

  public Version getVersion() throws IOException {
    int accel = readRegister(BNO055Constants.BNO055_ACCEL_REV_ID_ADDR);
    int mag = readRegister(BNO055Constants.BNO055_MAG_REV_ID_ADDR);
    int gyro = readRegister(BNO055Constants.BNO055_GYRO_REV_ID_ADDR);
    int bl = readRegister(BNO055Constants.BNO055_BL_REV_ID_ADDR);
    int swLsb = readRegister(BNO055Constants.BNO055_SW_REV_ID_LSB_ADDR);
    int swMsb = readRegister(BNO055Constants.BNO055_SW_REV_ID_MSB_ADDR);
    return new Version(Float.parseFloat(swMsb + "." + swLsb), bl, accel, mag, gyro);
  }

  public Orientation getOrientation() throws IOException {
    byte[] buffer = new byte[6];
    readRegister(BNO055Constants.BNO055_EULER_H_LSB_ADDR, buffer, 0, 6);
    double heading = convert(buffer[0], buffer[1]) / 16.0;
    double roll = convert(buffer[2], buffer[3]) / 16.0;
    double pitch = convert(buffer[4], buffer[5]) / 16.0;
    return new Orientation(heading, roll, pitch);
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

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}