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
import org.json.JSONObject;

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

  public JSONObject pack() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Pm1_0_standard", getPm1_0Standard());
    jsonObject.put("Pm2_5_standard", getPm2_5Standard());
    jsonObject.put("Pm10_standard", getPm10Standard());
    jsonObject.put("Pm1_0_atmospheric", getPm1_0Atmospheric());
    jsonObject.put("Pm2_5_atmospheric", getPm2_5Atmospheric());
    jsonObject.put("Pm10_atmospheric", getPm10Atmospheric());
    jsonObject.put("particles_larger_than_0.3", getParticlesLargerThan3());
    jsonObject.put("particles_larger_than_0.5", getParticlesLargerThan5());
    jsonObject.put("particles_larger_than_1.0", getParticlesLargerThan10());
    jsonObject.put("particles_larger_than_2.5", getParticlesLargerThan25());
    jsonObject.put("particles_larger_than_5.0", getParticlesLargerThan50());
    jsonObject.put("particles_larger_than_10.0", getParticlesLargerThan100());
    jsonObject.put("error_code", getErrorCode());
    return jsonObject;
  }
}
