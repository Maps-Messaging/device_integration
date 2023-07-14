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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.PowerManagement;
import io.mapsmessaging.devices.Resetable;
import io.mapsmessaging.devices.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers.*;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class Msa311Sensor extends I2CDevice implements Sensor, PowerManagement, Resetable {

  private final ResetRegister resetRegister;
  private final PartIdRegister partIdRegister;
  private final AxisRegister xAxisRegister;
  private final AxisRegister yAxisRegister;
  private final AxisRegister zAxisRegister;
  private final MotionInterruptRegister motionInterruptRegister;
  private final DataReadyRegister dataReadyRegister;
  private final TapActiveStatusRegister tapActiveStatusRegister;
  private final OrientationRegister orientationRegister;
  private final RangeRegister rangeRegister;
  private final OdrRegister odrRegister;
  private final PowerModeRegister powerModeRegister;
  private final SwapPolarityRegister swapPolarityRegister;
  private final InterruptSetRegister interruptSetRegister;
  private final InterruptSet1Register interruptSet1Register;
  private final InterruptMap0Register interruptMap0Register;
  private final InterruptMap1Register interruptMap1Register;
  private final IntConfigRegister intConfigRegister;
  private final IntLatchRegister intLatchRegister;
  private final FreefallDurRegister freefallDurRegister;
  private final FreefallThRegister freefallThRegister;
  private final FreefallHyRegister freefallHyRegister;
  private final ActiveDurRegister activeDurRegister;
  private final ActiveThRegister activeThRegister;
  private final TapDurRegister tapDurRegister;
  private final TapThresholdRegister tapThresholdRegister;
  private final OrientHyRegister orientHyRegister;
  private final ZBlockRegister zBlockRegister;
  private final OffsetCompensationRegister xOffsetCompensation;
  private final OffsetCompensationRegister yOffsetCompensation;
  private final OffsetCompensationRegister zOffsetCompensation;

  public Msa311Sensor(I2C device) {
    super(device, LoggerFactory.getLogger(Msa311Sensor.class));
    resetRegister = new ResetRegister(this);
    partIdRegister = new PartIdRegister(this);
    xAxisRegister = new AxisRegister(this, 0x2);
    yAxisRegister = new AxisRegister(this, 0x4);
    zAxisRegister = new AxisRegister(this, 0x6);
    motionInterruptRegister = new MotionInterruptRegister(this);
    dataReadyRegister = new DataReadyRegister(this);
    tapActiveStatusRegister = new TapActiveStatusRegister(this);
    orientationRegister = new OrientationRegister(this);
    rangeRegister = new RangeRegister(this);
    odrRegister = new OdrRegister(this);
    powerModeRegister = new PowerModeRegister(this);
    swapPolarityRegister = new SwapPolarityRegister(this);
    interruptSetRegister = new InterruptSetRegister(this);
    interruptSet1Register = new InterruptSet1Register(this);
    interruptMap0Register = new InterruptMap0Register(this);
    interruptMap1Register = new InterruptMap1Register(this);
    intConfigRegister = new IntConfigRegister(this);
    intLatchRegister = new IntLatchRegister(this);
    freefallDurRegister = new FreefallDurRegister(this);
    freefallThRegister = new FreefallThRegister(this);
    freefallHyRegister = new FreefallHyRegister(this);
    activeDurRegister = new ActiveDurRegister(this);
    activeThRegister = new ActiveThRegister(this);
    tapDurRegister = new TapDurRegister(this);
    tapThresholdRegister = new TapThresholdRegister(this);
    orientHyRegister = new OrientHyRegister(this);
    zBlockRegister = new ZBlockRegister(this);
    xOffsetCompensation = new OffsetCompensationRegister(this, 0x38);
    yOffsetCompensation = new OffsetCompensationRegister(this, 0x39);
    zOffsetCompensation = new OffsetCompensationRegister(this, 0x3A);
  }


  @Override
  public boolean isConnected() {
    return true;
  }


  @Override
  public String getName() {
    return "MSA311";
  }

  @Override
  public String getDescription() {
    return "Digital Tri-axial Accelerometer";
  }


  @Override
  public void powerOn() throws IOException {

  }

  @Override
  public void powerOff() throws IOException {

  }

  @Override
  public void reset()  throws IOException{

  }

  @Override
  public void softReset()  throws IOException{

  }
}