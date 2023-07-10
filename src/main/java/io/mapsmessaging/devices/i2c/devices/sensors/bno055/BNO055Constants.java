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

public class BNO055Constants {
    // I2C addresses
    public static final byte BNO055_ADDRESS_A = 0x28;
    public static final byte BNO055_ADDRESS_B = 0x29;
    public static final int BNO055_ID = 0xA0;

    //Page id register definition
    public static final byte BNO055_PAGE_ID_ADDR = 0X07;

    //PAGE0 REGISTER DEFINITION START
    public static final byte BNO055_CHIP_ID_ADDR = 0x00;
    public static final byte BNO055_ACCEL_REV_ID_ADDR = 0x01;
    public static final byte BNO055_MAG_REV_ID_ADDR = 0x02;
    public static final byte BNO055_GYRO_REV_ID_ADDR = 0x03;
    public static final byte BNO055_SW_REV_ID_LSB_ADDR = 0x04;
    public static final byte BNO055_SW_REV_ID_MSB_ADDR = 0x05;
    public static final byte BNO055_BL_REV_ID_ADDR = 0X06;

    //    # Accel data register
    public static final byte BNO055_ACCEL_DATA_X_LSB_ADDR = 0X08;
    public static final byte BNO055_ACCEL_DATA_X_MSB_ADDR = 0X09;
    public static final byte BNO055_ACCEL_DATA_Y_LSB_ADDR = 0X0A;
    public static final byte BNO055_ACCEL_DATA_Y_MSB_ADDR = 0X0B;
    public static final byte BNO055_ACCEL_DATA_Z_LSB_ADDR = 0X0C;
    public static final byte BNO055_ACCEL_DATA_Z_MSB_ADDR = 0X0D;

    //    # Mag data register
    public static final byte BNO055_MAG_DATA_X_LSB_ADDR = 0X0E;
    public static final byte BNO055_MAG_DATA_X_MSB_ADDR = 0X0F;
    public static final byte BNO055_MAG_DATA_Y_LSB_ADDR = 0X10;
    public static final byte BNO055_MAG_DATA_Y_MSB_ADDR = 0X11;
    public static final byte BNO055_MAG_DATA_Z_LSB_ADDR = 0X12;
    public static final byte BNO055_MAG_DATA_Z_MSB_ADDR = 0X13;

    //    # Gyro data registers
    public static final byte BNO055_GYRO_DATA_X_LSB_ADDR = 0X14;
    public static final byte BNO055_GYRO_DATA_X_MSB_ADDR = 0X15;
    public static final byte BNO055_GYRO_DATA_Y_LSB_ADDR = 0X16;
    public static final byte BNO055_GYRO_DATA_Y_MSB_ADDR = 0X17;
    public static final byte BNO055_GYRO_DATA_Z_LSB_ADDR = 0X18;
    public static final byte BNO055_GYRO_DATA_Z_MSB_ADDR = 0X19;

    //    # Euler data registers
    public static final byte BNO055_EULER_H_LSB_ADDR = 0X1A;
    public static final byte BNO055_EULER_H_MSB_ADDR = 0X1B;
    public static final byte BNO055_EULER_R_LSB_ADDR = 0X1C;
    public static final byte NO055_EULER_R_MSB_ADDR = 0X1D;
    public static final byte BNO055_EULER_P_LSB_ADDR = 0X1E;
    public static final byte BNO055_EULER_P_MSB_ADDR = 0X1F;

    //    # Quaternion data registers
    public static final byte BNO055_QUATERNION_DATA_W_LSB_ADDR = 0X20;
    public static final byte BNO055_QUATERNION_DATA_W_MSB_ADDR = 0X21;
    public static final byte BNO055_QUATERNION_DATA_X_LSB_ADDR = 0X22;
    public static final byte BNO055_QUATERNION_DATA_X_MSB_ADDR = 0X23;
    public static final byte BNO055_QUATERNION_DATA_Y_LSB_ADDR = 0X24;
    public static final byte BNO055_QUATERNION_DATA_Y_MSB_ADDR = 0X25;
    public static final byte BNO055_QUATERNION_DATA_Z_LSB_ADDR = 0X26;
    public static final byte BNO055_QUATERNION_DATA_Z_MSB_ADDR = 0X27;

