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
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.LowPowerBandwidth;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Odr;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.PowerMode;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Range;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

public class Msa311Sensor extends I2CDevice implements Sensor, PowerManagement, Resetable {
  private static final float GRAVITY = 9.80665f; //m/s^2
  private static final int PART_ID = 0x1;

  public static int getId(I2C device) {
    return device.readRegister(PART_ID);
  }

  @Getter
  private final ResetRegister resetRegister;
  @Getter
  private final PartIdRegister partIdRegister;
  @Getter
  private final AxisRegister xAxisRegister;
  @Getter
  private final AxisRegister yAxisRegister;
  @Getter
  private final AxisRegister zAxisRegister;
  @Getter
  private final MotionInterruptRegister motionInterruptRegister;
  @Getter
  private final DataReadyRegister dataReadyRegister;
  @Getter
  private final TapActiveStatusRegister tapActiveStatusRegister;
  @Getter
  private final OrientationRegister orientationRegister;
  @Getter
  private final RangeRegister rangeRegister;
  @Getter
  private final OdrRegister odrRegister;
  @Getter
  private final PowerModeRegister powerModeRegister;
  @Getter
  private final SwapPolarityRegister swapPolarityRegister;
  @Getter
  private final InterruptSet0Register interruptSetRegister;
  @Getter
  private final InterruptSet1Register interruptSet1Register;
  @Getter
  private final InterruptMap0Register interruptMap0Register;
  @Getter
  private final InterruptMap1Register interruptMap1Register;
  @Getter
  private final IntConfigRegister intConfigRegister;
  @Getter
  private final IntLatchRegister intLatchRegister;
  @Getter
  private final FreefallDurRegister freefallDurRegister;
  @Getter
  private final FreefallThRegister freefallThRegister;
  @Getter
  private final FreefallHyRegister freefallHyRegister;
  @Getter
  private final ActiveDurRegister activeDurRegister;
  @Getter
  private final ActiveThRegister activeThRegister;
  @Getter
  private final TapDurRegister tapDurRegister;
  @Getter
  private final TapThresholdRegister tapThresholdRegister;
  @Getter
  private final OrientHyRegister orientHyRegister;
  @Getter
  private final ZBlockRegister zBlockRegister;
  @Getter
  private final OffsetCompensationRegister xOffsetCompensation;
  @Getter
  private final OffsetCompensationRegister yOffsetCompensation;
  @Getter
  private final OffsetCompensationRegister zOffsetCompensation;

  @Getter
  private final List<SensorReading<?>> readings;

  public Msa311Sensor(I2C device) throws IOException {
    super(device, LoggerFactory.getLogger(Msa311Sensor.class));
    resetRegister = new ResetRegister(this);
    partIdRegister = new PartIdRegister(this);
    rangeRegister = new RangeRegister(this);

    xAxisRegister = new AxisRegister(this, 0x2,"ACC_X");
    yAxisRegister = new AxisRegister(this, 0x4, "ACC_Y");
    zAxisRegister = new AxisRegister(this, 0x6, "ACC_Z");
    motionInterruptRegister = new MotionInterruptRegister(this);
    dataReadyRegister = new DataReadyRegister(this);
    tapActiveStatusRegister = new TapActiveStatusRegister(this);
    orientationRegister = new OrientationRegister(this);
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
    activeThRegister = new ActiveThRegister(this, rangeRegister);
    tapDurRegister = new TapDurRegister(this);
    tapThresholdRegister = new TapThresholdRegister(this, rangeRegister);
    orientHyRegister = new OrientHyRegister(this);
    zBlockRegister = new ZBlockRegister(this);
    xOffsetCompensation = new OffsetCompensationRegister(this, 0x38, "X offset compensation");
    yOffsetCompensation = new OffsetCompensationRegister(this, 0x39, "Y offset compensation");
    zOffsetCompensation = new OffsetCompensationRegister(this, 0x3A, "Z offset compensation");


    FloatSensorReading xOrientation = new FloatSensorReading("x_orientation", "m/s^2", -100, 100, this::getX);
    FloatSensorReading yOrientation = new FloatSensorReading("y_orientation", "m/s^2", -100, 100, this::getY);
    FloatSensorReading zOrientation = new FloatSensorReading("z_orientation", "m/s^2", -100, 100, this::getZ);

    readings = List.of(xOrientation, yOrientation, zOrientation);
    initialise();
  }

  public void initialise() throws IOException {
    reset();
    delay(10);
    powerModeRegister.setPowerMode(PowerMode.NORMAL);
    powerModeRegister.setLowPowerBandwidth(LowPowerBandwidth.HERTZ_250);
    rangeRegister.setRange(Range.RANGE_4G);
    odrRegister.setOdr(Odr.HERTZ_500);
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
    powerModeRegister.setPowerMode(PowerMode.NORMAL);
  }

  @Override
  public void powerOff() throws IOException {
    powerModeRegister.setPowerMode(PowerMode.SUSPENDED);
  }

  @Override
  public void reset() throws IOException {
    resetRegister.reset();
  }

  @Override
  public void softReset() throws IOException {
    initialise();
  }

  @Override
  public String toString() {
    return registerMap.toString();
  }

  protected float getX() throws IOException {
    float raw = xAxisRegister.getValue();
    float scale = getRange().getScale();
    return (raw / scale) * GRAVITY;
  }

  protected float getY() throws IOException {
    float raw = yAxisRegister.getValue();
    float scale = getRange().getScale();
    return (raw / scale) * GRAVITY;
  }

  protected float getZ() throws IOException {
    float raw = zAxisRegister.getValue();
    float scale = getRange().getScale();
    return (raw / scale) * GRAVITY;
  }

  private Range getRange() {
    return rangeRegister.getRange();
  }

}