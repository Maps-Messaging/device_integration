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
import io.mapsmessaging.devices.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Pmsa003iSensor extends I2CDevice implements Sensor {

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

  public int getPm1_0Standard() throws IOException {
    update();
    return registers.getPm1_0Standard();
  }

  public int getPm2_5Standard() throws IOException {
    update();
    return registers.getPm2_5Standard();
  }

  public int getPm10Standard() throws IOException {
    update();
    return registers.getPm10Standard();
  }

  public int getPm1_0Atmospheric() throws IOException {
    update();
    return registers.getPm1_0Atmospheric();
  }

  public int getPm2_5Atmospheric() throws IOException {
    update();
    return registers.getPm2_5Atmospheric();
  }

  public int getPm10Atmospheric() throws IOException {
    update();
    return registers.getPm10Atmospheric();
  }

  public int getParticlesLargerThan3() throws IOException {
    update();
    return registers.getParticlesLargerThan3();
  }

  public int getParticlesLargerThan5() throws IOException {
    update();
    return registers.getParticlesLargerThan5();
  }

  public int getParticlesLargerThan10() throws IOException {
    update();
    return registers.getParticlesLargerThan10();
  }

  public int getParticlesLargerThan25() throws IOException {
    update();
    return registers.getParticlesLargerThan25();
  }

  public int getParticlesLargerThan50() throws IOException {
    update();
    return registers.getParticlesLargerThan50();
  }

  public int getParticlesLargerThan100() throws IOException {
    update();
    return registers.getParticlesLargerThan100();
  }

  public int getVersion() throws IOException {
    update();
    return registers.getVersion();
  }

  public int getErrorCode() throws IOException {
    update();
    return registers.getErrorCode();
  }

  public void update() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      byte[] raw = registers.getByteBuffer().array();
      readRegister(0, raw, 0, raw.length);
      lastRead = System.currentTimeMillis() + 1000;
    }
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