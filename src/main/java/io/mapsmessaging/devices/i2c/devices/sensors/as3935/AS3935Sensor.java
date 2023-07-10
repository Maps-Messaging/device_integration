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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class AS3935Sensor extends I2CDevice {

  public static final byte IDLE = 0x0;
  public static final byte INTERRUPT_TOO_HIGH = 0x1;
  public static final byte INTERRUPT_DISTURBER = 0x4;
  public static final byte INTERRUPT_LIGHTNING = 0x8;

  private final Registers register;
  private final byte[] registers;
  private final int tuning;

  public AS3935Sensor(I2C device, int tuning) throws IOException {
    super(device, LoggerFactory.getLogger(AS3935Sensor.class));
    registers = new byte[128];

    register = new Registers(this);
    this.tuning = tuning;
    setup();
    /*
    if (pinNumber > -1) {
      InterruptFactory interruptFactory = DeviceBusManager.getInstance().getInterruptFactory();
      interruptFactory.create(
          "AS3935InterruptPin",
          "AS3935InterruptPin",
          pinNumber,
          InterruptManager.PULL.DOWN,
          this);
    }
     */
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public Registers getRegisters() {
    return register;
  }

  private void readData() {
    read(registers, 0, 128);
  }

  public void setup() throws IOException {
    delay(80);
    readData();
    if (tuning != 0) {
      if (tuning < 0x10 && tuning > -1) {
        write(0x08, (byte) ((registers[0x08] & 0xF0) | tuning));
        registers[8] = (byte) ((registers[0x08] & 0xF0) | tuning);
      }
      delay(200);
      readData();
    } else {
      throw new IOException("Value of TUN_CAP must be between 0 and 15");
    }
    write(0x08, (byte) (registers[0x08] | 0x20));
    delay(200);
    readData();
    write(0x08, (byte) (registers[0x08] & 0xDF));
    delay(200);
    readData();
  }


  @Override
  public String getName() {
    return "AS3935";
  }

  @Override
  public String getDescription() {
    return "Lightning detector and warning sensor";
  }
}