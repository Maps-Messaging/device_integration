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
import io.mapsmessaging.devices.PowerManagement;
import io.mapsmessaging.devices.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.*;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class AS3935Sensor extends I2CDevice implements PowerManagement, Sensor {
  private final AfeRegister afeRegister;
  private final ThresholdRegister thresholdRegister;
  private final Calib_SRCO_TRCO_Register calibSrcoTrcoRegister;
  private final Calib_SRCO_SRCO_Register calibSrcoSrcoRegister;
  private final DistanceRegister distanceRegister;
  private final InterruptRegister interruptRegister;
  private final Lightning_Strike_Register lightningStrikeRegister;
  private final LightningRegister lightningRegister;
  private final Tun_Cap_Register tunCapRegister;

  private final int tuning;

  public AS3935Sensor(I2C device, int tuning) throws IOException {
    super(device, LoggerFactory.getLogger(AS3935Sensor.class));
    afeRegister = new AfeRegister(this);
    thresholdRegister = new ThresholdRegister(this);
    calibSrcoTrcoRegister = new Calib_SRCO_TRCO_Register(this);
    calibSrcoSrcoRegister = new Calib_SRCO_SRCO_Register(this);
    distanceRegister = new DistanceRegister(this);
    interruptRegister = new InterruptRegister(this);
    lightningStrikeRegister = new Lightning_Strike_Register(this);
    lightningRegister = new LightningRegister(this);
    tunCapRegister = new Tun_Cap_Register(this);
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

  public void setup() throws IOException {
    delay(80);
    if (tuning != 0) {
      if (tuning < 0x10 && tuning > -1) {
        tunCapRegister.setTuningCap(tuning);
      }
      delay(200);
    } else {
      throw new IOException("Value of TUN_CAP must be between 0 and 15");
    }
    tunCapRegister.setDispSRCOEnabled(true);
    delay(200);
    tunCapRegister.setDispTRCOEnabled(true);
    delay(200);
  }


  @Override
  public String getName() {
    return "AS3935";
  }

  @Override
  public String getDescription() {
    return "Lightning detector and warning sensor";
  }

  // AFE_GAIN Register : 0
  public boolean isAFE_PowerDown() throws IOException {
    return afeRegister.isAFE_PowerDown();
  }

  public void powerOn() throws IOException {
    afeRegister.setAFE_PowerDown(false);
    tunCapRegister.setDispTRCOEnabled(true);
    delay(2);
    tunCapRegister.setDispTRCOEnabled(false);
  }

  public void powerOff() throws IOException {
    afeRegister.setAFE_PowerDown(true);
  }

  public int getAFE_GainBoost() throws IOException {
    return afeRegister.getAFE_GainBoost();
  }

  public void setAFE_GainBoost(int gainBoost) throws IOException {
    afeRegister.setAFE_GainBoost(gainBoost);
  }

  // THRESHOLD Register : 1
  public int getWatchdogThreshold() throws IOException {
    return thresholdRegister.getWatchdogThreshold();
  }

  public void setWatchdogThreshold(int threshold) throws IOException {
    thresholdRegister.setWatchdogThreshold(threshold);
  }

  public int getNoiseFloorLevel() throws IOException {
    return thresholdRegister.getNoiseFloorLevel();
  }

  public void setNoiseFloorLevel(int level) throws IOException {
    thresholdRegister.setNoiseFloorLevel(level);
  }

  // LIGHTNING_REG Register : 2
  public int getSpikeRejection() throws IOException {
    return lightningRegister.getSpikeRejection();
  }

  public void setSpikeRejection(int rejection) throws IOException {
    lightningRegister.setSpikeRejection(rejection);
  }

  public int getMinNumLightning() throws IOException {
    return lightningRegister.getMinNumLightning();
  }

  public void setMinNumLightning(int numLightning) throws IOException {
    lightningRegister.setMinNumLightning(numLightning);
  }

  public boolean isClearStatisticsEnabled() throws IOException {
    return lightningRegister.isClearStatisticsEnabled();
  }

  public void setClearStatisticsEnabled(boolean enabled) throws IOException {
    lightningRegister.setClearStatisticsEnabled(enabled);
  }

  // Interrupt Register

  public int getInterruptReason() throws IOException {
    return interruptRegister.getInterruptReason();
  }


  public boolean isMaskDisturberEnabled() throws IOException {
    return interruptRegister.isMaskDisturberEnabled();
  }

  public void setMaskDisturberEnabled(boolean enabled) throws IOException {
    interruptRegister.setMaskDisturberEnabled(enabled);
  }

  public int getEnergyDivRatio() throws IOException {
    return interruptRegister.getEnergyDivRatio();
  }

  public void setEnergyDivRatio(int divRatio) throws IOException {
    interruptRegister.setEnergyDivRatio(divRatio);
  }

  // DISTANCE Register
  public int getDistanceEstimation() throws IOException {
    return distanceRegister.getDistanceEstimation();
  }

  // TUN_CAP Register
  public int getTuningCap() throws IOException {
    return tunCapRegister.getTuningCap();
  }

  public void setTuningCap(int cap) throws IOException {
    tunCapRegister.setTuningCap(cap);
  }

  public boolean isDispTRCOEnabled() throws IOException {
    return tunCapRegister.isDispTRCOEnabled();
  }

  public void setDispTRCOEnabled(boolean enabled) throws IOException {
    tunCapRegister.setDispTRCOEnabled(enabled);
  }

  public boolean isDispSRCOEnabled() throws IOException {
    return tunCapRegister.isDispSRCOEnabled();
  }

  public void setDispSRCOEnabled(boolean enabled) throws IOException {
    tunCapRegister.setDispSRCOEnabled(enabled);
  }

  // CALIB_SRCO_TRCO Register
  public boolean isTRCOCalibrationSuccessful() throws IOException {
    return calibSrcoTrcoRegister.isTRCOCalibrationSuccessful();
  }

  // CALIB_SCRO_SRCO Register
  public boolean isSRCOCalibrationSuccessful() throws IOException {
    return calibSrcoSrcoRegister.isSRCOCalibrationSuccessful();
  }

  // LIGHTNING_STRIKE Register
  public int getEnergy() throws IOException {
    return lightningStrikeRegister.getEnergy();
  }

  @Override
  public String toString() {
    return registerMap.toString();
  }
}