    //    # Linear acceleration data registers
    public static final byte BNO055_LINEAR_ACCEL_DATA_X_LSB_ADDR = 0X28;
    public static final byte BNO055_LINEAR_ACCEL_DATA_X_MSB_ADDR = 0X29;
    public static final byte BNO055_LINEAR_ACCEL_DATA_Y_LSB_ADDR = 0X2A;
    public static final byte BNO055_LINEAR_ACCEL_DATA_Y_MSB_ADDR = 0X2B;
    public static final byte BNO055_LINEAR_ACCEL_DATA_Z_LSB_ADDR = 0X2C;
    public static final byte BNO055_LINEAR_ACCEL_DATA_Z_MSB_ADDR = 0X2D;

    //    # Gravity data registers
    public static final byte BNO055_GRAVITY_DATA_X_LSB_ADDR = 0X2E;
    public static final byte BNO055_GRAVITY_DATA_X_MSB_ADDR = 0X2F;
    public static final byte BNO055_GRAVITY_DATA_Y_LSB_ADDR = 0X30;
    public static final byte BNO055_GRAVITY_DATA_Y_MSB_ADDR = 0X31;
    public static final byte BNO055_GRAVITY_DATA_Z_LSB_ADDR = 0X32;
    public static final byte BNO055_GRAVITY_DATA_Z_MSB_ADDR = 0X33;

    //    # Temperature data register
    public static final byte BNO055_TEMP_ADDR = 0X34;

    //# Status registers
    public static final byte BNO055_CALIB_STAT_ADDR = 0X35;
    public static final byte BNO055_SELFTEST_RESULT_ADDR = 0X36;
    public static final byte BNO055_INTR_STAT_ADDR = 0X37;

    public static final byte BNO055_SYS_CLK_STAT_ADDR = 0X38;
    public static final byte BNO055_SYS_STAT_ADDR = 0X39;
    public static final byte BNO055_SYS_ERR_ADDR = 0X3A;

    //    # Unit selection register
    public static final byte BNO055_UNIT_SEL_ADDR = 0X3B;
    public static final byte BNO055_DATA_SELECT_ADDR = 0X3C;

    //    # Mode registers
    public static final byte BNO055_OPR_MODE_ADDR = 0X3D;
    public static final byte BNO055_PWR_MODE_ADDR = 0X3E;

    public static final byte BNO055_SYS_TRIGGER_ADDR = 0X3F;
    public static final byte BNO055_TEMP_SOURCE_ADDR = 0X40;

    //    # Axis remap registers
    public static final byte BNO055_AXIS_MAP_CONFIG_ADDR = 0X41;
    public static final byte BNO055_AXIS_MAP_SIGN_ADDR = 0X42;

    //    # Axis remap values
    public static final byte AXIS_REMAP_X = 0x00;
    public static final byte AXIS_REMAP_Y = 0x01;
    public static final byte AXIS_REMAP_Z = 0x02;
    public static final byte AXIS_REMAP_POSITIVE = 0x00;
    public static final byte AXIS_REMAP_NEGATIVE = 0x01;

    //    # SIC registers
    public static final byte BNO055_SIC_MATRIX_0_LSB_ADDR = 0X43;
    public static final byte BNO055_SIC_MATRIX_0_MSB_ADDR = 0X44;
    public static final byte BNO055_SIC_MATRIX_1_LSB_ADDR = 0X45;
    public static final byte BNO055_SIC_MATRIX_1_MSB_ADDR = 0X46;
    public static final byte BNO055_SIC_MATRIX_2_LSB_ADDR = 0X47;
    public static final byte BNO055_SIC_MATRIX_2_MSB_ADDR = 0X48;
    public static final byte BNO055_SIC_MATRIX_3_LSB_ADDR = 0X49;
    public static final byte BNO055_SIC_MATRIX_3_MSB_ADDR = 0X4A;
    public static final byte BNO055_SIC_MATRIX_4_LSB_ADDR = 0X4B;
    public static final byte BNO055_SIC_MATRIX_4_MSB_ADDR = 0X4C;
    public static final byte BNO055_SIC_MATRIX_5_LSB_ADDR = 0X4D;
    public static final byte BNO055_SIC_MATRIX_5_MSB_ADDR = 0X4E;
    public static final byte BNO055_SIC_MATRIX_6_LSB_ADDR = 0X4F;
    public static final byte BNO055_SIC_MATRIX_6_MSB_ADDR = 0X50;
    public static final byte BNO055_SIC_MATRIX_7_LSB_ADDR = 0X51;
    public static final byte BNO055_SIC_MATRIX_7_MSB_ADDR = 0X52;
    public static final byte BNO055_SIC_MATRIX_8_LSB_ADDR = 0X53;
    public static final byte BNO055_SIC_MATRIX_8_MSB_ADDR = 0X54;

