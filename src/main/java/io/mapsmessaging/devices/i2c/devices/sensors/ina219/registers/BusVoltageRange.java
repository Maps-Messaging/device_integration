package io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers;


public enum BusVoltageRange {
    RANGE_16V(0x0000),  // 0-16V Range
    RANGE_32V(0x2000);  // 0-32V Range

    private final int value;

    BusVoltageRange(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
