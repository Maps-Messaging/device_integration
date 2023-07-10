package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import lombok.Getter;

public enum DataRate {
    RATE_ONE_SHOT(0b000),
    RATE_1_HZ(0b001),
    RATE_10_HZ(0b010),
    RATE_25_HZ(0b011),
    RATE_50_HZ(0b100),
    RATE_75_HZ(0b101);

    @Getter
    private final int mask;

    DataRate(int mask) {
        this.mask = mask;
    }
}
