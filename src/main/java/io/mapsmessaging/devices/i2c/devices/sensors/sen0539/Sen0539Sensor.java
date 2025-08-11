/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.sen0539;


import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.BufferedRegister;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.BooleanSensorReading;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

/**
 * DFRobot SEN0539 (DF2301Q) – Offline Voice Recognition Module over I2C.
 * Registers:
 * 0x02: CMD_ID (R)
 * 0x03: PLAY_BY_CMDID (W)
 * 0x04: MUTE (R/W)      0/1
 * 0x05: VOLUME (R/W)    0..7
 * 0x06: WAKE_TIME (R/W) seconds 0..255
 */
public class Sen0539Sensor extends I2CDevice implements Sensor {

  private static final int REG_BASE = 0x02;  // first readable register
  private static final int FRAME_LEN = 5;     // 0x02..0x06 inclusive

  // Offsets in local buffer (relative to REG_BASE)
  private static final int OFF_CMD_ID = 0;   // 0x02
  private static final int OFF_PLAY_CMD = 1;   // 0x03 (write only)
  private static final int OFF_MUTE = 2;   // 0x04
  private static final int OFF_VOL = 3;   // 0x05
  private static final int OFF_WAKE = 4;   // 0x06

  private final byte[] data;
  private final BufferedRegister cmdIdReg;
  private final BufferedRegister muteReg;
  private final BufferedRegister volumeReg;
  private final BufferedRegister wakeTimeReg;

  @Getter
  private final List<SensorReading<?>> readings;

  private long nextRefreshMs;

  public Sen0539Sensor(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Sen0539Sensor.class));
    this.data = new byte[FRAME_LEN];

    this.cmdIdReg = new BufferedRegister(this, OFF_CMD_ID, 1, "CmdId", data);
    this.muteReg = new BufferedRegister(this, OFF_MUTE, 1, "Mute", data);
    this.volumeReg = new BufferedRegister(this, OFF_VOL, 1, "Volume", data);
    this.wakeTimeReg = new BufferedRegister(this, OFF_WAKE, 1, "WakeTime", data);

    this.nextRefreshMs = 0;

    this.readings = List.of(
        new IntegerSensorReading("cmd_id", "", "Last recognized command ID", 1, true, 0, 255, this::getCommandId),
        new StringSensorReading("cmd_name", "", "Last recognized command text", "Hello Robot", true, this::getCommandName),
        new BooleanSensorReading("mute", "", "Mute state", false, true, this::isMuted),
        new IntegerSensorReading("volume", "", "Speaker volume (0..7)", 5, true, 0, 7, this::getVolume),
        new IntegerSensorReading("wake_time", "s", "Wake window after recognition", 30, true, 0, 255, this::getWakeTimeSeconds)
    );
  }

  private void refresh() throws IOException {
    long now = System.currentTimeMillis();
    if (now >= nextRefreshMs) {
      // Burst read the contiguous register window [0x02..0x06]
      readRegister(REG_BASE, data, 0, FRAME_LEN);
      nextRefreshMs = now + 250; // ~4 Hz
    }
  }

  public int getCommandId() throws IOException {
    refresh();
    return cmdIdReg.getValue() & 0xFF;
  }

  public boolean isMuted() throws IOException {
    refresh();
    return (muteReg.getValue() & 0x01) != 0;
  }

  public int getVolume() throws IOException {
    refresh();
    return volumeReg.getValue() & 0x07;
  }

  public int getWakeTimeSeconds() throws IOException {
    refresh();
    return wakeTimeReg.getValue() & 0xFF;
  }

  /**
   * Write-only: request playback for a given command ID via 0x03.
   */
  public void playByCommandId(int cmdId) throws IOException {
    write(REG_BASE + OFF_PLAY_CMD, (byte) (cmdId & 0xFF));
  }

  public void setMuted(boolean mute) throws IOException {
    write(REG_BASE + OFF_MUTE, (byte) (mute ? 1 : 0));
  }

  public void setVolume(int vol) throws IOException {
    int v = Math.max(0, Math.min(7, vol));
    write(REG_BASE + OFF_VOL, (byte) v);
  }

  public String getCommandName() throws IOException {
    int id = getCommandId();
    DF2301QCommand c = DF2301QCommand.fromId(id);
    return (c != null) ? c.getDescription() : "UNKNOWN_" + id;
  }

  public void setWakeTimeSeconds(int seconds) throws IOException {
    int s = Math.max(0, Math.min(255, seconds));
    write(REG_BASE + OFF_WAKE, (byte) s);
  }

  @Override
  public String getName() {
    return "SEN0539";
  }

  @Override
  public String getDescription() {
    return "Offline Voice Recognition Module";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public boolean isConnected() {
    return true;
  }
}