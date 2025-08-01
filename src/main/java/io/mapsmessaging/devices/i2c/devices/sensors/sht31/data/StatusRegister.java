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

package io.mapsmessaging.devices.i2c.devices.sensors.sht31.data;

public class StatusRegister {
  private final int raw;

  public StatusRegister(byte msb, byte lsb) {
    this.raw = ((msb & 0xFF) << 8) | (lsb & 0xFF);
  }

  public boolean isAlertPending() {
    return (raw & (1 << 15)) != 0;
  }

  public boolean isHeaterOn() {
    return (raw & (1 << 13)) != 0;
  }

  public boolean isRhAlert() {
    return (raw & (1 << 11)) != 0;
  }

  public boolean isTempAlert() {
    return (raw & (1 << 10)) != 0;
  }

  public boolean isSystemResetDetected() {
    return (raw & (1 << 4)) != 0;
  }

  public boolean isLastCmdFailed() {
    return (raw & (1 << 1)) != 0;
  }

  public boolean isChecksumError() {
    return (raw & 1) != 0;
  }

  public int getRawValue() {
    return raw;
  }

  @Override
  public String toString() {
    return String.format("StatusRegister[raw=0x%04X, alertPending=%b, heater=%b, RHAlert=%b, TAlert=%b, reset=%b, cmdError=%b, crcError=%b]",
        raw, isAlertPending(), isHeaterOn(), isRhAlert(), isTempAlert(), isSystemResetDetected(), isLastCmdFailed(), isChecksumError());
  }
}
