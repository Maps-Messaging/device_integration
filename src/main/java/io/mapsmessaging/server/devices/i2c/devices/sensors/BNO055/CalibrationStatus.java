package io.mapsmessaging.server.devices.i2c.devices.sensors.BNO055;


public class CalibrationStatus {

    private int myStatus;

    protected CalibrationStatus(int status) {
        myStatus = status;
    }

    public int getSystem() {
        return (myStatus >> 6) & 0x3;
    }

    public int getGryoscope() {
        return (myStatus >> 4) & 0x3;
    }

    public int getAccelerometer() {
        return (myStatus >> 2) & 0x3;
    }

    public int getMagnetometer() {
        return myStatus & 0x3;
    }

    public boolean isCalibrated() {
        return getGryoscope() == 3 && getAccelerometer() == 3 && getMagnetometer() == 3 && getSystem() == 3;
    }

    public String toString() {
        return "Gryoscope:" + getGryoscope() + " Accelerometer:" + getAccelerometer() + " Magnetometer:" + getMagnetometer() + " System:" + getSystem();
    }
}