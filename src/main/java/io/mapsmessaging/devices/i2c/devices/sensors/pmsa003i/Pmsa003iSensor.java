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

package io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.nio.ByteBuffer;

public class Pmsa003iSensor extends I2CDevice {

    @Getter
    private Registers registers;

    private final ByteBuffer buffer;
    private final byte[] raw;

    public Pmsa003iSensor(I2C device) {
        super(device, LoggerFactory.getLogger(Pmsa003iSensor.class));
        raw = new byte[0x1f];
        buffer = ByteBuffer.wrap(raw);
        registers = new Registers(buffer);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    protected void readDevice() {
        readRegister(0, raw, 0, raw.length);
    }

    @Override
    public String getName() {
        return "PMSA003I";
    }

    @Override
    public String getDescription() {
        return "Air Quality Breakout";
    }


}