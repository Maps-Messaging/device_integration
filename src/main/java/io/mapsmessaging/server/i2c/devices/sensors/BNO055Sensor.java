package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import io.mapsmessaging.server.i2c.I2CDevice;

public class BNO055Sensor extends I2CDevice {

    private Logger logger = LoggerFactory.getLogger(BNO055Sensor.class);


    public BNO055Sensor(I2C device){
        super(device);
    }

}