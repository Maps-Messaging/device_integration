/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;

import java.io.IOException;

import static java.lang.Math.log;

public class CrcValidatingRegister extends Register {

  private final Command command;

  public CrcValidatingRegister(I2CDevice sensor, Command command) {
    super(sensor, command.getCommandValue(), command.name());
    this.command = command;
  }

  protected boolean sendBufferCommand(byte[] buffer) throws IOException {
    byte[] data = new byte[9];
    return (request(buffer, data) && data[2] == 1);
  }

  protected boolean simpleRequest(byte val) throws IOException {
    byte[] data = new byte[9];
    byte[] request = new byte[6];
    request[1] = val;
    return (request(request, data) && data[2] == 1);
  }

  @Override
  protected void reload() {
    // no Op
  }

  @Override
  protected void setControlRegister(int mask, int value) {
    // no Op
  }

  @Override
  public String toString(int maxLength) {
    return null;
  }

  protected boolean request(byte[] buf, byte[] result) throws IOException {
    buf[0] = command.getCommandValue();
    sensor.write(pack(buf));
    sensor.delay(100);
    sensor.readRegister(0, result, 0, result.length);
    byte checksum = calculateChecksum(result);
    return (result[8] == checksum);
  }

  protected byte[] pack(byte[] data) {
    byte[] payload = new byte[9];
    payload[0] = (byte) 0xff;
    payload[1] = 0x1;
    System.arraycopy(data, 0, payload, 2, data.length);
    payload[8] = calculateChecksum(payload);
    return payload;
  }

  protected byte calculateChecksum(byte[] data) {
    int checksum = 0;
    for (int i = 1; i < data.length - 2; i++) {
      int t = (data[i] & 0xff);
      checksum += t;
    }
    checksum = (~checksum) & 0xff;
    checksum = (checksum + 1);
    return (byte) checksum;
  }

  protected float computeTemperature(int rawTemperature) {
    float vpd3 = 3 * (float) rawTemperature / 1024.0f;
    float rth = vpd3 * 10000f / (3f - vpd3);
    return (float) (1 / (1 / (273.15f + 25) + 1 / 3380.13f * log(rth / 10000f)) - 273.15f);
  }

  protected float adjustPowers(int decimalPoint, float raw) {
    switch (decimalPoint) {
      case 1:
        raw = raw * 0.1f;
        break;
      case 2:
        raw = raw * 0.01f;
        break;

      default:
        break;
    }
    return raw;
  }
}
