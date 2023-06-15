package io.mapsmessaging.server.devices.i2c.devices.sensors.BNO055;

public class SystemStatus {

    private String[] StateString = {
            "Idle",
            "Error",
            "Initialise Peripherals",
            "System Initialisation",
            "Self Test",
            "Fusion Algorithm",
            "No Fusion",
            "Unknown"
    };

    private String[] ErrorString = {
            "No error",
            "Peripheral initialization error",
            "System initialization error",
            "Self test result failed",
            "Register map value out of range",
            "Register map address out of range",
            "Register map write error",
            "BNO low power mode not available for selected operation mode",
            "Accelerometer power mode not available",
            "Fusion algorithm configuration error",
            "Sensor configuration error"
    };

    private int system;
    private int selftest;
    private int error;
    protected SystemStatus(int system, int selfTest, int error) {
        this.system = system;
        selftest = selfTest;
        this.error = error;
    }

    public String getErrorString() {
        if(error >= ErrorString.length){
            return "Unknown error string:"+ error;
        }
        return ErrorString[error];
    }

    public Error getError() {
        switch (error) {
            case 0:
                return Error.NoError;
            case 1:
                return Error.Peripheral;
            case 2:
                return Error.SystemInit;
            case 3:
                return Error.SelfTest;
            case 4:
                return Error.ValueOutOfRange;
            case 5:
                return Error.AddressOutOfRange;
            case 6:
                return Error.MapWrite;
            case 7:
                return Error.LowPower;
            case 8:
                return Error.AccelPowerMode;
            case 9:
                return Error.FusionConfig;

            default:
                return Error.Unknown;
        }
    }

    public String getStateString() {
        if(error >= StateString.length){
            return "Unknown state::"+ error;
        }
        return StateString[error];
    }

    public State getState() {
        switch (system) {
            case 0:
                return State.Idle;
            case 1:
                return State.Error;
            case 2:
                return State.InitPeripherals;
            case 3:
                return State.SystemInit;
            case 4:
                return State.SelfTest;
            case 5:
                return State.Fusion;
            case 6:
                return State.NoFusion;
            default:
                return State.Unknown;
        }
    }

    public boolean selfTestAccelerometer() {
        return ((selftest & 0x1) != 0) || selftest == 0xf;
    }

    public boolean selfTestMagnetometer() {
        return ((selftest & 0x2) != 0) || selftest == 0xf;
    }

    public boolean selfTestGyroscope() {
        return ((selftest & 0x4) != 0) || selftest == 0xf;
    }

    public boolean selfTestMCU() {
        return ((selftest & 0x8) != 0) || selftest == 0xf;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("System Status:\n");
        sb.append("\tState:").append(getStateString()).append("\n");
        sb.append("\tError:").append(getErrorString()).append("\n");
        sb.append("\tAccel:").append(selfTestAccelerometer()).append(" Magnetometer:").append(selfTestMagnetometer()).append(" Gyroscope:").append(selfTestGyroscope()).append(" MCU:").append(selfTestMCU()).append("\n");
        return sb.toString();
    }


    public enum State {
        Idle, Error, InitPeripherals, SystemInit, SelfTest, Fusion, NoFusion, Unknown
    }

    public enum Error {
        NoError, Peripheral, SystemInit, SelfTest, ValueOutOfRange, AddressOutOfRange,
        MapWrite, LowPower, AccelPowerMode, FusionConfig, Sensor, Unknown
    }

}