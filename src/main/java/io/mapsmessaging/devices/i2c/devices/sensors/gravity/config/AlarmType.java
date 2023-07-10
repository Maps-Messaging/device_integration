package io.mapsmessaging.devices.i2c.devices.sensors.gravity.config;

import lombok.Getter;

public enum AlarmType {
    LOW_THRESHOLD((byte) 0x00),
    HIGH_THRESHOLD((byte) 0x01);

    @Getter
    private final byte value;

    AlarmType(byte value) {
        this.value = value;
    }

}
