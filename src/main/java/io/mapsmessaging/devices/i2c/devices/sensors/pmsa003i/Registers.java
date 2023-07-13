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

import lombok.Getter;

import java.nio.ByteBuffer;

public class Registers {

  @Getter
  private final ByteBuffer byteBuffer;

  public Registers(ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  public int getPm1_0Standard() {
    return byteBuffer.getShort(4);
  }

  public int getPm2_5Standard() {
    return byteBuffer.getShort(6);
  }

  public int getPm10Standard() {
    return byteBuffer.getShort(8);
  }

  public int getPm1_0Atmospheric() {
    return byteBuffer.getShort(0xa);
  }

  public int getPm2_5Atmospheric() {
    return byteBuffer.getShort(0xc);
  }

  public int getPm10Atmospheric() {
    return byteBuffer.getShort(0xe);
  }

  public int getParticlesLargerThan3() {
    return byteBuffer.getShort(0x10);
  }

  public int getParticlesLargerThan5() {
    return byteBuffer.getShort(0x12);
  }

  public int getParticlesLargerThan10() {
    return byteBuffer.getShort(0x14);
  }

  public int getParticlesLargerThan25() {
    return byteBuffer.getShort(0x16);
  }

  public int getParticlesLargerThan50() {
    return byteBuffer.getShort(0x18);
  }

  public int getParticlesLargerThan100() {
    return byteBuffer.getShort(0x1a);
  }

  public int getVersion() {
    return byteBuffer.get(0x1c);
  }

  public int getErrorCode() {
    return byteBuffer.get(0x1d);
  }
}
