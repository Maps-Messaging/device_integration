package io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers;

public enum Registers {
    CONFIGURATION(0x00),
    SHUNT_VOLTAGE(0x01),
    BUS_VOLTAGE(0x02),
    POWER(0x03),
    CURRENT(0x04),
    CALIBRATION(0x05);

    private final int address;

    Registers(int address) {
        this.address = address;
    }

    public int getAddress() {
        return address;
    }
}
