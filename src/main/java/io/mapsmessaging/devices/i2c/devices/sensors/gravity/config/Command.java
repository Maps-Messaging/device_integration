package io.mapsmessaging.devices.i2c.devices.sensors.gravity.config;

import lombok.Getter;

public enum Command {
    CHANGE_GET_METHOD(0X78, ""),
    GET_GAS_CONCENTRATION(0x86, "Get the current gas concentration"),
    GET_TEMP(0x87, "Get the current sensor temperature"),
    GET_ALL_DATA(0x88, "Get all the data in one request"),
    SET_THRESHOLD_ALARMS(0x89, "Set threshold alarms high and low"),
    I2C_DATA_AVAILABLE(0x90, "Is data available to read"),
    SENSOR_VOLTAGE(0x91, "Read the raw ADC voltage sensor"),
    CHANGE_I2C_ADDR(0x92, "Change the devices I2C address, reboot is required after setting");

    @Getter
    private final byte commandValue;

    @Getter
    private final String description;

    Command(int value, String description) {
        commandValue = (byte) value;
        this.description = description;
    }
}
