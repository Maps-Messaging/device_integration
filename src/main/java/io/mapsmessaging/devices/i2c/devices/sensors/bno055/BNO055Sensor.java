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

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class BNO055Sensor extends I2CDevice {

    private final float[] myEuler = new float[3];
    private long lastRead;
    @Getter
    private String version;

    public BNO055Sensor(I2C device) throws IOException {
        super(device, LoggerFactory.getLogger(BNO055Sensor.class));
        initialise();
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

    public void setConfigMode() {
        setMode(BNO055Constants.OPERATION_MODE_CONFIG);
    }

    public void setOperationalMode() {
        setMode(BNO055Constants.OPERATION_MODE_NDOF);
    }

    private void setMode(byte mode) {
        write(BNO055Constants.BNO055_OPR_MODE_ADDR, mode);
        delay(30);
    }

    public CalibrationStatus getCalibrationStatus() {
        return new CalibrationStatus(readRegister(BNO055Constants.BNO055_CALIB_STAT_ADDR));
    }

    public float[] readEuler() {
        if (lastRead < System.currentTimeMillis()) {
            int[] res = readVector(BNO055Constants.BNO055_EULER_H_LSB_ADDR, 3);
            for (int x = 0; x < res.length; x++) {
                myEuler[x] = res[x] / 16.0f;
            }
            lastRead = System.currentTimeMillis() + 20;
        }
        return myEuler;
    }

    public float[] readMagnetometer() {
        int[] res = readVector(BNO055Constants.BNO055_MAG_DATA_X_LSB_ADDR, 3);
        float[] ret = new float[res.length];
        for (int x = 0; x < res.length; x++) {
            ret[x] = res[x] / 16.0f;
        }
        return ret;
    }

    public float[] readGyroscope() {
        int[] res = readVector(BNO055Constants.BNO055_GYRO_DATA_X_LSB_ADDR, 3);
        float[] ret = new float[res.length];
        for (int x = 0; x < res.length; x++) {
            ret[x] = res[x] / 900.0f;
        }
        return ret;
    }

    public float[] readAccelerometer() {
        int[] res = readVector(BNO055Constants.BNO055_ACCEL_DATA_X_LSB_ADDR, 3);
        float[] ret = new float[res.length];
        for (int x = 0; x < res.length; x++) {
            ret[x] = res[x] / 100.0f;
        }
        return ret;
    }

    public float[] readLinearAcceleration() {
        int[] res = readVector(BNO055Constants.BNO055_LINEAR_ACCEL_DATA_X_LSB_ADDR, 3);
        float[] ret = new float[res.length];
        for (int x = 0; x < res.length; x++) {
            ret[x] = res[x] / 100.0f;
        }
        return ret;
    }

    public float[] readGravity() {
        int[] res = readVector(BNO055Constants.BNO055_GRAVITY_DATA_X_LSB_ADDR, 3);
        float[] ret = new float[res.length];
        for (int x = 0; x < res.length; x++) {
            ret[x] = res[x] / 100.0f;
        }
        return ret;
    }

    public float[] readQuaternion() {
        int[] res = readVector(BNO055Constants.BNO055_QUATERNION_DATA_W_LSB_ADDR, 4);
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
            int sysTrigger = readRegister(BNO055Constants.BNO055_SYS_TRIGGER_ADDR);
            write(BNO055Constants.BNO055_SYS_TRIGGER_ADDR, (byte) (sysTrigger | 0x1));
            delay(1000);
            results = readRegister(BNO055Constants.BNO055_SELFTEST_RESULT_ADDR);
            setOperationalMode();
        }
        int status = readRegister(BNO055Constants.BNO055_SYS_STAT_ADDR);
        int error = readRegister(BNO055Constants.BNO055_SYS_ERR_ADDR);
        return new SystemStatus(status, results, error);
    }

    public String computeVersion() {
        int accel = readRegister(BNO055Constants.BNO055_ACCEL_REV_ID_ADDR);
        int mag = readRegister(BNO055Constants.BNO055_MAG_REV_ID_ADDR);
        int gyro = readRegister(BNO055Constants.BNO055_GYRO_REV_ID_ADDR);
        int bl = readRegister(BNO055Constants.BNO055_BL_REV_ID_ADDR);
        int swLsb = readRegister(BNO055Constants.BNO055_SW_REV_ID_LSB_ADDR);
        int swMsb = readRegister(BNO055Constants.BNO055_SW_REV_ID_MSB_ADDR);
        JSONObject versionObject = new JSONObject();
        versionObject.put("software", Float.parseFloat(swMsb + "." + swLsb));
        versionObject.put("bootLoader", bl);
        versionObject.put("accelerometer", accel);
        versionObject.put("magnetometer", mag);
        versionObject.put("gyroscope", gyro);
        return versionObject.toString(2);
    }

    public double[] getOrientation() {
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