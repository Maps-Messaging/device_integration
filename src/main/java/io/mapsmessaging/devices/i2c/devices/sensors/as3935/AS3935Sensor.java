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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.*;
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

  public AS3935Sensor(AddressableDevice device, int tuning) throws IOException {
    super(device, LoggerFactory.getLogger(AS3935Sensor.class));
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
      powerOn();
      reset();
    }
    IntegerSensorReading energySensor = new IntegerSensorReading(
        "lightning_energy",
        "arbitrary",
        "Energy value of the last lightning event",
        1200,
        true,
        0,
        0xFFFF,
        this::getEnergy
    );

    IntegerSensorReading distance = new IntegerSensorReading(
        "lightning_distance",
        "km",
        "Estimated distance to the lightning strike",
        5,
        true,
        0,
        63,  // 0b0111111
        this::getDistanceEstimation
    );

    StringSensorReading reason = new StringSensorReading(
        "interrupt_reason",
        "",
        "Last interrupt reason (e.g., noise, disturber, lightning)",
        "lightning",
        true,
        this::getInterruptReason
    );

    readings = generateSensorReadings(List.of(energySensor, distance, reason));
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public void softReset() throws IOException {
    if (tuning >= 0 && tuning <= 15) {
      tunCapRegister.setTuningCap(tuning);
    } else {
      throw new IOException("Value of TUN_CAP must be between 0 and 15");
    }
    tunCapRegister.setDispTRCOEnabled(true);
    delay(2);
    tunCapRegister.setDispTRCOEnabled(false);
  }

  @Override
  public void reset() throws IOException {
    presetDefaultRegister.reset();
    calibrateRcoRegister.reset();
    delay(200);
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
    tunCapRegister.setDispTRCOEnabled(true);
    tunCapRegister.setDispTRCOEnabled(false);
  }

  public void powerOff() throws IOException {
    afeRegister.setPowerDown(true);
  }

  protected String getInterruptReason() {
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