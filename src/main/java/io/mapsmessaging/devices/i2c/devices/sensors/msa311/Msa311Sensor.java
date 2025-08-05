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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.LowPowerBandwidth;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Odr;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.PowerMode;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Range;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

import static io.mapsmessaging.devices.util.Constants.EARTH_GRAVITY_FLOAT;

@SuppressWarnings("java:S6539") // yes this is a monster of a sensor
@Getter
public class Msa311Sensor extends I2CDevice implements Sensor, PowerManagement, Resetable {
  private static final int PART_ID = 0x1;
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
  private final List<SensorReading<?>> readings;

  public Msa311Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Msa311Sensor.class));

    resetRegister = new ResetRegister(this);
    partIdRegister = new PartIdRegister(this);
    rangeRegister = new RangeRegister(this);

    xAxisRegister = new AxisRegister(this, 0x2, "ACC_X");
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

    FloatSensorReading xOrientation = new FloatSensorReading(
        "x_orientation",
        "m/s²",
        "X-axis acceleration from MSA311 sensor",
        0.0f,
        true,
        -100f,
        100f,
        2,
        this::getX
    );

    FloatSensorReading yOrientation = new FloatSensorReading(
        "y_orientation",
        "m/s²",
        "Y-axis acceleration from MSA311 sensor",
        0.0f,
        true,
        -100f,
        100f,
        2,
        this::getY
    );

    FloatSensorReading zOrientation = new FloatSensorReading(
        "z_orientation",
        "m/s²",
        "Z-axis acceleration from MSA311 sensor",
        9.8f,
        true,
        -100f,
        100f,
        2,
        this::getZ
    );

    readings = List.of(xOrientation, yOrientation, zOrientation);
    initialise();
  }

  public static int getId(AddressableDevice device) {
    return device.readRegister(PART_ID);
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
    return (raw / scale) * EARTH_GRAVITY_FLOAT;
  }

  protected float getY() throws IOException {
    float raw = yAxisRegister.getValue();
    float scale = getRange().getScale();
    return (raw / scale) * EARTH_GRAVITY_FLOAT;
  }

  protected float getZ() throws IOException {
    float raw = zAxisRegister.getValue();
    float scale = getRange().getScale();
    return (raw / scale) * EARTH_GRAVITY_FLOAT;
  }

  private Range getRange() {
    return rangeRegister.getRange();
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}