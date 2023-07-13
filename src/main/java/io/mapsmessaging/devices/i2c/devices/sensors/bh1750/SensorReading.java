package io.mapsmessaging.devices.i2c.devices.sensors.bh1750;

import lombok.Getter;

public enum SensorReading {

    CONTINUOUS ( 0b00010000),
    ONE_TIME( 0b00100000);

    @Getter
    private final int mask;
    SensorReading(int mask){
        this.mask = mask;
    }
}
