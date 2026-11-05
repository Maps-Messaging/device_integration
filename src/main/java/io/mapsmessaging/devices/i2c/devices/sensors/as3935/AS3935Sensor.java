/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.values.InterruptReason;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class AS3935Sensor extends I2CDevice implements PowerManagement, Sensor, Resetable {

  private final AfeRegister afeRegister;
  @Getter
  private final ThresholdRegister thresholdRegister;
  @Getter
  private final CalibTrcoRegister calibSrcoTrcoRegister;
  @Getter
  private final CalibrateSrcoRegister calibSrcoSrcoRegister;
  @Getter
  private final DistanceRegister distanceRegister;
  @Getter
  private final InterruptRegister interruptRegister;
  @Getter
  private final LightningStrikeRegister lightningStrikeRegister;
  @Getter
  private final LightningRegister lightningRegister;
  @Getter
  private final TunCapRegister tunCapRegister;
  @Getter
  private final CalibrateRcoRegister calibrateRcoRegister;
  @Getter
  private final PresetDefaultRegister presetDefaultRegister;
  @Getter
  private final List<SensorReading<?>> readings;

  private final int tuning;
  @Getter(lombok.AccessLevel.NONE)
  private final ThreadLocal<SampleState> sampleState = new ThreadLocal<>();

  private record Sample(int energy, int distance, InterruptReason reason) { }

  private static class SampleState {
    private Sample sample;
    private IOException failure;
  }

  public AS3935Sensor(AddressableDevice device, int tuning) throws IOException {
    super(device, LoggerFactory.getLogger(AS3935Sensor.class));
    if (tuning < 0 || tuning > 15) {
      throw new IOException("Value of TUN_CAP must be between 0 and 15");
    }
    afeRegister = new AfeRegister(this);
    thresholdRegister = new ThresholdRegister(this);
    calibSrcoTrcoRegister = new CalibTrcoRegister(this);
    calibSrcoSrcoRegister = new CalibrateSrcoRegister(this);
    distanceRegister = new DistanceRegister(this);
    interruptRegister = new InterruptRegister(this);
    lightningStrikeRegister = new LightningStrikeRegister(this);
    lightningRegister = new LightningRegister(this);
    tunCapRegister = new TunCapRegister(this);
    calibrateRcoRegister = new CalibrateRcoRegister(this);
    presetDefaultRegister = new PresetDefaultRegister(this);
    this.tuning = tuning;
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      reset();
    }
    IntegerSensorReading energySensor = new IntegerSensorReading(
        "lightning_energy",
        "arbitrary",
        "Raw energy for the reported lightning interrupt; zero otherwise",
        1200,
        true,
        0,
        0x1FFFFF,
        () -> getSample().energy()
    );

    IntegerSensorReading distance = new IntegerSensorReading(
        "lightning_distance",
        "km",
        "Estimated storm distance in km; -1 when unavailable",
        5,
        true,
        -1,
        40,
        () -> getSample().distance()
    );

    StringSensorReading reason = new StringSensorReading(
        "interrupt_reason",
        "",
        "Last interrupt reason (e.g., noise, disturber, lightning)",
        "lightning",
        true,
        () -> getSample().reason().getDescription()
    );

    readings = generateSensorReadings(List.of(energySensor, distance, reason));
  }

  @Override
  public boolean isConnected() {
    try {
      return readRegister(0) >= 0;
    } catch (IOException e) {
      return false;
    }
  }

  void beginSample() {
    sampleState.set(new SampleState());
  }

  void invalidateSample() {
    sampleState.remove();
  }

  private Sample getSample() throws IOException {
    SampleState state = sampleState.get();
    if (state == null) {
      return readSample();
    }
    if (state.failure != null) {
      throw state.failure;
    }
    if (state.sample == null) {
      try {
        state.sample = readSample();
      } catch (IOException e) {
        state.failure = e;
        throw e;
      }
    }
    return state.sample;
  }

  private Sample readSample() throws IOException {
    // Without an IRQ edge this is best-effort polling, not guaranteed event capture.
    delay(2);
    byte[] data = new byte[5];
    if (readRegister(0x03, data, 0, data.length) != data.length) {
      throw new IOException("Incomplete AS3935 event reading");
    }
    InterruptReason reason = InterruptReason.NONE;
    for (InterruptReason candidate : InterruptReason.values()) {
      if (candidate.getMask() == (data[0] & 0x0f)) {
        reason = candidate;
        break;
      }
    }
    int energy = 0;
    int distance = -1;
    if (reason == InterruptReason.INT_L) {
      energy = ((data[3] & 0x1f) << 16) | ((data[2] & 0xff) << 8) | (data[1] & 0xff);
      int rawDistance = data[4] & 0x3f;
      if (rawDistance >= 1 && rawDistance <= 40) {
        distance = rawDistance;
      }
    }
    return new Sample(energy, distance, reason);
  }

  @Override
  public void softReset() throws IOException {
    invalidateSample();
    tunCapRegister.setTuningCap(tuning);
    calibrateRcoRegister.reset();
    tunCapRegister.setDispSRCOEnabled(true);
    delay(2);
    tunCapRegister.setDispSRCOEnabled(false);
    int trco = readRegister(0x3a);
    int srco = readRegister(0x3b);
    if ((trco & 0xc0) != 0x80 || (srco & 0xc0) != 0x80) {
      throw new IOException("AS3935 RC oscillator calibration failed");
    }
  }

  @Override
  public void reset() throws IOException {
    presetDefaultRegister.reset();
    delay(200);
    afeRegister.setPowerDown(false);
    softReset();
  }

  @Override
  public String getName() {
    return "AS3935";
  }

  @Override
  public String getDescription() {
    return "Lightning detector and warning sensor";
  }

  public void powerOn() throws IOException {
    afeRegister.setPowerDown(false);
    softReset();
  }

  public void powerOff() throws IOException {
    afeRegister.setPowerDown(true);
    invalidateSample();
  }

  protected String getInterruptReason() throws IOException {
    return interruptRegister.getInterruptReason().getDescription();
  }

  public int getEnergy() throws IOException {
    return lightningStrikeRegister.getEnergy();
  }

  protected int getDistanceEstimation() throws IOException {
    return distanceRegister.getDistanceEstimation();
  }

  @Override
  public String toString() {
    return registerMap.toString();
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}
