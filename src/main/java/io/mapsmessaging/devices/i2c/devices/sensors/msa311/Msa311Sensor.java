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
import io.mapsmessaging.devices.i2c.devices.RegisterMap;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.*;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class Msa311Sensor extends I2CDevice implements Sensor, PowerManagement, Resetable {
  private final RegisterMap registerMap;
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

  public Msa311Sensor(I2C device) throws IOException {
    super(device, LoggerFactory.getLogger(Msa311Sensor.class));
    registerMap = new RegisterMap();
    resetRegister = new ResetRegister(this, registerMap);
    partIdRegister = new PartIdRegister(this, registerMap);
    xAxisRegister = new AxisRegister(this, 0x2, registerMap);
    yAxisRegister = new AxisRegister(this, 0x4, registerMap);
    zAxisRegister = new AxisRegister(this, 0x6, registerMap);
    motionInterruptRegister = new MotionInterruptRegister(this, registerMap);
    dataReadyRegister = new DataReadyRegister(this, registerMap);
    tapActiveStatusRegister = new TapActiveStatusRegister(this, registerMap);
    orientationRegister = new OrientationRegister(this, registerMap);
    rangeRegister = new RangeRegister(this, registerMap);
    odrRegister = new OdrRegister(this, registerMap);
    powerModeRegister = new PowerModeRegister(this, registerMap);
    swapPolarityRegister = new SwapPolarityRegister(this, registerMap);
    interruptSetRegister = new InterruptSetRegister(this, registerMap);
    interruptSet1Register = new InterruptSet1Register(this, registerMap);
    interruptMap0Register = new InterruptMap0Register(this, registerMap);
    interruptMap1Register = new InterruptMap1Register(this, registerMap);
    intConfigRegister = new IntConfigRegister(this, registerMap);
    intLatchRegister = new IntLatchRegister(this, registerMap);
    freefallDurRegister = new FreefallDurRegister(this, registerMap);
    freefallThRegister = new FreefallThRegister(this, registerMap);
    freefallHyRegister = new FreefallHyRegister(this, registerMap);
    activeDurRegister = new ActiveDurRegister(this, registerMap);
    activeThRegister = new ActiveThRegister(this, registerMap);
    tapDurRegister = new TapDurRegister(this, registerMap);
    tapThresholdRegister = new TapThresholdRegister(this, registerMap);
    orientHyRegister = new OrientHyRegister(this, registerMap);
    zBlockRegister = new ZBlockRegister(this, registerMap);
    xOffsetCompensation = new OffsetCompensationRegister(this, 0x38, "X offset compensation", registerMap);
    yOffsetCompensation = new OffsetCompensationRegister(this, 0x39, "Y offset compensation", registerMap);
    zOffsetCompensation = new OffsetCompensationRegister(this, 0x3A, "Z offset compensation", registerMap);

    powerModeRegister.setPowerMode(PowerMode.NORMAL);
    odrRegister.setOdr(Odr.HERTZ_250);
    rangeRegister.setRange(Range.RANGE_2G);

        /*
      enableAxes(true, true, true);
  // normal mode
  setPowerMode(MSA301_NORMALMODE);
  // 500Hz rate
  setDataRate(MSA301_DATARATE_500_HZ);
  // 250Hz bw
  setBandwidth(MSA301_BANDWIDTH_250_HZ);
  setRange(MSA301_RANGE_4_G);
  setResolution(MSA301_RESOLUTION_14);


     */
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

  public Range getRange() {
    return rangeRegister.getRange();
  }

  public float getX() throws IOException {
    float raw = xAxisRegister.getValue();
    float scale = getRange().getScale();
    return raw / scale;
  }

  public float getY() throws IOException {
    float raw = yAxisRegister.getValue();
    float scale = getRange().getScale();
    return raw / scale;
  }

  public float getZ() throws IOException {
    float raw = zAxisRegister.getValue();
    float scale = getRange().getScale();
    return raw / scale;
  }

  public List<TapActiveStatus> getTapActivity() throws IOException {
    return tapActiveStatusRegister.getTapActiveStatus();
  }

  public OrientationStatus getOrientation() throws IOException {
    return orientationRegister.getOrientation();
  }

  @Override
  public void powerOn() throws IOException {

  }

  @Override
  public void powerOff() throws IOException {

  }

  @Override
  public void reset() throws IOException {

  }

  @Override
  public void softReset() throws IOException {

  }

  @Override
  public String toString() {
    return registerMap.toString();
  }

}