    //    # Accelerometer Offset registers
    public static final byte ACCEL_OFFSET_X_LSB_ADDR = 0X55;
    public static final byte ACCEL_OFFSET_X_MSB_ADDR = 0X56;
    public static final byte ACCEL_OFFSET_Y_LSB_ADDR = 0X57;
    public static final byte ACCEL_OFFSET_Y_MSB_ADDR = 0X58;
    public static final byte ACCEL_OFFSET_Z_LSB_ADDR = 0X59;
    public static final byte ACCEL_OFFSET_Z_MSB_ADDR = 0X5A;

    //    # Magnetometer Offset registers
    public static final byte MAG_OFFSET_X_LSB_ADDR = 0X5B;
    public static final byte MAG_OFFSET_X_MSB_ADDR = 0X5C;
    public static final byte MAG_OFFSET_Y_LSB_ADDR = 0X5D;
    public static final byte MAG_OFFSET_Y_MSB_ADDR = 0X5E;
    public static final byte MAG_OFFSET_Z_LSB_ADDR = 0X5F;
    public static final byte MAG_OFFSET_Z_MSB_ADDR = 0X60;

    //    # Gyroscope Offset register s
    public static final byte GYRO_OFFSET_X_LSB_ADDR = 0X61;
    public static final byte GYRO_OFFSET_X_MSB_ADDR = 0X62;
    public static final byte GYRO_OFFSET_Y_LSB_ADDR = 0X63;
    public static final byte GYRO_OFFSET_Y_MSB_ADDR = 0X64;
    public static final byte GYRO_OFFSET_Z_LSB_ADDR = 0X65;
    public static final byte GYRO_OFFSET_Z_MSB_ADDR = 0X66;

    //    # Radius registers
    public static final byte ACCEL_RADIUS_LSB_ADDR = 0X67;
    public static final byte ACCEL_RADIUS_MSB_ADDR = 0X68;
    public static final byte MAG_RADIUS_LSB_ADDR = 0X69;
    public static final byte MAG_RADIUS_MSB_ADDR = 0X6A;

    //    # Power modes
    public static final byte POWER_MODE_NORMAL = 0X00;
    public static final byte POWER_MODE_LOWPOWER = 0X01;
    public static final byte POWER_MODE_SUSPEND = 0X02;

    //    # Operation mode settings
    public static final byte OPERATION_MODE_CONFIG = 0X00;
    public static final byte OPERATION_MODE_ACCONLY = 0X01;
    public static final byte OPERATION_MODE_MAGONLY = 0X02;
    public static final byte OPERATION_MODE_GYRONLY = 0X03;
    public static final byte OPERATION_MODE_ACCMAG = 0X04;
    public static final byte OPERATION_MODE_ACCGYRO = 0X05;
    public static final byte OPERATION_MODE_MAGGYRO = 0X06;
    public static final byte OPERATION_MODE_AMG = 0X07;
    public static final byte OPERATION_MODE_IMUPLUS = 0X08;
    public static final byte OPERATION_MODE_COMPASS = 0X09;
    public static final byte OPERATION_MODE_M4G = 0X0A;
    public static final byte OPERATION_MODE_NDOF_FMC_OFF = 0X0B;
    public static final byte OPERATION_MODE_NDOF = 0X0C;
}
