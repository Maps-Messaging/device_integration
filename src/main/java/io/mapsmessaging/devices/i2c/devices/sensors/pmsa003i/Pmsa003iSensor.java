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

import java.io.IOException;
import java.nio.ByteBuffer;

public class Pmsa003iSensor extends I2CDevice {

  private final Registers registers;
  private long lastRead;


  public Pmsa003iSensor(I2C device) {
    super(device, LoggerFactory.getLogger(Pmsa003iSensor.class));
    byte[] raw = new byte[0x1f];
    ByteBuffer buffer = ByteBuffer.wrap(raw);
    registers = new Registers(buffer);
    lastRead = 0;
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public Registers getRegisters() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      readDevice();
      lastRead = System.currentTimeMillis() + 1000;
    }
    return registers;
  }

  protected void readDevice() throws IOException {
    byte[] raw = registers.getByteBuffer().array();
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