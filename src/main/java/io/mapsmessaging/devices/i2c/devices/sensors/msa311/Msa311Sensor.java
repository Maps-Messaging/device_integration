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
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.*;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class Msa311Sensor extends I2CDevice implements Sensor, PowerManagement, Resetable {
  private static final float GRAVITY = 9.80665f; //m/s^2

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
  private final InterruptSet0Register interruptSetRegister;
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
    resetRegister = new ResetRegister(this);
    partIdRegister = new PartIdRegister(this);
    xAxisRegister = new AxisRegister(this, 0x2,"ACC_X");
    yAxisRegister = new AxisRegister(this, 0x4, "ACC_Y");
    zAxisRegister = new AxisRegister(this, 0x6, "ACC_Z");
    motionInterruptRegister = new MotionInterruptRegister(this);
    dataReadyRegister = new DataReadyRegister(this);
    tapActiveStatusRegister = new TapActiveStatusRegister(this);
    orientationRegister = new OrientationRegister(this);
    rangeRegister = new RangeRegister(this);
    odrRegister = new OdrRegister(this);
    powerModeRegister = new PowerModeRegister(this);
    swapPolarityRegister = new SwapPolarityRegister(this);
    interruptSetRegister = new InterruptSet0Register(this);
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
    xOffsetCompensation = new OffsetCompensationRegister(this, 0x38, "X offset compensation");
    yOffsetCompensation = new OffsetCompensationRegister(this, 0x39, "Y offset compensation");
    zOffsetCompensation = new OffsetCompensationRegister(this, 0x3A, "Z offset compensation");

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
    return (raw / scale) * GRAVITY;
  }

  public float getY() throws IOException {
    float raw = yAxisRegister.getValue();
    float scale = getRange().getScale();
    return (raw / scale) * GRAVITY;
  }

  public float getZ() throws IOException {
    float raw = zAxisRegister.getValue();
    float scale = getRange().getScale();
    return (raw / scale) * GRAVITY;
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